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

import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import com.ipd.jsf.worker.log.dao.JsfAlarmHistoryDao;
import com.ipd.jsf.worker.manager.AlarmManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlarmManagerImpl implements AlarmManager {

    @Autowired
    private JsfAlarmHistoryDao jsfAlarmHistoryDao;

    @Override
    public void batchInsert(List<JsfAlarmHistory> list) throws Exception {
        jsfAlarmHistoryDao.batchInsert(list);
    }

	@Override
	public int insert(JsfAlarmHistory record) {
		return jsfAlarmHistoryDao.insert(record);
	}

    @Override
    public List<JsfAlarmHistory> getAlarmHistoryList(Date createTime, int alarmType) {
        return jsfAlarmHistoryDao.getAlarmHistoryList(createTime, alarmType);
    }
}
