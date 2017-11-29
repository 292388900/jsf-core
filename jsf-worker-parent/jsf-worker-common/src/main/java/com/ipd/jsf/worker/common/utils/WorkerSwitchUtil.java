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
import com.ipd.jsf.worker.common.Constants;
import com.ipd.jsf.worker.common.ScheduleServerInfo;
import com.ipd.jsf.zookeeper.IZkDataListener;
import com.ipd.jsf.zookeeper.ZkClient;
import com.ipd.jsf.zookeeper.common.StringUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class WorkerSwitchUtil {

    private static final Logger logger = LoggerFactory.getLogger(WorkerSwitchUtil.class);

    private static ZkClient zkClient;

    private static Properties zkConfig;

    static {
        try {
            zkConfig = new Properties();
            zkConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("worker.properties"));
            zkClient = new ZkClient(zkConfig.getProperty("zk.address"),Long.valueOf(zkConfig.getProperty("zk.connectionTimeout","10000")),
                    Integer.valueOf(zkConfig.getProperty("zk.sessionTimeout","30000")));
        } catch (IOException e) {
            logger.error("create zkClient error",e);
        }
    }

    /**
     * 获得指定workerType的master server的相关信息
     * <br/>
     * 在主从发生切换时，回调方法被执行
     *
     * @param workerType workerType
     * @param callback 当检测到主从发生切换后的回调
     * @return
     */
    public static ScheduleServerInfo getWorkerMasterServerInfo(final String workerType,final MasterSwitchCallback callback){

        String zkPath = "/" + Constants.SAF_WORKER_ROOT + "/" + workerType + "/" + Constants.SAF_WORKER_SERVER;
        if (callback != null) {
            //master 切换监听
            zkClient.subscribeDataChanges(zkPath,new IZkDataListener() {
                @Override
                public void handleDataChange(String dataPath, Object data) throws Exception {
                    String masterJSONStr = new String((byte[]) data);
                    JSONObject masterJSON = null;
                    try {
                        masterJSON = JSONObject.parseObject(masterJSONStr);
                    } catch (JSONException e) {
                        logger.error("#{}# error master server id ",masterJSONStr,e);
                    }
                    String masterID = StringUtils.isNotEmpty(masterJSON.getString(WorkerUtil.FIXEDMASTER)) ? masterJSON.getString(WorkerUtil.FIXEDMASTER):masterJSON.getString(WorkerUtil.MASTERID);
                    ScheduleServerInfo newestServerInfo = WorkerUtil.getScheduleServerInfo(zkClient, workerType, masterID);
                    callback.execute(newestServerInfo);
                }
                @Override
                public void handleDataDeleted(String dataPath) throws Exception {
                }
            });
        }
        try {
            return WorkerUtil.getScheduleServerInfo(zkClient, workerType, WorkerUtil.getMasterID(zkClient, workerType));
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            return null;
        }
    }

    /**
     * 主从切换后回调方法
     */
    public static interface MasterSwitchCallback{

        void execute(ScheduleServerInfo newestServerInfo);

    }




}
