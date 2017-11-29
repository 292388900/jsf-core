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
package com.ipd.jsf.registry.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.dao.AppDao;
import com.ipd.jsf.registry.domain.App;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.manager.AppManager;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;

@Service
public class AppManagerImpl implements AppManager {
    private static Logger logger = LoggerFactory.getLogger(AppManagerImpl.class);

    @Autowired
    private AppDao appDao;

    @Override
    public int create(App app) throws Exception {
        if (app == null || app.getAppId() == 0 || app.getAppName() == null || app.getAppName().isEmpty()) {
            return 0;
        }
        return appDao.create(app);
    }

    @Override
    public int update(App app) throws Exception {
        if (app == null || app.getAppId() == 0 || app.getAppName() == null || app.getAppName().isEmpty()) {
            return 0;
        }
        return appDao.update(app);
    }

    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.manager.AppManager#get(int)
     */
    @Override
    public App get(int appId) throws Exception {
        return appDao.get(appId);
    }

    @Override
    public List<App> getList() throws Exception {
        List<App> list = new ArrayList<App>();
        int limit = 5000;
        int start = 0;
        int totalCount = appDao.getListCount();
        if (totalCount > 0) {
            int page = 0;
            int totalPage = 0;
            if (totalCount % limit == 0) {
                totalPage = totalCount / limit;
            } else {
                totalPage = totalCount / limit + 1;
            }
            while (page < totalPage) {
                start = page * limit;
                page ++;
                try {
                    long startTime = System.currentTimeMillis();
                    List<App> temp = appDao.getList(start, limit);
                    logger.info("elapse time:{}ms, startIndex:{}, limit:{}", (System.currentTimeMillis() - startTime), start, limit);
                    list.addAll(temp);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return list;
    }

    @Override
    public App getAppFromJsfIns(JsfIns ins) {
        if (ins != null) {
            App app = new App();
            app.setAppId(ins.getAppId());
            app.setAppName(ins.getAppName());
            app.setCreateTime(DateTimeZoneUtil.getTargetTime());
            app.setCreator(RegistryUtil.getRegistryIPPort());
            return app;
        }
        return null;
    }

}
