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

import com.ipd.jsf.registry.dao.AppIfaceInvokeDao;
import com.ipd.jsf.registry.domain.AppIfaceInvoke;
import com.ipd.jsf.registry.manager.AppIfaceInvokeManager;

@Service
public class AppIfaceInvokeManagerImpl implements AppIfaceInvokeManager {
    private Logger logger = LoggerFactory.getLogger(AppIfaceInvokeManagerImpl.class);
    @Autowired
    private AppIfaceInvokeDao appIfaceInvokeDao;

    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.manager.AppIfaceInvokeManager#getList()
     */
    @Override
    public List<AppIfaceInvoke> getList() throws Exception {
        List<AppIfaceInvoke> list = new ArrayList<AppIfaceInvoke>();
        int limit = 5000;
        int start = 0;
        int totalCount = appIfaceInvokeDao.getListCount();
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
                    List<AppIfaceInvoke> temp = appIfaceInvokeDao.getList(start, limit);
                    logger.info("AppIfaceInvoke load app elapse time:{}ms, startIndex:{}, limit:{}, size:{}", (System.currentTimeMillis() - startTime), start, limit, temp.size());
                    list.addAll(temp);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return list;
    }

    @Override
    public List<String> getAppInvokeIfaceList() throws Exception {
        List<String> list = new ArrayList<String>();
        int limit = 5000;
        int start = 0;
        int totalCount = appIfaceInvokeDao.getIfaceListCount();
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
                    List<String> temp = appIfaceInvokeDao.getIfaceList(start, limit);
                    logger.info("AppIfaceInvoke load iface elapse time:{}ms, startIndex:{}, limit:{}, size:{}", (System.currentTimeMillis() - startTime), start, limit, temp.size());
                    list.addAll(temp);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return list;
    }

}
