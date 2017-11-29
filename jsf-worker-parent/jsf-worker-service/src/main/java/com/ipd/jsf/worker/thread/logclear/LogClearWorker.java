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
package com.ipd.jsf.worker.thread.logclear;

import com.ipd.jsf.worker.service.*;
import com.ipd.jsf.gd.util.DateUtils;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.worker.dao.JsfCheckHistoryDao;
import com.ipd.jsf.worker.domain.JsfRegAddr;
import com.ipd.jsf.worker.manager.RegHbManager;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.thread.checkdb.CheckDBWorker;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LogClearWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(LogClearWorker.class);

    @Autowired
    private CallbackLogService callbackLogService;

    @Autowired
    private JsfRegReqService jsfRegReqService;

    @Autowired
    private JsfRegStatService jsfRegStatService;

    @Autowired
    private ServiceTraceLogService serviceTraceLogService;

    @Autowired
    private ScanStatusLogService scanStatusLogService;

    @Autowired
    private JsfCheckHistoryDao jsfCheckHistoryDao;

    @Autowired
    private RegHbManager regHbManager;

    @Autowired
    private JsfRegAddrService jsfRegAddrService;

    private ExecutorService delHBService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("Del-HB-Service"));

    /* (non-Javadoc)
     * @see Worker#run()
     */
    @Override
    public boolean run() {
        if (!CheckDBWorker.isDBOK) {
            return true;
        }
        logger.info("clear-log delete worker is starting ...");
        StopWatch clock = new StopWatch();
        try {
            clock.start();     // 计时开始
            callbackLogService.deleteByTime();
            jsfRegReqService.deleteByTime();
            jsfRegStatService.deleteByTime();
            serviceTraceLogService.deleteByTime();
            scanStatusLogService.deleteScanStatusLog();
            jsfCheckHistoryDao.deleteByTime(new Date(System.currentTimeMillis() - 7 * DateUtils.MILLISECONDS_PER_DAY));

            //删除注册中心心跳历史记录 24小时之前的数据
            List<JsfRegAddr> regAddrList = jsfRegAddrService.listAll();
            if (delHBService == null || delHBService.isShutdown()) {
                delHBService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new NamedThreadFactory("Del-HB-Service"));
            }
            if (regAddrList != null && !regAddrList.isEmpty()) {
                for (final JsfRegAddr jsfRegAddr : regAddrList) {
                    delHBService.submit(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                regHbManager.delHealthHistory(getIpPort(jsfRegAddr.getIp(), jsfRegAddr.getPort()), System.currentTimeMillis() - HeartbeatConstants.DEL_REG_HB_HISTORY_PERIOD);
                            } catch (Throwable e) {
                                logger.error("del reg health history data error : " + e.getMessage(), e);
                            }
                        }
                    });
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        clock.stop();      // 计时结束
        logger.info("clear-log delete worker is finished ...... it elapse time: " + clock.getTime() + " ms");
        return true;
    }

    /* (non-Javadoc)
     * @see Worker#getWorkerType()
     */
    @Override
    public String getWorkerType() {
        return "logClearWorker";
    }

    private String getIpPort(String ip, int port) {
        return ip + ":" + port;
    }

}
