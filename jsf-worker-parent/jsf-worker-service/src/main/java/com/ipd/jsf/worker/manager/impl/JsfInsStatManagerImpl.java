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

import java.util.Map;

import com.ipd.jsf.worker.log.dao.JsfInsStatDao;
import com.ipd.jsf.worker.manager.JsfInsStatManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.domain.JsfInsStat;

@Service
public class JsfInsStatManagerImpl implements JsfInsStatManager {
	
	@Autowired
	private JsfInsStatDao safInsStatDao;

	@Override
	public int insert(JsfInsStat safInsStat) {
		return safInsStatDao.insert(safInsStat);
	}

	@Override
	public boolean isExist(Map<String, Object> param) {
		return safInsStatDao.exist(param) > 0 ? true : false;
	}

	@Override
	public JsfInsStat getLastWeekStat() {
		return safInsStatDao.getLastWeekStat();
	}

	@Override
	public JsfInsStat getByWeek(int week) {
		return safInsStatDao.getByWeek(week);
	}

}
