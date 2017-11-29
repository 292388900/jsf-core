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
package com.ipd.jsf.worker.service.impl;

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.JsfIns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.manager.InstanceManager;
import com.ipd.jsf.worker.manager.ScanStatusLogManager;
import com.ipd.jsf.worker.service.ScanStatusLogService;

@Service
public class ScanStatusLogServiceImpl implements ScanStatusLogService {
    private static final Logger logger = LoggerFactory.getLogger(ScanStatusLogServiceImpl.class);
    //删除server和实例的日志信息保留30天
    private final long bakLogInterval = 30 * 24 * 3600000L;
    //标记为删除状态的实例信息保留7天
    private final long delInsInterval = 7 * 24 * 3600000L;

    @Autowired
    private ScanStatusLogManager scanStatusLogManager;

    @Autowired
    private InstanceManager instanceManager;

    /* (non-Javadoc)
     * @see ScanStatusLogService#syncToHistory()
     */
    @Override
    public void deleteScanStatusLog() throws Exception {
        long start = System.currentTimeMillis();
        //获取30天以前的时间
        Date time = new Date(System.currentTimeMillis() - bakLogInterval);
        try {
            //将30天以前的日志删除
            int result = scanStatusLogManager.batchDelete(time);
            logger.info("delete scanstatus Log  size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void deleteInstance() throws Exception {
        long start = System.currentTimeMillis();
        Date time = new Date(System.currentTimeMillis() - delInsInterval);
        try {
            int result = 0;
            //获取7天以前删除标志的实例
            while (true) {
                //批量删除，每次先取100条，然后删除，并记录日志
                List<JsfIns> deleteInsList = instanceManager.getDelYnInsBeforeTime(time);
                if (deleteInsList != null && !deleteInsList.isEmpty()) {
                    //批量删除
                    instanceManager.batchDeleteByInsKey(deleteInsList);
                    result += deleteInsList.size();
                } else {
                    break;
                }
            }
            logger.info("delete instance  size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
       
    }

}
