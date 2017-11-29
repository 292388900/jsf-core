/**
 * Copyright 2004-2048 .
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.worker.common;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.worker.common.domain.WorkerAlarm;
import com.ipd.jsf.worker.common.event.*;
import com.ipd.jsf.worker.common.schedule.DefaultScheduleDataManager;
import com.ipd.jsf.worker.common.service.WorkerAlarmService;
import com.ipd.jsf.worker.common.service.WorkerDefineService;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.common.domain.WorkerInfo;
import com.ipd.jsf.zookeeper.IZkDataListener;
import com.ipd.jsf.zookeeper.IZkStateListener;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.cache.PathCache;
import com.ipd.jsf.zookeeper.cache.PathCacheListener;
import com.ipd.jsf.zookeeper.cache.PathNode;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.apache.zookeeper.Watcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.PriorityOrdered;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class WorkerManager implements BeanPostProcessor,PriorityOrdered,ApplicationListener,ApplicationContextAware,WorkerEventPublisher,InitializingBean {

    private Logger logger = LoggerFactory.getLogger(getClass());


    private ApplicationContext ac;

    /**
     * 所有worker
     * key:workerType
     * value:worker bean name
     */
    private Map<String,String> workers = new HashMap<String, String>();

    private ZkClient zkClient;

    private ScheduleDataManager scheduleDataManager;

    /**
     * key:workerType
     *
     * value:ScheduleManager
     *
     */
    private final Map<String,ScheduleManager> scheduleManagers = new HashMap<String, ScheduleManager>();

    private WorkerEventMulticaster multicaster = new SimpleWorkerEventMulticaster();

    private Timer scanMasterTimer ;

    /**
     * 统计master 结点不在server结点下的情况
     *
     * key:workerType
     * value:MasterStatistics
     */
    private ConcurrentHashMap<String,MasterStatistics> masterStatisticsMap = new ConcurrentHashMap<String, MasterStatistics>();

    @Autowired
    private WorkerDefineService workerDefineServiceImpl;

    @Autowired
    private WorkerAlarmService workerAlarmServiceImpl;

    private ExecutorService workerAlarmSendService = Executors.newFixedThreadPool(2, new WorkerThreadFactory("workerManger"));

    private PropertyFactory propertyFactory;


    public void init(){
        try {
            propertyFactory = new PropertyFactory("worker.properties");
            zkClient = new ZkClient((String) propertyFactory.getProperty("zk.address"),Long.valueOf(propertyFactory.getProperty("zk.connectionTimeout","10000")),
                    Integer.valueOf(propertyFactory.getProperty("zk.sessionTimeout","30000")));
        } catch (IOException e) {
            logger.error("create zkClient error",e);
            return;
        }
        scheduleDataManager = new DefaultScheduleDataManager(zkClient,this);
        //worker server 上、下线领取任务监听
        multicaster.addWorkerListener(new WorkerServerListener());
        //worker task 创建监听
        multicaster.addWorkerListener(new WorkerTaskCreatedListener());


    }




    /**
     * 注册zk事件（触发master的争夺）
     *<br/>
     * 触发点:
     *
     * <ul>
     *  <li>初始状态无master结点时,直接作为master</li>
     *  <li>master结点下线时，其他结点争夺</li>
     *  <li>下线master重新上线时</li>
     * <ul/>
     * @param workerType
     */
    private void initServerZKListener(final String workerType){
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        PathCache pathCache = new PathCache(zkClient,zkPath,true);

        pathCache.addNodeAddedListener(new PathCacheListener() {
            @Override
            public void processEvent(String parentPath, List<PathNode> childrenPath) throws Exception {
                ScheduleManager scheduleManager = getScheduleManagers().get(workerType);
                scheduleDataManager.contendMaster(workerType, scheduleManager.getCurrentScheduleServer());
                publishWorkerEvent(new WorkerServerEvent(WorkerManager.this,scheduleManager));
            }
        });
        pathCache.addNodeDeletedListener(new PathCacheListener() {
            @Override
            public void processEvent(String parentPath, List<PathNode> childrenPath) throws Exception {
                ScheduleManager scheduleManager = getScheduleManagers().get(workerType);
                scheduleDataManager.contendMaster(workerType, scheduleManager.getCurrentScheduleServer());
                publishWorkerEvent(new WorkerServerEvent(WorkerManager.this,scheduleManager));
            }
        });
        pathCache.start();
        zkClient.subscribeStateChanges(new IZkStateListener() {
            @Override
            public void handleStateChanged(Watcher.Event.KeeperState state) throws Exception {
                if ( state == Watcher.Event.KeeperState.SyncConnected ){
                    ScheduleManager scheduleManager = getScheduleManagers().get(workerType);
                    scheduleDataManager.contendMaster(workerType, scheduleManager.getCurrentScheduleServer());
                    publishWorkerEvent(new WorkerServerEvent(WorkerManager.this,scheduleManager));
                }
            }

            @Override
            public void handleNewSession() throws Exception {
                ScheduleManager scheduleManager = getScheduleManagers().get(workerType);
                scheduleDataManager.contendMaster(workerType,scheduleManager.getCurrentScheduleServer());
                logger.info("reRegister schedule server {} for [{}]",scheduleManager.getCurrentScheduleServer().getId(),workerType);
                scheduleDataManager.registerScheduleServer(scheduleManager.getCurrentScheduleServer());
                publishWorkerEvent(new WorkerServerEvent(WorkerManager.this,scheduleManager));
            }
        });
        //master 切换监听
        zkClient.subscribeDataChanges(zkPath,new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                ScheduleManager scheduleManager = getScheduleManagers().get(workerType);
                scheduleManager.refresh();
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        });
    }


    /**
     * 创建调度管理器
     *
     * @return
     */
    public void createScheduleManager(){
        if ( workers == null || workers.size() == 0 ){
            return;
        }
        for ( Map.Entry<String,String> entry : workers.entrySet() ){
            if ( getWorker(entry.getKey()) instanceof DistributeWorker ){
                scheduleDataManager.initialData(entry.getKey(), true);
            } else {
                scheduleDataManager.initialData(entry.getKey(), false);
            }
            initServerZKListener(entry.getKey());
            new ScheduleManager(entry.getKey(),scheduleDataManager,this);
        }
    }


    public Map<String, ScheduleManager> getScheduleManagers() {
        return scheduleManagers;
    }

    public void addScheduleManager(String workerType,ScheduleManager scheduleManager){
        scheduleManagers.put(workerType,scheduleManager);
    }

    /**
     *
     * @param workerType
     * @return 返回worker实例
     */
    public Worker getWorker(String workerType){
        if (StringUtils.isEmpty(workerType)){
            return null;
        }
        return ac.getBean(workers.get(workerType),Worker.class);
    }


    /**
     * worker 报警
     *
     * @param workerAlarm
     */
    public void sendWorkerAlarm(final WorkerAlarm workerAlarm){
        workerAlarmSendService.execute(new Runnable() {
            @Override
            public void run() {
                workerAlarmServiceImpl.addWorkerAlarm(workerAlarm);
            }
        });
    }


    /**
     * 加载最新的worker信息
     * @param worker
     */
    public void loadNewestWorkerInfo(Worker worker){
        logger.info(" load newest worker info {}",worker.getWorkerType());
        WorkerInfo workerInfo = workerDefineServiceImpl.loadWorkerInfo(worker.getWorkerType());
        if ( workerInfo != null && worker instanceof AbstractWorker ){
            ((AbstractWorker) worker).setCronExpression(workerInfo.getCronExpression());
            ((AbstractWorker) worker).setImmediate(workerInfo.isImmediate());
            ((AbstractWorker) worker).setWorkerParameters(JSON.parseObject(workerInfo.getWorkerParameters()));
            ((AbstractWorker) worker).setErrorAlert(workerInfo.isErrorAlert());
            ((AbstractWorker) worker).setStatus(workerInfo.getActive());
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }

    @Override
    public void onApplicationEvent(ApplicationEvent applicationEvent) {
        //spring 容器加载完毕
        if ( applicationEvent instanceof ContextRefreshedEvent){
            initWorker();
            if ( workers.size() > 0 ){
                createScheduleManager();
                startScanMasterStatTask();
            }
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if ( bean instanceof Worker){
            Worker worker = (Worker) bean;
            if ( worker.getWorkerType() == null || "".equals(worker.getWorkerType())){
                logger.error(" worker spring id is :{} . workerType is null.ignore this worker.",beanName);
                return bean;
            }
            workers.put(((Worker) bean).getWorkerType(),beanName);
            if ( bean instanceof WorkerListener){
                multicaster.addWorkerListener((WorkerListener) bean);
            }
            if ( bean instanceof ZKClientAware){
                ((ZKClientAware) bean).setZKClient(zkClient);
            }
            if ( bean instanceof ScheduleDataManagerAware){
                ((ScheduleDataManagerAware) bean).setScheduleDataManager(scheduleDataManager);
            }
        }
        return bean;
    }

    private void startScanMasterStatTask(){
        scanMasterTimer = new Timer();
        scanMasterTimer.schedule(new ScanWorkerMasterTask(),Long.valueOf(propertyFactory.getProperty("master.stats.scan.delay","1000")),
                Long.valueOf(propertyFactory.getProperty("master.stats.scan.period","5000")));
    }

    /**
     * 将数据库中的worker定义信息赋值到worker bean当中
     *
     */
    private void initWorker(){
        if ( "false".equals(propertyFactory.getProperty("workerFromDB","true"))){
            return;
        }
        if ( workers != null && !workers.isEmpty() && workerDefineServiceImpl != null){
            List<WorkerInfo> workerInfos = workerDefineServiceImpl.loadWorkerInfos(Lists.newArrayList(workers.keySet()));
            if ( workerInfos != null ){
                for ( WorkerInfo workerInfo :workerInfos ){
                    Worker worker = getWorker(workerInfo.getWorkerName());
                    if ( worker instanceof AbstractWorker ){
                        ((AbstractWorker) worker).setCronExpression(workerInfo.getCronExpression());
                        ((AbstractWorker) worker).setImmediate(workerInfo.isImmediate());
                        try {
                            ((AbstractWorker) worker).setWorkerParameters(JSON.parseObject(workerInfo.getWorkerParameters()));
                        } catch (Exception e) {
                            logger.warn("{} worker worker parameter is invalid json",worker.getWorkerType());
                        }
                        ((AbstractWorker) worker).setErrorAlert(workerInfo.isErrorAlert());
                        ((AbstractWorker) worker).setStatus(workerInfo.getActive());
                    }
                }
            }
        }
    }



    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        ac = applicationContext;
    }

    @Override
    public void publishWorkerEvent(WorkerEvent event) {
        multicaster.multicastEvent(event);
    }

    public WorkerDefineService getWorkerDefineServiceImpl() {
        return workerDefineServiceImpl;
    }

    public void setWorkerDefineServiceImpl(WorkerDefineService workerDefineServiceImpl) {
        this.workerDefineServiceImpl = workerDefineServiceImpl;
    }

    public WorkerAlarmService getWorkerAlarmServiceImpl() {
        return workerAlarmServiceImpl;
    }

    public void setWorkerAlarmServiceImpl(WorkerAlarmService workerAlarmServiceImpl) {
        this.workerAlarmServiceImpl = workerAlarmServiceImpl;
    }

    /**
     * 扫描所有worker的master的存活状态，防止zookeeper watcher失效的情况下
     * <br/>
     * master 结点没有及时更新
     *
     */
    class ScanWorkerMasterTask extends TimerTask{

        @Override
        public void run() {
            if ( workers == null || workers.size() == 0 ){
                return;
            }
            for ( Map.Entry<String,String> entry : workers.entrySet() ){
                MasterStatistics masterStat =masterStatisticsMap.putIfAbsent(entry.getKey(),new MasterStatistics());
                if ( masterStat == null ){
                    masterStat = masterStatisticsMap.get(entry.getKey());
                }

                if ( isMasterStale(entry.getKey())){
                    masterStat.addOfflineStats(true);
                } else {
                    masterStat.addOfflineStats(false);
                }
                //连续{master.stats.scan.timesforjudge}次master结点都不在列表当中
                if ( masterStat.getScanTimes() == Integer.valueOf(propertyFactory.getProperty("master.stats.scan.timesforjudge","2")) ){
                    ScheduleManager scheduleManager = scheduleManagers.get(entry.getKey());
                    if ( scheduleManager != null ){
                        if ( masterStat.isMasterStale() ){
                            logger.info("{} master server id is stale so that schedule manager contendMaster and refresh it",entry.getKey());
                            scheduleDataManager.contendMaster(entry.getKey(),scheduleManager.getCurrentScheduleServer());
                            scheduleManager.refresh();
                            masterStat.reset();
                        }
                    } else {
                        logger.error("schedule manager for {} is null in ScanWorkerMasterTask ",entry.getKey());
                    }
                }
                masterStat.release();
            }

        }

        /**
         * 判定指定workerType的master 已经不在服务ID列表当中
         *
         * @param workerType
         * @return
         */
        private boolean isMasterStale(String workerType){
            String masterID = scheduleDataManager.getMaster(workerType);
            try {
                List<String> servers = scheduleDataManager.loadScheduleServerIds(workerType);
                if ( servers != null){
                    if ( !servers.contains(masterID)){
                        return true;
                    }
                } else {
                    //服务ID列表都没有值，masterID还存在，master结点上的ID肯定是过期ID
                    return true;
                }
            } catch (Exception e) {
                logger.info("can't get server id list.Optimistic about the master is to survive",e);
                //在取不到服务ID列表的情况下，乐观认为master结点正常
                return false;
            }
            return false;
        }
    }

    /**
     * 统计计算master的状态
     */
    class MasterStatistics{

        private List<Boolean> offline = new ArrayList<Boolean>();

        private ReentrantLock lock = new ReentrantLock();

        MasterStatistics() {
        }

        public void addOfflineStats(Boolean isOffline){
            try {
                lock.lock();
                offline.add(isOffline);
            } finally {
                lock.unlock();
            }
        }

        public synchronized int getScanTimes(){
            return offline.size();
        }

        public synchronized boolean isMasterStale(){
            if ( offline.size() == 0 ){
                return false;
            }
            boolean result = offline.get(0);
            for ( int i = 1; i < offline.size() ;i++ ){
                result = result && offline.get(i);
            }
            return result;
        }

        public synchronized void reset(){
            offline.clear();
        }


        /**
         * 释放掉一些已经没有参考价值的状态标志
         */
        public synchronized void release(){
            if ( offline != null ){
                int index = -1;
                //只保留最新连续状态为true的数据
                for ( int i = offline.size() - 1 ;i >=0 ;i-- ){
                    if ( offline.get(i) ){
                        index = i;
                        continue;
                    } else {
                        break;
                    }
                }
                if ( index > -1 ){
                    Boolean[] tmp = Arrays.copyOfRange(offline.toArray(new Boolean[offline.size()]), index, offline.size());
                    offline.clear();
                    offline.addAll(Arrays.asList(tmp));
                } else {
                    offline.clear();
                }
            }
        }
    }

}
