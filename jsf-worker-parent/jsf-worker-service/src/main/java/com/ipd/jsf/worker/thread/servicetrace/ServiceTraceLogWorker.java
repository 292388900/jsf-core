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
package com.ipd.jsf.worker.thread.servicetrace;

import com.ipd.jsf.worker.thread.checkdb.CheckDBWorker;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.service.ServiceTraceLogService;

public class ServiceTraceLogWorker extends SingleWorker {
    private static final Logger logger = LoggerFactory.getLogger(ServiceTraceLogWorker.class);

    @Autowired
    private ServiceTraceLogService serviceTraceLogServiceImpl;

    @Override
    public boolean run() {
        if (!CheckDBWorker.isDBOK) {
            return true;
        }
        logger.info("servicetrace-log delete worker is starting ...");
        StopWatch clock = new StopWatch();
        try {
            clock.start();     // 计时开始
            serviceTraceLogServiceImpl.deleteByTime();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            clock.stop();      // 计时结束
        }
        logger.info("servicetrace-log delete worker is finished ...... it elapse time: " + clock.getTime() + " ms");
        return true;
    }

    @Override
    public String getWorkerType() {
        return "serviceTraceLogWorker";
    }

}
