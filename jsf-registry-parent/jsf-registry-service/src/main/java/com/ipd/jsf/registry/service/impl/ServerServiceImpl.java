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

import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.local.manager.ServerLocalManager;
import com.ipd.jsf.registry.manager.ServerManager;
import com.ipd.jsf.registry.service.ServerService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.util.RegistryUtil;

@Service
public class ServerServiceImpl implements ServerService {
    private Logger logger = LoggerFactory.getLogger(ServerServiceImpl.class);
//    private volatile boolean isOpenLds = false;

    @Autowired
    private ServerLocalManager serverLocalManagerImpl;

    @Autowired
    private ServerManager serverManagerImpl;

    @Autowired
    private SubscribeHelper subscribeHelper;

    @PostConstruct
    public void init() {
    	logger.info("init...");
    }

    @Override
    public void registerServer(Server server, JsfIns ins) throws Exception {
    	if (server != null) {
	    	List<Server> serverList = new ArrayList<Server>();
	    	serverList.add(server);
	    	registerServer(serverList, ins);
    	}
    }

	@Override
	public void registerServer(List<Server> serverList, JsfIns ins) throws Exception {
		if (serverList != null && !serverList.isEmpty()) {
			if (RegistryUtil.isOpenProviderBerkeleyDB) {
	            serverLocalManagerImpl.registerServer(serverList, ins);
	        } else {
	        	saveServer(serverList, ins);
	        }
		}
	}

	@Override
	public void saveServer(List<Server> serverList, JsfIns ins) throws Exception {
		List<Integer> updateFlagList = new ArrayList<Integer>();
		int updateFlag;
		for (Server server : serverList) {
			updateFlag = subscribeHelper.checkServer(server.getInterfaceName(), server);
			updateFlagList.add(updateFlag);
		}
		serverManagerImpl.registerServer(serverList, updateFlagList);
	}

    @Override
    public void unRegisterServer(Server server, JsfIns ins) throws Exception {
    	if (server != null) {
	    	List<Server> serverList = new ArrayList<Server>();
	    	serverList.add(server);
	    	unRegisterServer(serverList, ins);
    	}
    }

	@Override
	public void unRegisterServer(List<Server> serverList, JsfIns ins) throws Exception {
		if (RegistryUtil.isOpenProviderBerkeleyDB) {
            serverLocalManagerImpl.unRegisterServer(serverList, ins);
        } else {
        	removeServer(serverList, ins);
        }
	}

	@Override
	public void removeServer(List<Server> serverList, JsfIns ins) throws Exception {
		//写入mysql
    	serverManagerImpl.unRegisterServer(serverList);
	}


}
