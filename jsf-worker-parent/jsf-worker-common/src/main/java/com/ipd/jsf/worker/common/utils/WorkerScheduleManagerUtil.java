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
package com.ipd.jsf.worker.common.utils;

import com.alibaba.fastjson.JSONObject;
import com.ipd.jsf.worker.common.ScheduleServer;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * worker 调度管理工具类
 * 便于管理端对worker进行管理
 *
 */
public class WorkerScheduleManagerUtil {

    private static final Logger logger = LoggerFactory.getLogger(WorkerScheduleManagerUtil.class);



    public static String getMasterID(String workerType){
        return WorkerUtil.getMasterID(ZkClientUtil.getZkClient(), workerType);
    }

    /**
     * 是否是master
     *
     * @param serverID
     * @param workerType
     * @return
     */
    public static boolean isMaster(String serverID, String workerType) {
        if (StringUtils.isNotEmpty(workerType) && serverID.equals(getMasterID(workerType))){
            return true;
        }
        return false;
    }

    /**
     * 启动worker的调度执行
     *
     * @param workerType
     */
    public static void startWorker(String workerType) throws Exception {
        try {
            List<ScheduleServer> servers = WorkerUtil.loadScheduleServers(ZkClientUtil.getZkClient(),workerType);
            if ( servers != null ){
                for(ScheduleServer server : servers ){
                    server.setStart(true);
                    if ( !WorkerUtil.updateScheduleServer(ZkClientUtil.getZkClient(),workerType,server) ){
                        logger.error(" start {} schedule server for [{}] failed",server.getId(),workerType);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("start [{}] worker failed ",workerType,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 停止worker的执行
     *
     * @param workerType
     */
    public static void stopWorker(String workerType ) throws Exception {
        try {
            List<ScheduleServer> servers = WorkerUtil.loadScheduleServers(ZkClientUtil.getZkClient(),workerType);
            if ( servers != null ){
                for(ScheduleServer server : servers ){
                    server.setStart(false);
                    if ( !WorkerUtil.updateScheduleServer(ZkClientUtil.getZkClient(),workerType,server) ){
                        logger.error(" workerScheduleStop {} schedule server for [{}] failed",server.getId(),workerType);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("workerScheduleStop [{}] worker failed ",workerType,e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 启动指定server
     *
     * @param workerType
     * @param serverID
     */
    public static void startWorker(String workerType,String serverID){
        try {
            List<ScheduleServer> servers = WorkerUtil.loadScheduleServers(ZkClientUtil.getZkClient(),workerType);
            if ( servers != null ){
                for(ScheduleServer server : servers ){
                    if ( server.getId().equals(serverID)){
                        server.setStart(true);
                        if ( !WorkerUtil.updateScheduleServer(ZkClientUtil.getZkClient(),workerType,server) ){
                            logger.error(" start {} schedule server for [{}] failed",server.getId(),workerType);
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("start [{}] worker on {} server failed ",workerType,serverID,e);
        }
    }

    /**
     * 停止指定serverID的server
     *
     * @param workerType
     * @param serverID
     */
    public static void stopWorker(String workerType,String serverID ){
        try {
            List<ScheduleServer> servers = WorkerUtil.loadScheduleServers(ZkClientUtil.getZkClient(),workerType);
            if ( servers != null ){
                for(ScheduleServer server : servers ){
                    if ( server.getId().equals(serverID)){
                        server.setStart(false);
                        if ( !WorkerUtil.updateScheduleServer(ZkClientUtil.getZkClient(),workerType,server) ){
                            logger.error(" stop {} schedule server for [{}] failed",server.getId(),workerType);
                        }
                        return;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("stop [{}] worker on {} server failed ",workerType,serverID,e);
        }
    }


    /**
     * worker 是否在运行
     *
     * @param workerType
     * @param workerKind
     * @return
     */
    public static boolean isRunning(String workerType ,String workerKind){
        if ( "single".equals(workerKind)){
            String masterServerID = WorkerUtil.getMasterID(ZkClientUtil.getZkClient(),workerType);
            if (StringUtils.isNotEmpty(masterServerID)){
                ScheduleServer masterServer = WorkerUtil.loadScheduleServer(ZkClientUtil.getZkClient(),workerType,masterServerID);
                if ( masterServer != null ){
                    return masterServer.isStart();
                } else {
                    logger.error(" load {} master server error for [{}] in isRunning.return false",masterServerID,workerType);
                    return false;
                }
            } else {
                logger.error(" get [{}] master server id is empty in isRunning.return false",workerType);
                return false;
            }
        } else if ("distribute".equals(workerKind)){
            List<ScheduleServer> servers = listWorkerScheduleServers(workerType);
            if ( servers == null ){
                return false;
            } else {
                for( ScheduleServer server : servers ){
                    //分布式worker只要一个实例在运行则返回true
                    if ( server.isStart() ){
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 指定workerType下面所有的schedule server实例
     *
     * @param workerType
     * @return
     */
    public static List<ScheduleServer> listWorkerScheduleServers(String workerType){
        try {
            return WorkerUtil.loadScheduleServers(ZkClientUtil.getZkClient(),workerType);
        } catch (Exception e) {
            logger.error("list [{}] worker failed ",workerType,e);
            return null;
        }
    }


    /**
     * 设置为固定的master结点
     * @param workerType
     * @param serverID
     * @return
     */
    public static boolean fixMaster(String workerType,String serverID) {
        ZkClient zkClient = ZkClientUtil.getZkClient();
        JSONObject masterJSON = WorkerUtil.getMasterServerID(zkClient,workerType,null);
        masterJSON.put(WorkerUtil.FIXEDMASTER, serverID);
        return WorkerUtil.updateMasterServer(zkClient,workerType, masterJSON);
    }


    /**
     * 取消为固定的master结点
     * @param workerType
     * @return
     */
    public static boolean cancelfixMaster(String workerType) {
        ZkClient zkClient = ZkClientUtil.getZkClient();
        JSONObject masterJSON = WorkerUtil.getMasterServerID(zkClient,workerType,null);
        masterJSON.put(WorkerUtil.FIXEDMASTER, "");
        return WorkerUtil.updateMasterServer(zkClient,workerType, masterJSON);
    }


}
