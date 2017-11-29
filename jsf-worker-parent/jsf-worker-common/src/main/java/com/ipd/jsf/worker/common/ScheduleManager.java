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

import com.ipd.jsf.worker.common.processor.DefaultWorkerProcessor;
import com.ipd.jsf.worker.common.utils.DateUtil;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.zookeeper.common.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * worker调度管理器
 * 一个workType(对应一个worker)对应一个worker调度管理器
 *
 */
public class ScheduleManager {


    private static final Logger logger = LoggerFactory.getLogger(ScheduleManager.class);


    private WorkerProcessor processor;

    private ScheduleDataManager scheduleDataManager;

    private WorkerManager workerManager;

    private String workerType;

    private ScheduledExecutorService scheduledExecutorService;


    private ReentrantLock lock = new ReentrantLock();

    /**
     * 当前调度服务器的信息
     */
    private ScheduleServer currentScheduleServer;

    public ScheduleManager(String workerType,ScheduleDataManager scheduleDataManager, WorkerManager workerManager) {
        this.scheduleDataManager = scheduleDataManager;
        this.workerManager = workerManager;
        this.workerType = workerType;
        activeScheduleManager();
    }


    /**
     * 初始化ScheduleManager
     *
     */
    public void activeScheduleManager(){
        Worker worker = workerManager.getWorker(workerType);
        this.currentScheduleServer = ScheduleServer.createScheduleServer(worker);
        try {
            workerManager.addScheduleManager(this.workerType, this);
            scheduleDataManager.registerScheduleServer(currentScheduleServer);
            logger.info("{} server registered for [{}]",currentScheduleServer.getId(),workerType);
        } catch (Exception e) {
            logger.error("{} server registered failed.", currentScheduleServer.getId(), e);
            return;
        }
        scheduledExecutorService = Executors.newScheduledThreadPool(2, new WorkerThreadFactory("scheduleManager"));
        processor = new DefaultWorkerProcessor(scheduleDataManager,worker ,currentScheduleServer,workerManager);
        workerScheduleStart();
    }

    /**
     * schedule start
     */
    public void workerScheduleStart(){
        if ( !currentScheduleServer.isStart() ){
            return;
        }
        processor.startProcess();
        boolean isRunNow = false;
        try {
            isRunNow = scheduleWorker();
        } catch (Exception e) {
            isRunNow = false;
            logger.error(" worker schedule start failed ",e);
        } finally {
        }
        if ( isRunNow ){
            this.start();
        }
    }


    /**
     * 调度worker
     *
     */
    public boolean scheduleWorker() throws Exception {
        Worker worker = workerManager.getWorker(workerType);
        if ( worker instanceof SingleWorker ){
            if ( !scheduleDataManager.isMaster(currentScheduleServer.getId(),workerType)){
                logger.info("{} current server is not master,so the [{}] worker will not be running on this server",currentScheduleServer.getId(),workerType);
                return false;
            }
        }
        String cronExp = worker.cronExpression();
        if ( !StringUtils.isEmpty( cronExp )){
            CronExpression startExp = new CronExpression(cronExp);
            Date current = new Date();
            Date firstStartTime = startExp.getNextValidTimeAfter(current);
            if ( firstStartTime == null ){
                logger.info(" worker [{}] will not be executed .the cronExp is :{}",worker.getWorkerType(),cronExp);
                return false;
            }
//            Date nextTime = startExp.getNextValidTimeAfter(firstStartTime);
           /* this.scheduledExecutorService.scheduleAtFixedRate(
                    new StartScheduleWorkerRunner(this,scheduledExecutorService,cronExp),firstStartTime.getTime() - current.getTime(),
                    nextTime.getTime() - firstStartTime.getTime(), TimeUnit.MILLISECONDS);*/
            logger.info(" worker [{}] will execute first time on {}",worker.getWorkerType(), DateUtil.format(firstStartTime, "yyyy-MM-dd HH:mm:ss"));
            this.scheduledExecutorService.schedule(
                    new StartScheduleWorkerRunner(this, scheduledExecutorService, cronExp, processor), firstStartTime.getTime() - current.getTime(), TimeUnit.MILLISECONDS);
            return false;
        } else {
            if ( worker instanceof EventWorker ){
                if (((EventWorker)worker).activeEvent()){
                    ((EventWorker)worker).offActive();
                    return true;
                } else {
                    return false;
                }
            } else {//没有时间表达式的容许执行一次
                return true;
            }
        }
    }


