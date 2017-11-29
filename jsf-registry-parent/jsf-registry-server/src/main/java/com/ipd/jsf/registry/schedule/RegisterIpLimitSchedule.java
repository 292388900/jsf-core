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

import com.ipd.jsf.registry.service.RegisterIpLimitService;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

public class RegisterIpLimitSchedule {
    private static final Logger logger = LoggerFactory.getLogger(RegisterIpLimitSchedule.class);

    @Autowired
    private RegisterIpLimitService registerIpLimitServiceImpl;

    //同步间隔
    private int interval = 1;
    
    //设置是否需要ip数量访问限制
    private boolean checkflag = true;
    private int visitLimit = 100;
    private byte minuteLimit = 5;

    public void start() {
        registerIpLimitServiceImpl.init(checkflag, visitLimit, minuteLimit);
        schedule();
        logger.info("registerIpLimitSchedule is running...");
    }

    public void schedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ip-limit"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    long start = RegistryUtil.getSystemCurrentTime();
                    registerIpLimitServiceImpl.eraseVisitCount();
                    if (logger.isDebugEnabled()) {
                        logger.debug("erase ip visit count, elapse:{}ms", RegistryUtil.getSystemCurrentTime() - start);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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
     * @return the checkflag
     */
    public boolean isCheckflag() {
        return checkflag;
    }

    /**
     * @param checkflag the checkflag to set
     */
    public void setCheckflag(boolean checkflag) {
        this.checkflag = checkflag;
    }

    /**
     * @return the visitLimit
     */
    public int getVisitLimit() {
        return visitLimit;
    }

    /**
     * @param visitLimit the visitLimit to set
     */
    public void setVisitLimit(int visitLimit) {
        this.visitLimit = visitLimit;
    }

    /**
     * @return the minuteLimit
     */
    public byte getMinuteLimit() {
        return minuteLimit;
    }

    /**
     * @param minuteLimit the minuteLimit to set
     */
    public void setMinuteLimit(byte minuteLimit) {
        this.minuteLimit = minuteLimit;
    }

}
