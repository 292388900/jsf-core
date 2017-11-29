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

import com.ipd.jsf.registry.berkeley.dao.ServerListLocalDao;
import com.ipd.jsf.registry.berkeley.domain.BdbServerList;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.local.manager.ServerLocalManager;

@Service
public class ServerLocalManagerImpl implements ServerLocalManager {

	@Autowired
	private ServerListLocalDao localDao;

	@Override
	public void registerServer(Server server, JsfIns ins) throws Exception {
		localDao.registry(server, ins);
	}

	@Override
	public void registerServer(List<Server> serverList, JsfIns ins) throws Exception {
		localDao.registry(serverList, ins);
	}

	@Override
	public void unRegisterServer(Server server, JsfIns ins) throws Exception {
		localDao.unRegistry(server, ins);
	}

	@Override
	public void unRegisterServer(List<Server> serverList, JsfIns ins) throws Exception {
		localDao.unRegistry(serverList, ins);
	}

	@Override
	public List<BdbServerList> getServers() throws Exception {
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
