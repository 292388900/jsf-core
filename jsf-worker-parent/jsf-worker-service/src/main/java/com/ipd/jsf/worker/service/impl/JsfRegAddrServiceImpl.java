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
package com.ipd.jsf.worker.service.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.ipd.jsf.worker.log.dao.JsfRegAddrDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Joiner;
import com.ipd.jsf.worker.domain.JsfRegAddr;
import com.ipd.jsf.worker.service.JsfRegAddrService;

@Service
public class JsfRegAddrServiceImpl implements JsfRegAddrService{
	
	@Autowired
	private JsfRegAddrDao safRegAddrDao;

	@Override
	public int updateByWorker(JsfRegAddr safRegAddr) {
		return safRegAddrDao.updateByWorker(safRegAddr);
	}

	@Override
	public List<JsfRegAddr> listAll() {
		return safRegAddrDao.listAll();
	}

	@Override
	public int updateStatus(JsfRegAddr addr) {
		return safRegAddrDao.updateStasus(addr);
	}

	@Override
	public String getAllUrl() {
		Set<String> set = new HashSet<String>();
		List<JsfRegAddr> addrs = (List<JsfRegAddr>) this.listAll();
		if(!CollectionUtils.isEmpty(addrs)){
			for(JsfRegAddr addr : addrs){
				set.add(addr.getIp()+":"+addr.getPort());
			}
		}
		addrs = null;
		return Joiner.on(";").join(set);
	}

	@Override
	public List<JsfRegAddr> getValidList(){
		return safRegAddrDao.getValidList();
	}

}
