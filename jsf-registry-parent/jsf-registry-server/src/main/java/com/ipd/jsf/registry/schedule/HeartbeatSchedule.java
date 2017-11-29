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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.registry.manager.SysParamManager;
import com.ipd.jsf.registry.service.HeartbeatService;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

/**
 * 心跳检查
 */
public class HeartbeatSchedule {

    private static final Logger logger = LoggerFactory.getLogger(HeartbeatSchedule.class);

    private int taskHbInterval = HeartbeatConstants.REGISTRY_HEARTBEAT_PERIOD / 1000;  //需要设置小些，防止心跳队列积压过多的数据
    private volatile long saveHbInterval = 30000L;  //需要设置小些，防止心跳队列积压过多的数据
    private volatile long preExecuteTime = 0L;

    @Autowired
    private HeartbeatService heartbeatServiceImpl;

    @Autowired
    private SysParamManager sysParamManager;

    public void start() {
        saveHbSchedule();
        logger.info("heartbeatSchedule is running...");
    }

    /**
     * 定时将hb写入数据库
     */
    private void saveHbSchedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("hb"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                long currExecuteTime = System.currentTimeMillis();
                try {
                    String saveHbIntervalStr = sysParamManager.getByTypeAndKey("save.hb.interval", DataEnum.SysParamType.Worker.getValue());
                    if (saveHbIntervalStr != null && !saveHbIntervalStr.isEmpty()) {
                        saveHbInterval = Long.parseLong(saveHbIntervalStr) * 1000;
                    }
                } catch (Throwable e) {
                    logger.error("saveHbSchedule parse saveHbInterval error", e);
                }
                try {
                    if (currExecuteTime - preExecuteTime >= saveHbInterval) {
                        //写心跳到mysql
                        heartbeatServiceImpl.saveHb(RegistryUtil.getRegistryIPPort());
                        preExecuteTime = currExecuteTime;
                    }
                } catch (Throwable e) {
                    logger.error("saveHbSchedule error", e);
                }
            }
        }, 30, taskHbInterval, TimeUnit.SECONDS);
    }

}