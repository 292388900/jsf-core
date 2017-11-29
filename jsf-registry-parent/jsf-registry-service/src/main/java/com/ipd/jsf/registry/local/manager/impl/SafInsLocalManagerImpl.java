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
package com.ipd.jsf.registry.local.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.berkeley.dao.SafInsLocalDao;
import com.ipd.jsf.registry.berkeley.domain.BdbJsfIns;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.local.manager.SafInsLocalManager;

@Service
public class SafInsLocalManagerImpl implements SafInsLocalManager {
	
	@Autowired
	private SafInsLocalDao localDao;

	@Override
	public void register(JsfIns jsfIns) throws Exception {
		localDao.registry(jsfIns);
	}

	@Override
	public List<BdbJsfIns> getJsfInsList() throws Exception {
		return localDao.getAll();
	}

	@Override
	public void delete(String key) throws Exception {
		localDao.delete(key);
	}
	
    @Override
    public int getTotalCount() throws Exception {
        return localDao.getTotalCount();
    }

    @Override
	public void flush(){
		localDao.flush();
	}

}
