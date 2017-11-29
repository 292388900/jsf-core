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

import com.ipd.jsf.worker.manager.IfaceAlarmManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.domain.IfaceAlarm;
import com.ipd.jsf.worker.log.dao.IfaceAlarmDao;

@Service
public class IfaceAlarmManagerImpl implements IfaceAlarmManager {

    @Autowired
    IfaceAlarmDao ifaceAlarmDao;

    public List<IfaceAlarm> list() {
        return ifaceAlarmDao.list();
    }

    @Override
    public List<IfaceAlarm> getListByAlarmType(List<Integer> typeList) throws Exception {
        return ifaceAlarmDao.getListByAlarmType(typeList, null);
    }

    @Override
    public List<IfaceAlarm> getListByRestartAlarm(List<Integer> typeList, Integer restartAlarm) throws Exception {
        return ifaceAlarmDao.getListByAlarmType(typeList, restartAlarm);
    }
}
