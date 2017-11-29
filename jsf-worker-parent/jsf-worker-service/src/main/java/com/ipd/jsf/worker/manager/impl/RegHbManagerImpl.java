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

import java.util.List;

import com.ipd.jsf.worker.manager.RegHbManager;
import com.ipd.jsf.worker.domain.RegHealthInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.dao.RegHbDao;

@Service
public class RegHbManagerImpl implements RegHbManager {

    @Autowired
    private RegHbDao regHbDao;

    @Override
    public List<String> getUncheckList(long hbTime) throws Exception {
        return regHbDao.getUncheckList(hbTime);
    }

    @Override
    public List<RegHealthInfo> getUncheckRegList(long startHbTime, long endHbTime) throws Exception {
        return regHbDao.getUncheckRegList(startHbTime, endHbTime);
    }

    @Override
    public RegHealthInfo getLatestUncheckRegList(long startHbTime, long endHbTime, String regAddr) throws Exception {
        return regHbDao.getLatestUncheckRegList(startHbTime, endHbTime, regAddr);
    }

    @Override
    public void delHealthHistory(String regAddr, long period) throws Exception {
        regHbDao.delHealthHistory(regAddr, period);
    }

}