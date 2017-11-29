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

import com.ipd.jsf.registry.berkeley.dao.ClientListLocalDao;
import com.ipd.jsf.registry.berkeley.domain.BdbClientList;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.local.manager.ClientLocalManager;

@Service
public class ClientLocalManagerImpl implements ClientLocalManager {
	
	@Autowired
	private ClientListLocalDao localDao;

	@Override
	public void registerClient(Client client, JsfIns ins) throws Exception {
		localDao.registry(client, ins);
	}

	@Override
	public void registerClient(List<Client> clientList, JsfIns ins) throws Exception {
		localDao.registry(clientList, ins);
	}

	@Override
	public void unregisterClient(Client client, JsfIns ins) throws Exception {
		localDao.unRegistry(client, ins);
	}

	@Override
	public void unregisterClient(List<Client> clientList, JsfIns ins) throws Exception {
		localDao.unRegistry(clientList, ins);
	}

	@Override
	public List<BdbClientList> getClients() throws Exception {
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
