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
package com.ipd.jsf.worker.thread.scanstatus;

import com.ipd.jsf.worker.service.ScanInsStatusService;
import com.ipd.jsf.worker.thread.checkdb.CheckDBWorker;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.common.SingleWorker;

/**
 * 实例、server、client状态扫描worker
 */
@Deprecated
public class ScanInsStatusWorker extends SingleWorker {
    private static final Logger logger = LoggerFactory.getLogger(ScanInsStatusWorker.class);
    @Autowired
    private ScanInsStatusService scanInsStatusService;

    private volatile boolean isRunning = false;

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
        logger.info("Scanning status of servers and clients...");
        StopWatch clock = new StopWatch();
        try {
            clock.start(); // 计时开始
            isRunning = true;
            scanInsStatusService.scanStatus();
        } catch (Exception e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行", e);
            return false;
        } catch (Throwable e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行(Throwable)", e);
            return false;
        } finally {
            isRunning = false;
            clock.stop(); // 计时结束
        }
        logger.info("The end of scanning status of servers and clients... it elapse time: " + clock.getTime() + " ms");
        return true;
    }

    @Override
    public String getWorkerType() {
        return "scanInsStatusWorker";
    }
}
