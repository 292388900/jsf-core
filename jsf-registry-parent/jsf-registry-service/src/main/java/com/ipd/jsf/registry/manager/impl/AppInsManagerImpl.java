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

import com.ipd.jsf.registry.dao.AppInsDao;
import com.ipd.jsf.registry.domain.AppIns;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.manager.AppInsManager;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;

@Service
public class AppInsManagerImpl implements AppInsManager {
    private Logger logger = LoggerFactory.getLogger(AppInsManagerImpl.class);

    @Autowired
    private AppInsDao appInsDao;

    @Override
    public int create(AppIns appIns) throws Exception {
        if (appIns == null || appIns.getAppId() == 0 || appIns.getAppInsId() == null || appIns.getAppInsId().isEmpty()) {
            return 0;
        }
        return appInsDao.create(appIns);
    }

    @Override
    public int update(AppIns appIns) throws Exception {
        if (appIns == null || appIns.getAppId() == 0 || appIns.getAppInsId() == null || appIns.getAppInsId().isEmpty()) {
            return 0;
        }
        return appInsDao.update(appIns);
    }

    @Override
    public List<AppIns> getList() throws Exception {
        List<AppIns> list = new ArrayList<AppIns>();
        int limit = 5000;
        int start = 0;
        int totalCount = appInsDao.getListCount();
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
                    List<AppIns> temp = appInsDao.getList(start, limit);
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
    public AppIns get(int appId, String appInsId) throws Exception {
        return appInsDao.get(appId, appInsId);
    }

    @Override
    public AppIns convertAppInsFromJsfIns(JsfIns ins) {
        AppIns appIns = new AppIns();
        appIns.setInsKey(ins.getInsKey());
        appIns.setIp(ins.getIp());
        appIns.setAppId(ins.getAppId());
        appIns.setAppInsId(ins.getAppInsId());
        appIns.setCreateTime(ins.getCreateTime());
        appIns.setCreator(RegistryUtil.getRegistryIPPort());
        appIns.setUpdateTime(DateTimeZoneUtil.getTargetTime());
        appIns.setModifier(RegistryUtil.getRegistryIPPort());
        return appIns;
    }

}
