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

import com.ipd.jsf.worker.domain.IfaceInsStat;
import com.ipd.jsf.worker.log.dao.IfaceInsStatDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.manager.IfaceInsStatManager;


@Service
public class IfaceInsStatManagerImpl implements IfaceInsStatManager{
	
	@Autowired
	private IfaceInsStatDao ifaceInsStatDao;

	@Override
	public boolean exists(IfaceInsStat insStat) {
		return ifaceInsStatDao.exists(insStat) > 0 ? true : false;
	}

	@Override
	public IfaceInsStat getByLastWeek(String iface, int week) {
		return ifaceInsStatDao.getByLastWeek(iface, week);
	}

	@Override
	public IfaceInsStat getLastWeek(IfaceInsStat ifaceInsStat) {
		return ifaceInsStatDao.getLastWeek(ifaceInsStat);
	}

	@Override
	public IfaceInsStat getLastWeekRecord(String iface) {
		return ifaceInsStatDao.getLastWeekRecord(iface);
	}

	@Override
	public int insert(IfaceInsStat ifaceInsStat) {
		return ifaceInsStatDao.insert(ifaceInsStat);
	}

}
