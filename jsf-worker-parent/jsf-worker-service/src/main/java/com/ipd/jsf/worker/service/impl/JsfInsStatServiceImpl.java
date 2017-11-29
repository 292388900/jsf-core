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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfInsStat;
import com.ipd.jsf.worker.manager.ClientManager;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import com.ipd.jsf.worker.manager.JsfInsStatManager;
import com.ipd.jsf.worker.manager.ServerManager;
import com.ipd.jsf.worker.manager.impl.JsfInsStatManagerImpl;
import com.ipd.jsf.worker.service.JsfInsStatService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.Server;

@Service
public class JsfInsStatServiceImpl implements JsfInsStatService {
	
	private final static Logger logger = LoggerFactory.getLogger(JsfInsStatManagerImpl.class);
	
	@Autowired
    JsfInsStatManager safInsStatManager;
	
	@Autowired
    InterfaceInfoManager interfaceInfoManager;
	
	@Autowired
    ClientManager clientManager;
	
	@Autowired
    ServerManager serverManager;

	@Override
	@Transactional
	public Map<String, Integer> getInsStatMap(Date date) throws Exception{
		
		ExecutorService executorService = Executors.newFixedThreadPool(10, new WorkerThreadFactory("jsfInsStatServicePool"));
		
		Map<String, Integer> result = new HashMap<String, Integer>();
		final List<InterfaceInfo> ifaces = interfaceInfoManager.getAllWithJsfclient(date);
		final AtomicInteger clientIns = new AtomicInteger(0);
		final AtomicInteger serverIns = new AtomicInteger(0);
		final Set<String> clientIp = Collections.synchronizedSet(new HashSet<String>());
		final Set<String> serverIp = Collections.synchronizedSet(new HashSet<String>());
		final Set<String> totalIp = Collections.synchronizedSet(new HashSet<String>());
		
		List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
		
		if (!CollectionUtils.isEmpty(ifaces)) {
			try {
				for (final InterfaceInfo info : ifaces) {
					Future<Boolean> task = executorService
							.submit(new Callable<Boolean>() {

								@Override
								public Boolean call() {
									try {
										List<Client> clients = clientManager.getJsfClients(info.getInterfaceName());
										List<Server> servers = serverManager.getJsfServers(info.getInterfaceName());
										if (clients != null) {
											clientIns.addAndGet(clients.size());
											for (Client client : clients) {
												clientIp.add(client.getIp());
												totalIp.add(client.getIp());
											}
										}
										if (servers != null) {
											serverIns.addAndGet(servers.size());
											for (Server server : servers) {
												serverIp.add(server.getIp());
												totalIp.add(server.getIp());
											}
										}
										clients = null;
										servers = null;
										return true;
									} catch (Exception e) {
										logger.error(e.getMessage(), e);
										Thread.currentThread().interrupt();
									}
									return null;

								}
							});
					tasks.add(task);
				}

				for (Future<Boolean> future : tasks) {
					try {
						future.get();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			} finally {
				executorService.shutdown();
				executorService = null;
			}
		}
		result.put(SAF_PROVIDER_INSTANCE_NUM, serverIns.get());
		result.put(SAF_PROVIDER_IP_NUM, serverIp.size());
		result.put(SAF_CONSUMER_INSTANCE_NUM, clientIns.get());
		result.put(SAF_CONSUMER_IP_NUM, clientIp.size());
		result.put(SAF_TOTAL_IP_NUM, totalIp.size());
		clientIp.clear();
		serverIp.clear();
		totalIp.clear();
		ifaces.clear();
		tasks = null;
		return result;
	}

	@Override
	public int insert(JsfInsStat safInsStat) {
		return safInsStatManager.insert(safInsStat);
	}

	@Override
	public boolean isExist(Map<String, Object> param) {
		return safInsStatManager.isExist(param);
	}

	@Override
	public JsfInsStat getLastWeekStat() {
		return safInsStatManager.getLastWeekStat();
	}

	@Override
	public JsfInsStat getByWeek(int week) {
		return safInsStatManager.getByWeek(week);
	}

}
