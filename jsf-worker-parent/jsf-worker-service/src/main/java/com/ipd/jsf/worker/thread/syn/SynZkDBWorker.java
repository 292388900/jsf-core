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
package com.ipd.jsf.worker.thread.syn;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.common.PropertyFactory;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.service.DBSynToZkService;
import com.ipd.jsf.worker.service.ZkSynToDBService;
import com.ipd.jsf.worker.thread.checkdb.CheckDBWorker;
import com.ipd.jsf.worker.util.StringUtils;
import com.ipd.jsf.worker.util.WorkerAppConstant;

public class SynZkDBWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(SynZkDBWorker.class);

    private boolean isSynToDBProvider = false;

    private boolean isSynToDBConsumer = false;

    private boolean isSynToZkProvider = false;

    @Autowired
    private ZkSynToDBService zkSynToDBService;

    @Autowired
    private DBSynToZkService dbSynToZkHandler;

    private boolean isRunning = false;
    
    @Override
    public boolean run() {
        if (isRunning) {
            logger.info("ScanInsStatusWorker is running now, the task is over...");
            return true;
        }
        if (!CheckDBWorker.isDBOK) {
            logger.info("can not connet to mysql, the task is over...");
            return true;
        }
        logger.info("syning zk and db is starting...");
        StopWatch clock = new StopWatch();
        try {
            this.initData();//开关
            this.getSwitch();
            clock.start(); // 计时开始
            isRunning = true;
            try {
                if (isSynToDB()) {
                    zkSynToDBService.syn(isSynToDBProvider, isSynToDBConsumer);
                }
                if (isSynToZk()) {
                    dbSynToZkHandler.syn(isSynToZkProvider);
                }
            } catch (Throwable e) {
                logger.error("doSynWork 异常：" + e.getMessage(), e);
            }
            clock.stop(); // 计时结束
        } catch (Exception e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行", e);
        } finally {
            isRunning = false;
        }
        logger.info("The end of syning zk and db... it elapse time: " + clock.getTime() + " ms");
        return false;
    }

    /**
     * 获取times，减少consumer同步次数。每同步times次provider，只同步一次consumer
     */
    private void getSwitch() {
        try {
            isSynToDBProvider = getWorkerParameters().getBooleanValue("pTodb");
        } catch (Exception e) {
            logger.warn("get pTodb is error:{}, isSynToDBProvider value:{}", e.getMessage(), isSynToDBProvider);
        }
        try {
            isSynToDBConsumer = getWorkerParameters().getBooleanValue("cTodb");
        } catch (Exception e) {
            logger.warn("get cTodb is error:{}, isSynToDBConsumer value:{}", e.getMessage(), isSynToDBConsumer);
        }
        try {
            isSynToZkProvider = getWorkerParameters().getBooleanValue("pTozk");
        } catch (Exception e) {
            logger.warn("get pTozk is error:{}, isSynToZkProvider value:{}", e.getMessage(), isSynToZkProvider);
        }
    }

    public String getWorkerType() {
        return "synZkDBWorker";
    }

    private void initData() {
        isSynToDBProvider = isFunctionOpen(WorkerAppConstant.FUN_WORKER_SYN_DB_PROVIDER);
        isSynToDBConsumer = isFunctionOpen(WorkerAppConstant.FUN_WORKER_SYN_DB_CONSUMER);
        isSynToZkProvider = isFunctionOpen(WorkerAppConstant.FUN_WORKER_SYN_ZK_PROVIDER);
    }

    private boolean isSynToDB() {
        return isSynToDBProvider == true || isSynToDBConsumer == true;
    }

    private boolean isSynToZk() {
        return isSynToZkProvider == true;
    }

    public static boolean isFunctionOpen(String functionName){
		String config = (String)PropertyFactory.getProperty(functionName);
		if (!StringUtils.isBlank(config)) {
			return Boolean.valueOf(config);
		}else{
			throw new IllegalArgumentException("there is not exist function OC config like " + config + ",please make sure you config it in global.properties file");
		}
	}
}
