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
package com.ipd.jsf.registry.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.local.manager.ClientLocalManager;
import com.ipd.jsf.registry.manager.ClientManager;
import com.ipd.jsf.registry.service.ClientService;
import com.ipd.jsf.registry.util.RegistryUtil;

@Service
public class ClientServiceImpl implements ClientService {
	private Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
//	private volatile boolean isOpenLds = false;

    @Autowired
    private ClientManager clientManager;

    @Autowired
    private ClientLocalManager clientLocalManagerImpl;

    @PostConstruct
    public void init() {
    	logger.info("init...");
    }

    @Override
    public void registerClient(Client client, JsfIns ins) throws Exception {
    	if (client != null) {
	    	List<Client> clientList = new ArrayList<Client>();
	    	clientList.add(client);
	        if (RegistryUtil.isOpenWholeBerkeleyDB) {
	            clientLocalManagerImpl.registerClient(clientList, ins);
	        } else {
	            clientManager.registerClient(clientList);
	        }
    	}
    }

	@Override
	public void registerClient(List<Client> clientList, JsfIns ins) throws Exception {
		if (RegistryUtil.isOpenWholeBerkeleyDB) {
            clientLocalManagerImpl.registerClient(clientList, ins);
        } else {
        	saveClient(clientList, ins);
        }
	}

	@Override
	public void saveClient(List<Client> clientList, JsfIns ins) throws Exception {
		clientManager.registerClient(clientList);
	}

    @Override
    public void unRegisterClient(Client client, JsfIns ins) throws Exception {
    	if (client != null) {
	    	List<Client> clientList = new ArrayList<Client>();
	    	clientList.add(client);
	        if (RegistryUtil.isOpenWholeBerkeleyDB) {
	            clientLocalManagerImpl.unregisterClient(clientList, ins);
	        } else {
	            clientManager.unRegisterClient(clientList);
	        }
    	}
    }

	@Override
	public void unRegisterClient(List<Client> clientList, JsfIns ins) throws Exception {
		if (RegistryUtil.isOpenWholeBerkeleyDB) {
            clientLocalManagerImpl.unregisterClient(clientList, ins);
        } else {
        	removeClient(clientList, ins);
        }
	}

	@Override
	public void removeClient(List<Client> clientList, JsfIns ins) throws Exception {
		clientManager.unRegisterClient(clientList);
	}
}
