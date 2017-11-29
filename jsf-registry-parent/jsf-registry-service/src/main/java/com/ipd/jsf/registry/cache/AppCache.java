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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ipd.jsf.registry.domain.App;
import com.ipd.jsf.registry.manager.AppManager;

@Component
public class AppCache {
    private static Logger logger = LoggerFactory.getLogger(AppCache.class);

    @Autowired
    private AppManager appManager;

    private ConcurrentHashMap<Integer, App> appCache = new ConcurrentHashMap<Integer, App>();

    /**
     * 放入缓存中
     * @param list
     */
    public void refreshCache() {
        try {
            List<App> list = appManager.getList();
            if (list != null && !list.isEmpty()) {
                Map<Integer, App> tempMap = new HashMap<Integer, App>();
                for (App app : list) {
                    tempMap.put(app.getAppId(), app);
                }
                if (!tempMap.isEmpty()) {
                    appCache.clear();
                    appCache.putAll(tempMap);
                }
            }
        } catch (Exception e) {
            logger.error("load app cache error:" + e.getMessage(), e);
        }
    }

    public int getJsfAppId(int appId) {
        if (appId == 0) return 0;
        int jsfAppId = 0;
        App app = appCache.get(appId);
        if (app != null) {
            jsfAppId = app.getJsfAppId();
        }
        if (jsfAppId == 0) {
            try {
                app = appManager.get(appId);
                if (app != null) {
                    jsfAppId = app.getJsfAppId();
                }
            } catch (Exception e) {
                logger.error("get jsfappid error, appId:" + appId + ", error:" + e.getMessage(), e);
            }
        }
        return jsfAppId;
    }
}
