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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.dao.InterfaceDataVersionDao;
import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ipd.jsf.version.common.service.AliasVersionService;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.util.ListPaging;

/**
 * 实例、server、client状态扫描worker
 */
@Component
public class AliasVersionUpdateWorker extends SingleWorker {
    private static final Logger logger = LoggerFactory.getLogger(AliasVersionUpdateWorker.class);
    @Autowired
    private InterfaceDataVersionDao interfaceDataVersionDao;

    @Autowired
    private AliasVersionService aliasVersionService;

    private Date lastScanDate = new Date(1L);

    @Override
    public boolean run() {
        logger.info("AliasVersionUpdateWorker starting...");
        StopWatch clock = new StopWatch();
        try {
            clock.start(); // 计时开始
            ListPaging<Integer> paging = new ListPaging<Integer>(interfaceDataVersionDao.getListByTime(lastScanDate), 50);
            List<Integer> subList = null;
            while (!(subList = paging.nextPageList()).isEmpty()) {
            	aliasVersionService.updateByInterfaceIdList(subList, new Date());
            }
            lastScanDate = new Date();
        } catch (Exception e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行", e);
        } finally {
            clock.stop(); // 计时结束
        }

        logger.info("The end of AliasVersionUpdateWorker... it elapse time: " + clock.getTime() + " ms");
        return true;
    }

    @Override
    public String getWorkerType() {
        return "aliasVersionUpdateWorker";
    }
}
