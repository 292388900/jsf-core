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
package com.ipd.jsf.registry.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.registry.domain.AppIns;
import com.ipd.jsf.registry.manager.AppInsManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class AppInsCache {
    private static Logger logger = LoggerFactory.getLogger(AppInsCache.class);

    @Autowired
    private AppInsManager appInsManager;

    //key:appId-appInsId
    private ConcurrentHashMap<String, Integer> appInsMap = new ConcurrentHashMap<String, Integer>();

    private volatile boolean isSaveAppIns = false;

    @PostConstruct
    public void init() {
        try {
            Object objSaveIns = PropertyFactory.getProperty("deploy.app.saveins");
            isSaveAppIns = (objSaveIns == null) ? false : Boolean.parseBoolean(String.valueOf(objSaveIns));
            logger.info("register well save appins is : {}", objSaveIns);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 放入缓存中
     */
    public void refreshCache() {
        try {
            if(!isSaveAppIns){
                return;
            }
            List<AppIns> list = appInsManager.getList();
            if (list != null && !list.isEmpty()) {
                Map<String, Integer> tempMap = new HashMap<String, Integer>();
                for (AppIns appIns : list) {
                    tempMap.put(getKey(appIns.getAppId(), appIns.getAppInsId()), appIns.getJsfAppInsId());
                }
                if (!tempMap.isEmpty()) {
                    appInsMap.clear();
                    appInsMap.putAll(tempMap);
                }
            }
        } catch (Exception e) {
            logger.error("load appIns cache error:" + e.getMessage(), e);
        }
    }

    /**
     * 放入缓存中
     * @param appIns
     */
    public void put(AppIns appIns) {
        if (appIns != null && appIns.getAppId() != 0 && appIns.getAppInsId() != null && !appIns.getAppInsId().isEmpty()) {
            appInsMap.put(getKey(appIns.getAppId(), appIns.getAppInsId()), appIns.getJsfAppInsId());
        }
    }

    /**
     * 获取id信息
     * @param appId
     * @param appInsId
     * @return
     */
    public int getJsfAppInsId(int appId, String appInsId) {
        if (appId == 0 || appInsId == null) return 0;
        int result = 0;
        Integer temp = appInsMap.get(getKey(appId, appInsId));
        if (temp != null) {
            result = temp.intValue();
        }
        if (result == 0) {
            //如果为0，从数据库取下
            try {
                AppIns app = appInsManager.get(appId, appInsId);
                if (app != null) {
                    result = app.getJsfAppInsId();
                }
            } catch (Exception e) {
                logger.error("get appIns appId:" + appId + ", appInsId:" + appInsId + " error:" + e.getMessage(), e);
            }
        }
        return result;
    }

    /**
     * 生成key
     * @param appId
     * @param appInsId
     * @return
     */
    private String getKey(int appId, String appInsId) {
        return appId + "-" + appInsId;
    }

}
