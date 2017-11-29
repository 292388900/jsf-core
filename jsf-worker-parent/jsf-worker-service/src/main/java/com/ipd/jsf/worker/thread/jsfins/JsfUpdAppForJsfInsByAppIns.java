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
package com.ipd.jsf.worker.thread.jsfins;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.dao.AppInsDao;
import com.ipd.jsf.worker.domain.AppIns;
import com.ipd.jsf.worker.dao.JsfInsDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class JsfUpdAppForJsfInsByAppIns extends SingleWorker {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsfUpdAppForJsfInsByAppIns.class);

    @Autowired
    private JsfInsDao jsfInsDao;

    @Autowired
    private AppInsDao appInsDao;

    @Override
    public boolean run() {
        int jsfAppInsId = 0;
        int i = 0;
        while (true) {
            try {
                List<AppIns> appInses = appInsDao.getAppInsListNeedSync(jsfAppInsId, 1000);
                if (appInses.isEmpty()) {
                    LOGGER.info("JsfUpdAppForJsfInsByAppIns  all data end，jsfAppInsId：{}", jsfAppInsId);
                    break;
                }
                if(appInses.get(appInses.size() - 1).getJsfAppInsId() == jsfAppInsId){
                    i++;
                    if(i>2){
                        i = 0;
                        jsfAppInsId++;
                        continue;
                    }
                }
                for (AppIns appIns : appInses) {
                    try {
                        jsfInsDao.updInsAppIdAndAppInsId(appIns.getInsKey(), appIns.getAppId(), appIns.getAppInsId());
                    } catch (Exception e) {
                        LOGGER.error("When JsfUpdAppForJsfInsByAppIns upd jsfins is error:" + e.getMessage(), e);
                    }
                    jsfAppInsId = appIns.getJsfAppInsId();
                }
                LOGGER.info("JsfUpdAppForJsfInsByAppIns 100 date end，jsfAppInsId：{}", jsfAppInsId);
            } catch (Exception e) {
                LOGGER.error("When JsfUpdAppForJsfInsByAppIns do run is error:" + e.getMessage(), e);
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
        LOGGER.info("JsfUpdAppForJsfInsByAppIns worker end，jsfAppInsId：{}", jsfAppInsId);
        return true;
    }

    @Override
    public String getWorkerType() {
        return "jsfUpdAppForJsfInsByAppInsWorker";
    }
}
