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
package com.ipd.jsf.worker.saf1.manager.impl;

import com.ipd.jsf.worker.domain.Saf1InterfaceInfo;
import com.ipd.jsf.worker.saf1.manager.Saf1InterfaceInfoManager;
import com.ipd.jsf.worker.saf1dao.Saf1InterfaceInfoDao;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

public class Saf1InterfaceInfoManagerImpl implements Saf1InterfaceInfoManager {
    
    @Resource(name="saf1InterfaceInfoDao",type=Saf1InterfaceInfoDao.class)
    private Saf1InterfaceInfoDao saf1InterfaceInfoDao;

    @Override
    public Saf1InterfaceInfo getSaf1InterfaceByName(String interfaceName) throws Exception {
        return saf1InterfaceInfoDao.getByName(interfaceName);
    }

    @Override
    public List<Saf1InterfaceInfo> getSaf1InterfaceAfterTime(Date time) throws Exception {
        return saf1InterfaceInfoDao.getAfterTime(time);
    }
}
