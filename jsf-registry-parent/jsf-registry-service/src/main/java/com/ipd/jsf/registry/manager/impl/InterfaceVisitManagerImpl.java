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

import com.ipd.jsf.registry.dao.InterfaceVisitDao;
import com.ipd.jsf.registry.domain.InterfaceVisit;
import com.ipd.jsf.registry.manager.InterfaceVisitManager;

@Service
public class InterfaceVisitManagerImpl implements InterfaceVisitManager {
    private static Logger logger = LoggerFactory.getLogger(InterfaceVisitManagerImpl.class);
    @Autowired
    private InterfaceVisitDao interfaceVisitDao;
    
    @Override
    public List<InterfaceVisit> getAllList() throws Exception {
        List<InterfaceVisit> resultList = new ArrayList<InterfaceVisit>();
        int limit = 5000;
        int start = 0;
        int totalCount = interfaceVisitDao.getListCount();
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
                    List<InterfaceVisit> temp = interfaceVisitDao.getList(start, limit);
                    logger.info("InterfaceVisit load elapse time:{}ms, startIndex:{}, limit:{}", (System.currentTimeMillis() - startTime), start, limit);
                    resultList.addAll(temp);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        
        
        return resultList;
    }

}
