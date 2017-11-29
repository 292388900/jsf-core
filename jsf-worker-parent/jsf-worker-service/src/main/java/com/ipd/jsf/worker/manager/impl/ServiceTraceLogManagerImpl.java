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
package com.ipd.jsf.worker.manager.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.domain.ServiceTraceLog;
import com.ipd.jsf.worker.log.dao.ServiceTraceLogDao;
import com.ipd.jsf.worker.manager.ServiceTraceLogManager;

@Service
public class ServiceTraceLogManagerImpl implements ServiceTraceLogManager {

    @Autowired
    private ServiceTraceLogDao serviceTraceLogDao;

    /* (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServiceTraceLogManager#create(java.util.List)
     */
    @Override
    public void create(List<ServiceTraceLog> list) throws Exception {
        serviceTraceLogDao.create(list);
    }

    @Override
    public List<ServiceTraceLog> getListByTime(String interfaceName, int pcType, Date startTime, Date endTime) throws Exception {
        return serviceTraceLogDao.getListByTime(interfaceName, pcType, startTime, endTime);
    }

    @Override
    public int deleteByTime(Date time) throws Exception {
        return serviceTraceLogDao.deleteByTime(time);
    }
}
