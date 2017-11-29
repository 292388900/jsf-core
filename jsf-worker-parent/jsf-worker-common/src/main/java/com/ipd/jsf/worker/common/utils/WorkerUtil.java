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

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ipd.jsf.worker.common.Constants;
import com.ipd.jsf.worker.common.ScheduleServer;
import com.ipd.jsf.worker.common.ScheduleServerInfo;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class WorkerUtil {

    private static final Logger logger = LoggerFactory.getLogger(WorkerUtil.class);

    //固定的master结点不容许contend
    public static final String FIXEDMASTER = "fixedMaster";

    //master server 结点id，容许被contended
    public static final String MASTERID = "masterID";


    /**
     * 获得master结点 server id
     *
     * @param workerType
     * @return
     */
    public static String getMasterID(ZkClient zkClient,String workerType){
        String zkPath = "/"+ Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        byte[] value = zkClient.readData(zkPath,true);
        if( value == null || value[0] == 0){
            logger.warn("{} master is null", zkPath);
            return null;
        }
        String master = new String(value);
        if ( StringUtils.isEmpty(master)){
            logger.warn("{} master is null", zkPath);
            return null;
        }
        JSONObject masterJSON = null;
        try {
            masterJSON = JSONObject.parseObject(master);
        } catch (JSONException e) {
            logger.error("#{}# error master server id for [{}} ",master,workerType,e);
            return null;
        }
        return StringUtils.isNotEmpty(masterJSON.getString(FIXEDMASTER)) ? masterJSON.getString(FIXEDMASTER):masterJSON.getString(MASTERID);
    }


    /**
     * 根据workerType以及serverID获取ScheduleServer对象
     *
     * @param zkClient
     * @param workerType
     * @param serverID
     * @return
     */
    public static ScheduleServer loadScheduleServer(ZkClient zkClient,String workerType,String serverID){
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+ workerType +"/"+Constants.SAF_WORKER_SERVER+"/"+serverID;
        byte[] value = zkClient.readData(zkPath,true);
        if ( value != null ){
            return JSONObject.toJavaObject(JSONObject.parseObject(new String(value)),ScheduleServer.class);
        } else {
            return null;
        }
    }


    /**
     * 更新schedule server
     *
     * @param zkClient
     * @param workerType
     * @param server
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static boolean updateScheduleServer(ZkClient zkClient,String workerType,ScheduleServer server)  {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+ workerType +"/"+Constants.SAF_WORKER_SERVER+"/"+server.getId();
        JSONObject serverJSON = (JSONObject) JSONObject.toJSON(server);
        byte[] serverValue = serverJSON.toJSONString().getBytes();
        try {
            zkClient.writeData(zkPath,serverValue);
        } catch (KeeperException e) {
            logger.error("update {} schedule server failed",server.getId(),e);
            return false;
        } catch (InterruptedException e) {
            logger.error("update {} schedule server failed",server.getId(),e);
            return false;
        }
        return true;

    }

    /**
     * 获取指定workerType下的ScheduleServer对象
     *
     * @param zkClient
     * @param workerType
     * @return
     * @throws Exception
     */
    public static List<ScheduleServer> loadScheduleServers(ZkClient zkClient,String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+ workerType +"/"+Constants.SAF_WORKER_SERVER;
        List<String> serverIDs = loadScheduleServerIds(zkClient, workerType);
        if ( serverIDs != null ){
            List<ScheduleServer> scheduleServers = new ArrayList<ScheduleServer>();
            for( String serverID : serverIDs ){
                byte[] value = zkClient.readData(zkPath+"/"+serverID,true);
                if ( value != null ){
                    ScheduleServer server = JSONObject.toJavaObject(JSONObject.parseObject(new String(value)),ScheduleServer.class);
                    scheduleServers.add(server);
                }
            }
            return scheduleServers;
        }
        return null;
    }

    public static List<ScheduleServer> loadRunningScheduleServers(ZkClient zkClient,String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+ workerType +"/"+Constants.SAF_WORKER_SERVER;
        List<String> serverIDs = loadScheduleServerIds(zkClient, workerType);
        if ( serverIDs != null ){
            List<ScheduleServer> scheduleServers = new ArrayList<ScheduleServer>();
            for( String serverID : serverIDs ){
                byte[] value = zkClient.readData(zkPath+"/"+serverID,true);
                if ( value != null ){
                    ScheduleServer server = JSONObject.toJavaObject(JSONObject.parseObject(new String(value)),ScheduleServer.class);
                    if ( server.isStart() ){
                        scheduleServers.add(server);
                    }
                }
            }
            return scheduleServers;
        }
        return null;
    }

    public static ScheduleServerInfo getScheduleServerInfo(ZkClient zkClient,String workerType,String serverID){
        ScheduleServer server = loadScheduleServer(zkClient,workerType,serverID);
        return getScheduleServerInfo(zkClient,server);
    }

    public static ScheduleServerInfo getScheduleServerInfo(ZkClient zkClient,ScheduleServer server){
        if ( server == null ){
            return null;
        }
        try {
            List<String> servers = loadScheduleServerIds(zkClient,server.getWorkerType());
            if ( servers != null ){
                Collections.sort(servers);
                return createScheduleServerInfo(zkClient,servers,server);
            }
        } catch (Exception e) {
            logger.error(" load {} server info failed",server.getId(),e);
            return null;
        }
        return null;
    }

    private static ScheduleServerInfo createScheduleServerInfo(ZkClient zkClient,List<String> serverIDs,ScheduleServer server){
        ScheduleServerInfo serverInfo = new ScheduleServerInfo();
        serverInfo.setServerNum(serverIDs.size());
        serverInfo.setWorkerType(server.getWorkerType());
        serverInfo.setMaster(isMaster(zkClient, server.getId(), server.getWorkerType()));
        serverInfo.setWorkerParameters(server.getWorkerParameters());
        serverInfo.setIp(server.getIp());
        serverInfo.setId(server.getId());
        for( int i = 0 ;i < serverIDs.size() ;i++ ){
            if ( serverIDs.get(i).equals(server.getId())){
                serverInfo.setIndex(i);
                return serverInfo;
            }
        }
        return null;
    }

    /**
     * 获取当前workerType 所有running状态的schedule server 信息

     * @param zkClient
     * @param workerType
     * @return
     */
    public static List<ScheduleServerInfo> getScheduleServerInfoList(ZkClient zkClient,String workerType){
        if ( workerType == null ){
            return null;
        }
        try {
            List<ScheduleServer> servers = loadRunningScheduleServers(zkClient, workerType);
            List<String> serverIDs = Lists.newArrayList(Lists.transform(servers,new Function<ScheduleServer, String>() {
                @Override
                public String apply(ScheduleServer scheduleServer) {
                    return scheduleServer.getId();
                }
            }));
            if ( servers != null ){
                List<ScheduleServerInfo> scheduleServerInfos = new ArrayList<ScheduleServerInfo>();
                Collections.sort(serverIDs);
                for(ScheduleServer server : servers ){
                    ScheduleServerInfo serverInfo = createScheduleServerInfo(zkClient,serverIDs,server);
                    if ( serverInfo != null ){
                        scheduleServerInfos.add(serverInfo);
                    }
                }
                return scheduleServerInfos;
            }
        } catch (Exception e) {
            logger.error(" load [{}] servers info failed",workerType,e);
            return null;
        }
        return null;
    }

    /**
     * load workerType下所有的服务结点ID
     *
     * @param zkClient
     * @param workerType
     * @return
     * @throws Exception
     */
    public static List<String> loadScheduleServerIds(ZkClient zkClient,String workerType) throws Exception {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+ workerType +"/"+Constants.SAF_WORKER_SERVER;
        return zkClient.getChildren(zkPath);
    }


    /**
     * 是否是master
     *
     * @param zkClient
     * @param serverID
     * @param workerType
     * @return
     */
    public static boolean isMaster(ZkClient zkClient,String serverID, String workerType) {
        if (StringUtils.isNotEmpty(workerType) && serverID.equals(getMasterID(zkClient,workerType))){
            return true;
        }
        return false;
    }

    /**
     * 获得master结点 server id
     *
     * @param workerType
     * @return
     */
    public static JSONObject getMasterServerID(ZkClient zkClient,String workerType,Stat stat){
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        byte[] value = null;
        try {
            value = zkClient.getZookeeper().getData(zkPath,false,stat);
        } catch (Exception e) {
            return null;
        }
        if( value == null ){
            return null;
        }
        return JSONObject.parseObject(new String(value));
    }


    public static boolean updateMasterServer(ZkClient zkClient,String workerType,JSONObject masterJSON) {
        String zkPath = "/"+Constants.SAF_WORKER_ROOT+"/"+workerType+"/"+Constants.SAF_WORKER_SERVER;
        String masterID = StringUtils.isNotEmpty(masterJSON.getString(FIXEDMASTER)) ? masterJSON.getString(FIXEDMASTER):masterJSON.getString(MASTERID);
        try {
            zkClient.writeData(zkPath, masterJSON.toJSONString().getBytes());
        } catch (KeeperException e) {
            logger.info("{} server contented master failed", masterID, e);
            return false;
        } catch (InterruptedException e) {
            logger.info("{} server contented master failed", masterID, e);
            return false;
        }
        logger.info("{} server contented master successfully", masterID);
        return true;
    }

}
