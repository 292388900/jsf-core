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
package com.ipd.jsf.registry.schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.registry.manager.CheckDBConnManager;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 检查数据库是否连通
 */
public class CheckDBSchedule {

    private static final Logger logger = LoggerFactory.getLogger(CheckDBSchedule.class);

    private long interval = HeartbeatConstants.REG_HB_INTERVAL;

    private String umpDBKey = null;
    
    @Autowired
    private CheckDBConnManager checkDbConnManagerImpl;

    public void start() {
        schedule();
        logger.info("CheckDBSchedule is running...");
    }

    /**
     * 定时测试数据库
     */
    private void schedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("checkdb"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    RegistryUtil.isDBOK = checkDbConnManagerImpl.check();
                } catch (Exception e) {
                	RegistryUtil.isDBOK = false;
                    logger.error("CheckDBSchedule error", e);
                }
                if (!RegistryUtil.isDBOK) {
                    logger.error("umpkey:" + umpDBKey + ", mysql连接测试失败,请检查mysql或网络连接. from registry, ip:" + RegistryUtil.getRegistryIPPort());
                }
            }
        }, 5, interval, TimeUnit.SECONDS);
    }

    /**
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(long interval) {
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

}
