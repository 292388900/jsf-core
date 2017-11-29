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

import java.util.Date;
import java.util.Map;

import com.ipd.jsf.common.constant.RegistryMonitorConstants;
import com.ipd.jsf.worker.domain.JsfRegAddr;
import com.ipd.jsf.worker.domain.RegistryStat;
import com.ipd.jsf.worker.service.JsfRegAddrService;
import com.ipd.jsf.worker.service.JsfRegStatService;
import com.ipd.jsf.worker.service.JsfRegistryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.worker.domain.RegistryReq;
import com.ipd.jsf.worker.service.JsfRegReqService;

@Service
public class JsfRegistryServiceImpl implements JsfRegistryService {
	
	@Autowired
	JsfRegReqService safRegReqService;
	
	@Autowired
    JsfRegStatService safRegStatService;
	
	@Autowired
    JsfRegAddrService safRegAddrService;

	@Override
	@Transactional
	public void saveRegistryMonit(String result, JsfRegAddr addr, Date date) throws Exception {
		Map<String, String> map = JSON.parseObject(result,Map.class);
		int conns = Integer.parseInt(map
				.get(RegistryMonitorConstants.STAT_CONN_TOTALCOUNT));
		int callbacks = Integer.parseInt(map
				.get(RegistryMonitorConstants.STAT_CALLBACK_COUNT));
		RegistryStat stat = new RegistryStat();
		stat.setIp(addr.getIp());
		stat.setPort(addr.getPort());
		stat.setConns(conns);
		stat.setCallbacks(callbacks);
		stat.setCreatedTime(date);
		safRegStatService.insert(stat);

		RegistryReq req = new RegistryReq();
		req.setIp(addr.getIp());
		req.setPort(addr.getPort());
		//provider
		req.setRequestType(RegistryMonitorConstants.STAT_REGISTRY_PROVIDER_COUNT);
		req.setRequest(Integer.parseInt(map
				.get(RegistryMonitorConstants.STAT_REGISTRY_PROVIDER_COUNT)));
		req.setCreatedTime(date);

		safRegReqService.insert(req);

		//consumer
		req.setRequestType(RegistryMonitorConstants.STAT_REGISTRY_CONSUMER_COUNT);
		req.setRequest(Integer.parseInt(map
				.get(RegistryMonitorConstants.STAT_REGISTRY_CONSUMER_COUNT)));
		safRegReqService.insert(req);
		
		addr.setConns(conns);
		addr.setRequests(Integer.parseInt(map
				.get(RegistryMonitorConstants.STAT_REGISTRY_PROVIDER_COUNT)) +
				Integer.parseInt(map
						.get(RegistryMonitorConstants.STAT_REGISTRY_CONSUMER_COUNT)));
		addr.setCallbacks(callbacks);
		addr.setLastCheckTime(date);
		safRegAddrService.updateByWorker(addr);
	}

}
