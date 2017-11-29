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

import com.ipd.jsf.worker.dao.IfaceAppDeptDao;
import com.ipd.jsf.worker.domain.IfaceAppDept;
import com.ipd.jsf.worker.manager.IfaceAppDeptManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class IfaceAppDeptManagerImpl implements IfaceAppDeptManager {
	
	@Autowired
    IfaceAppDeptDao ifaceAppDeptDao;

	@Override
	public void batchInsert(List<IfaceAppDept> appDepts) {
		for(IfaceAppDept dept : appDepts){
			if(!isExist(dept)){
				ifaceAppDeptDao.create(dept);
			}
		}
	}

	/**
	 * @param dept
	 * @return
	 */
	private boolean isExist(IfaceAppDept dept) {
		return ifaceAppDeptDao.getByInterfaceId(dept.getInterfaceId()) > 0 ? true : false;
	}

}
