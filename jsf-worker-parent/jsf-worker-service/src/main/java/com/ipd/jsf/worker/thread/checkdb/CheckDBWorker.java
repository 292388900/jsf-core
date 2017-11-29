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
package com.ipd.jsf.worker.thread.checkdb;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.worker.manager.CheckDBConnManager;
import com.ipd.jsf.worker.util.WorkerUtil;

/**
 * 此方法不用继承SingleWorker
 */
public class CheckDBWorker {
    public static volatile boolean isDBOK = true;

    private static final Logger logger = LoggerFactory.getLogger(CheckDBWorker.class);

    private int interval = 15;  //15秒，需要设置小些

    private String umpDBKey = null;

    private String workerName = "";

    @Autowired
    private CheckDBConnManager checkDbConnManagerImpl;

    public void start() {
        schedule();
        logger.info("CheckDBWorker is running...");
    }

    /**
     * 定时测试数据库
     */
    private void schedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("checkdb"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    isDBOK = checkDbConnManagerImpl.check();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    isDBOK = false;
                } finally {
                    if (!isDBOK) {
                        logger.error("umpkey:" + umpDBKey + ", mysql连接测试失败,请检查mysql或网络连接,from " + workerName + ", ip:" + WorkerUtil.getWorkerIP());
                    }
                }
            }
        }, 5, interval, TimeUnit.SECONDS);
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * @return the umpDBKey
     */
    public String getUmpDBKey() {
        return umpDBKey;
    }

    /**
     * @param umpDBKey the umpDBKey to set
     */
    public void setUmpDBKey(String umpDBKey) {
        this.umpDBKey = umpDBKey;
    }

    /**
     * @return the workerName
     */
    public String getWorkerName() {
        return workerName;
    }

    /**
     * @param workerName the workerName to set
     */
    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

}
