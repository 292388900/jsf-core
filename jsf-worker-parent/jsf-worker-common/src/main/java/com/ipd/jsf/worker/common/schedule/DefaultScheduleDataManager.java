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
package com.ipd.jsf.worker.common.schedule;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ipd.jsf.worker.common.*;
import com.ipd.jsf.worker.common.utils.WorkerUtil;
import com.ipd.jsf.zookeeper.IZkDataListener;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class DefaultScheduleDataManager implements ScheduleDataManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private static ZkClient zkClient;

    private WorkerManager workerManager;


    public DefaultScheduleDataManager() {
    }

    public DefaultScheduleDataManager(ZkClient zkClient,WorkerManager workerManager) {
        this.zkClient = zkClient;
        this.workerManager = workerManager;
    }

    @Override
    public void registerScheduleServer(final ScheduleServer server) throws Exception {
        String zkPath = "/"+ Constants.SAF_WORKER_ROOT+"/"+server.getWorkerType()+"/"+Constants.SAF_WORKER_SERVER+"/"+server.getId();
        server.setRegister(true);
        JSONObject serverJSON = (JSONObject) JSONObject.toJSON(server);
        byte[] serverValue = serverJSON.toJSONString().getBytes();
        zkClient.createEphemeral(zkPath, serverValue);
        //schedule server 状态监听（便于管理端对worker进行管理）
        zkClient.subscribeDataChanges(zkPath,new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                ScheduleManager scheduleManager = workerManager.getScheduleManagers().get(server.getWorkerType());
                ScheduleServer newestServer = loadScheduleServer(server.getWorkerType(),server.getId());
                scheduleManager.setCurrentScheduleServer(newestServer);
                scheduleManager.workerScheduleStop(true);
                if( newestServer.isStart() ){
                    //加载worker定义最新信息
                    workerManager.loadNewestWorkerInfo(workerManager.getWorker(server.getWorkerType()));
                    scheduleManager.workerScheduleStart();
                }
                logger.info("[{}] schedule server {} stat changed to [{}]",server.getWorkerType(),server.getId(),newestServer.isStart());
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
            }
        });
    }

    @Override
    public List<String> loadScheduleServerIds(String workerType) throws Exception {
        return WorkerUtil.loadScheduleServerIds(zkClient, workerType);
    }

    @Override
    public List<String> loadRunningScheduleServerIds(String workerType) throws Exception {
        List<ScheduleServer> runningServers = loadRunningScheduleServers(workerType);
        if ( runningServers == null ){
            return null;
        }
       return Lists.transform(runningServers,new Function<ScheduleServer, String>() {
            @Override
            public String apply(ScheduleServer input) {
                return input.getId();
            }
        });
    }

    @Override
    public List<ScheduleServer> loadScheduleServers(String workerType) throws Exception {
        return WorkerUtil.loadScheduleServers(zkClient,workerType);
    }

    @Override
    public List<ScheduleServer> loadRunningScheduleServers(String workerType) throws Exception {
        return WorkerUtil.loadRunningScheduleServers(zkClient,workerType);
    }

    @Override
    public boolean contendMaster(String workerType,ScheduleServer currentServer){
        String masterServerID = WorkerUtil.getMasterID(zkClient, workerType);
        String candidateMasterID;
        List<ScheduleServer> candidateServers = null;
        try {
            candidateServers = loadScheduleServers(workerType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if( masterServerID == null ){
            if ( candidateServers != null && candidateServers.size() > 0 ){
                candidateMasterID = candidateServers.get(0).getId();
                if ( currentServer.getId().equals(candidateMasterID)){
                    JSONObject candidateMasterJSON = new JSONObject();
                    candidateMasterJSON.put(WorkerUtil.MASTERID, candidateMasterID);
                    return updateMasterServer(workerType, candidateMasterJSON);
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER+"/"+masterServerID;
            try {
                if( zkClient.exists( zkPath)){
                    logger.info("{} for {} master is alive.contend didn't happen",masterServerID,workerType  );
                    return false;//master is alive
                } else {
                    Stat stat = new Stat();
                    JSONObject masterJSON = getMasterServerID(workerType,stat);
                    //fixed master 不容许争夺
                    /*if (masterJSON != null && StringUtils.isNotEmpty( masterJSON.getString(FIXEDMASTER))){
                        logger.info("{} is fixed master which can't be contended .so current server {} is slave ",masterJSON.getString(FIXEDMASTER),currentServer.getId());
                        return false;
                    }*/
                    if ( candidateServers != null && candidateServers.size() > 0 ){
                        List<String> ids = new ArrayList<String>();
                        for (ScheduleServer candidateServer : candidateServers ){
                            candidateMasterID = candidateServer.getId();
                            ids.add(candidateMasterID);
                            //当前server参与竞争
                            if ( candidateMasterID.equals(currentServer.getId())){
                                return contend(workerType, candidateMasterID, stat.getVersion());
                            }
                        }
                        logger.warn("{} current server for {} is not in server list {}", currentServer.getId(), workerType, StringUtils.join(ids, ","));
                    } else {
                        return false;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void initialData(String workerType,boolean isNeeded) {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        String taskPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        try {
            zkClient.createPersistent(zkPath,true);
            if ( isNeeded ){
                zkClient.createPersistent(taskPath,true);
            }
        } catch (Exception e) {
            logger.error("create {} path error",zkPath);
        }
    }


    @Override
    public String getMaster(String workerType) {
       return WorkerUtil.getMasterID(zkClient, workerType);
    }

    @Override
    public boolean isMaster(String serverID, String workerType) {
        if (StringUtils.isNotEmpty(workerType) && serverID.equals(getMaster(workerType))){
            return true;
        }
        return false;
    }

    @Override
    public ScheduleServerInfo loadScheduleServerInfo(ScheduleServer server) {
        return WorkerUtil.getScheduleServerInfo(zkClient, server);
    }

    /**
     * 设置为固定的master结点，其他结点不能争夺
     * @param workerType
     * @param serverID
     * @return
     */
    @Override
    public boolean fixMaster(String workerType,String serverID) {
        JSONObject masterJSON = getMasterServerID(workerType,null);
        masterJSON.put(WorkerUtil.FIXEDMASTER, serverID);
        return updateMasterServer(workerType, masterJSON);
    }

    @Override
    public void saveWorkerParameters(ScheduleServer currentServer, JSONObject parameters) {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+currentServer.getWorkerType()+"/"+Constants.SAF_WORKER_SERVER+"/"+currentServer.getId();
        JSONObject serverJSON = (JSONObject) JSONObject.toJSON(currentServer);
        byte[] serverValue = serverJSON.toJSONString().getBytes();
        try {
            zkClient.writeData(zkPath,serverValue);
        } catch (Exception e) {
            logger.error(" save [{}] parameters on server {} failed",currentServer.getWorkerType(),currentServer.getId(),e);
        }
    }

    @Override
    public JSONObject getWorkerParameters(String workerType,String serverID) {
        ScheduleServer scheduleServer = loadScheduleServer(workerType,serverID);
        if ( scheduleServer != null ){
            return scheduleServer.getWorkerParameters();
        }
        return null;
    }

    @Override
    public void createTask(String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        List<ScheduleServerInfo> currentScheduleClusterInfos = WorkerUtil.getScheduleServerInfoList(zkClient, workerType);
        if ( currentScheduleClusterInfos != null ){
            for(ScheduleServerInfo serverInfo : currentScheduleClusterInfos ){
                String task = serverInfo.getId()+"#"+serverInfo.getServerNum()+"#"+serverInfo.getIndex();
                String path = zkPath+"/"+task+"#";
                zkClient.createPersistentSequential(path, TaskStat.CREATE.toString().getBytes());
            }
        }
    }

    @Override
    public void createTask(String workerType, String task) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        zkClient.createPersistent(zkPath + "/" + task, TaskStat.CREATE.toString().getBytes());
    }

    @Override
    public void createTask(String workerType, List<String> excludeServerIDs) throws Exception {
        if ( excludeServerIDs == null ){
            return;
        }
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        List<ScheduleServerInfo> currentScheduleClusterInfos = WorkerUtil.getScheduleServerInfoList(zkClient, workerType);
        if ( currentScheduleClusterInfos != null ){
            for(ScheduleServerInfo serverInfo : currentScheduleClusterInfos ){
                if ( !excludeServerIDs.contains(serverInfo.getId())){
                    String task = serverInfo.getId()+"#"+serverInfo.getServerNum()+"#"+serverInfo.getIndex();
                    String path = zkPath+"/"+task;
                    zkClient.createPersistent(path,TaskStat.CREATE.toString().getBytes());
                }
            }
        }
    }

    @Override
    public void removeTask(String workerType, String task) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK+"/"+task;
        zkClient.delete(zkPath);
    }

    @Override
    public void updateTaskStat(String workerType,String task,TaskStat stat,Stat expectedTaskVersion) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK+"/"+task;
        zkClient.writeData(zkPath, stat.toString().getBytes(),expectedTaskVersion.getVersion());
    }

    @Override
    public List<String> loadCreatedTasks(String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        List<String> tasks = zkClient.getChildren(zkPath,false);
        List<String> createdTasks = new ArrayList<String>();
        if ( tasks != null ){
            for( String task : tasks){
                byte[] v = null;
                try {
                    if ( !zkClient.exists(zkPath+"/"+task)){
                        continue;
                    }
                     v = zkClient.readData(zkPath+"/"+task,true);
                } catch (Exception e) {
                    //task maybe complete
                }
                if ( v != null ){
                    TaskStat stat = TaskStat.getTaskStat(new String(v));
                    if ( TaskStat.CREATE == stat ){
                        createdTasks.add(task);
                    }
                }
            }

        }
        return createdTasks;
    }


    @Override
    public List<String> loadTasks(String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        if ( !zkClient.exists(zkPath) ){
            return null;
        }
        return zkClient.getChildren(zkPath,false);
    }

    @Override
    public List<String> loadNotCompletedTasks(String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        if ( !zkClient.exists(zkPath) ){
            return null;
        }
        List<String> tasks = zkClient.getChildren(zkPath, false);
        List<String> createdTasks = new ArrayList<String>();
        List<String> serverIDs = loadRunningScheduleServerIds(workerType);
        if ( serverIDs == null ){
            return tasks;
        }
        if ( tasks != null ){
            for( String task : tasks){
                if (!serverIDs.contains(task.split("#")[0])){
                        createdTasks.add(task);
                }
            }
        }
        return createdTasks;
    }

    /**
     * 用数据版本来避免并发获取task冲突的问题
     * @param workerType
     * @param serverID
     * @return
     * @throws Exception
     */
    @Override
    public  String  getTask(String workerType, String serverID) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_TASK;
        List<String> tasks = loadCreatedTasks(workerType);
        if ( tasks != null ){
            Stat taskStat = new Stat();
            for( String task : tasks ){
                if ( task.indexOf(serverID) > -1 ){
                    try {
                        byte[] bytes = zkClient.getZookeeper().getData(zkPath+"/"+task,false,taskStat);
                        if ( TaskStat.CREATE == TaskStat.getTaskStat(new String(bytes))){
                            updateTaskStat(workerType, task, TaskStat.ASSIGNED,taskStat);
                            return task;
                        } else {
                            continue;
                        }
                    } catch (Exception e) {
                        // 获取task 竞争失败 continue get task
                        logger.error("竞争task失败",e);
                    }
                }
            }
        }
        return null;
    }

    private ScheduleServer loadScheduleServer(String workerType,String serverID){
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER+"/"+serverID;
        byte[] value = zkClient.readData(zkPath, true);
        if ( value == null ){
            return null;
        }
        return JSONObject.toJavaObject(JSONObject.parseObject(new String(value)),ScheduleServer.class);
    }

    /**
     * 获得master结点 server id
     *
     * @param workerType
     * @return
     */
    private JSONObject getMasterServerID(String workerType,Stat stat){
        return WorkerUtil.getMasterServerID(zkClient,workerType,stat);
    }

    private boolean updateMasterServer(String workerType,JSONObject masterJSON) {
        return WorkerUtil.updateMasterServer(zkClient,workerType,masterJSON);
    }


    private boolean contend(String workerType,String serverID,int expectedVersion){
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        try {
            JSONObject masterJSON = new JSONObject();
            masterJSON.put(WorkerUtil.MASTERID,serverID);
            zkClient.writeData(zkPath,masterJSON.toJSONString().getBytes(),expectedVersion);
        } catch (KeeperException e) {
            logger.info("{} server contended master for [{}] failed",serverID,workerType,e);
            return false;
        } catch (InterruptedException e) {
            logger.info("{} server contended master for [{}] failed",serverID,workerType,e);
            return false;
        }
        logger.info("{} server contended [{}] master successfully", serverID,workerType);
        return true;
    }
}
