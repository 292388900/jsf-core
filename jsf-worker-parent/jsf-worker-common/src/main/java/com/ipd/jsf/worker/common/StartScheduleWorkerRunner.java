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

import com.ipd.jsf.worker.common.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class StartScheduleWorkerRunner implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(StartScheduleWorkerRunner.class);

    private ScheduleManager scheduleManager;

    private ScheduledExecutorService scheduledExecutorService;

    private String cronExp;

    private WorkerProcessor processor;

    private ScheduledFuture nextWorkerfuture;


    public StartScheduleWorkerRunner( ScheduleManager scheduleManager,ScheduledExecutorService scheduledExecutorService,String cronExp ,WorkerProcessor processor) {
        this.scheduleManager = scheduleManager;
        this.scheduledExecutorService = scheduledExecutorService;
        this.cronExp = cronExp;
        this.processor = processor;
    }

    @Override
    public void run() {
        try {
            if ( !processor.isProcessing() ){
                if ( nextWorkerfuture != null ){
                    nextWorkerfuture.cancel(true);
                }
                return;
            }
            //1、先执行完上次的worker 任务
            scheduleManager.start();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            CronExpression startExp = new CronExpression(cronExp);
            Date current = new Date();
            Date nextTime = startExp.getNextValidTimeAfter(current);
            logger.info(" worker [{}] will execute next time on {}",scheduleManager.getWorkerType(), DateUtil.format(nextTime, "yyyy-MM-dd HH:mm:ss"));
            //2、下次worker执行
            nextWorkerfuture =  this.scheduledExecutorService.schedule(
                    new StartScheduleWorkerRunner(scheduleManager, scheduledExecutorService, cronExp,processor), nextTime.getTime() - current.getTime(), TimeUnit.MILLISECONDS);

        } catch (Throwable ex) {
            logger.error("StartScheduleWorkerTask failed", ex);
        }
    }
}