    public void refresh(){
        try {
            lock.lock();
            //当master发生变化时，即主从切换时，调度启动
            if ( processor != null ){
                Worker worker = workerManager.getWorker(workerType);
                if ( worker instanceof SingleWorker ){
                    workerScheduleStop(false);
                    workerScheduleStart();
                }
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 开始worker的任务处理
     */
    public void start(){
        try {
            if ( workerManager.getWorker(workerType) instanceof DistributeWorker){
                assignScheduleTask();
                createDistributeTask();
            }
        } catch (Exception e) {

        }
        processor.process();
    }

    /**
     * processor 恢复worker的处理
     *
     */
    public void resume(){
        processor.startProcess();
    }

    /**
     * 停止，不执行worker操作
     */
    public void workerScheduleStop(boolean isExecuteWorkerDestroy){
        scheduledExecutorService.shutdownNow();
        scheduledExecutorService = null;
        processor.stopProcess(isExecuteWorkerDestroy);
        scheduledExecutorService = Executors.newScheduledThreadPool(5, new WorkerThreadFactory("scheduleManager"));
    }

    public boolean isStop(){
        return !processor.isProcessing();
    }

    /**
     * 创建分布式任务
     */
    public void createDistributeTask(){
        Worker worker = workerManager.getWorker(workerType);
        if ( worker instanceof DistributeWorker ){
            try {
                List<String> tasks = scheduleDataManager.loadTasks(workerType);
                if ( tasks != null && tasks.size() > 0 ){
                    List<String> servers = scheduleDataManager.loadScheduleServerIds(workerType);
                    if ( servers != null && servers.size() > 0 && tasks.size() / servers.size() >= 10 ){
                        //生产的任务太多，则不在接着创建任务，在任务消费后，继续创建
                        logger.warn("[{}] task created to quickly ,but the worker consume little slowly .the cron expression is reasonable ?",workerType);
                        return;
                    }
                }
                //master 创建任务
                if(scheduleDataManager.isMaster(currentScheduleServer.getId(),worker.getWorkerType())){
                    logger.info("create tasks for [{}] ",workerType);
                    scheduleDataManager.createTask(worker.getWorkerType());
                } else {
                    logger.debug("slave can't create tasks for [{}] ", workerType);
                }
            } catch (Exception e) {
                logger.error("create distribute worker task for [{}] failed",worker.getWorkerType(),e);
                return;
            }
        }
    }

    /**
     * 分配未领取任务
     */
    public void assignScheduleTask() throws Exception {
        if(scheduleDataManager.isMaster(currentScheduleServer.getId(),workerType)){
            List<String> unCompletedTasks = scheduleDataManager.loadNotCompletedTasks(workerType);
            if ( unCompletedTasks == null || unCompletedTasks.size() == 0 ){
                return;
            }
            //所有workerType
//            List<String> serverIDs = scheduleDataManager.loadScheduleServerIds(workerType);
            List<ScheduleServer> runningServers = scheduleDataManager.loadRunningScheduleServers(workerType);
            if ( runningServers != null ){
                if ( unCompletedTasks.size() > runningServers.size() ){
                    int groupSize = unCompletedTasks.size() / runningServers.size();
                    for( int i = 1,j = 0 ;i <= runningServers.size() ;i++ ){
                        int remainder = 0;
                        if( i == runningServers.size()  ){
                            remainder = unCompletedTasks.size() % runningServers.size();
                        }
                        for( ;j< i * groupSize + remainder; j++){
                            String task = unCompletedTasks.get(j);
                            //newTask serverID 部分赋值为新的serverID
                            String newTask = task.replace(task.split("#")[0],runningServers.get(i-1).getId());
                            scheduleDataManager.createTask(workerType, newTask);
                            logger.info("assign [{}] task {} ----> {}", workerType, task, newTask);
                            scheduleDataManager.removeTask(workerType,task);
                        }
                    }
                } else {
                    for( int i = 0 ;i < unCompletedTasks.size() ;i++ ){
                        String task = unCompletedTasks.get(i);
                        //newTask serverID 部分赋值为新的serverID
                        String newTask = task.replace(task.split("#")[0],runningServers.get(i).getId());
                        scheduleDataManager.createTask(workerType,newTask);
                        logger.info("assign [{}] task {} ----> {}", workerType, task, newTask);
                        scheduleDataManager.removeTask(workerType,task);
                    }
                }
            }
        }

    }

    public ScheduleServer getCurrentScheduleServer() {
        return currentScheduleServer;
    }

    public void setCurrentScheduleServer(ScheduleServer currentScheduleServer) {
        this.currentScheduleServer = currentScheduleServer;
    }

    public String getWorkerType() {
        return workerType;
    }

}
