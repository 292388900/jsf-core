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

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.worker.dao.InterfaceDataVersionDao;
import com.ipd.jsf.worker.dao.ScanServerDao;
import com.ipd.jsf.worker.dao.ServerDao;
import com.ipd.jsf.worker.manager.JsfDeployManager;
import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.version.common.service.AliasVersionService;
import com.ipd.jsf.worker.domain.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JsfDeployManagerImpl implements JsfDeployManager {
	private static Logger logger = LoggerFactory.getLogger(JsfDeployManagerImpl.class);
	private String logCreator = "autodeploy";
	@Autowired
	private ServerDao serverDao;
	@Autowired
	private ScanServerDao scanServerDao;
	@Autowired
	private InterfaceDataVersionDao interfaceDataVersionDao;
	@Autowired
	private com.ipd.jsf.worker.manager.ScanServerManager ScanServerManager;
	@Autowired
	private AliasVersionService aliasVersionService;

	@Override
	@Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED, readOnly = false)
	public List<Server> onlineByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception {
		//用appId和appInsId查找server列表， 不用pid，为了减少时间，避开注册中心的berkeley异步注册时间
		List<Server> servers = serverDao.getServersByApp(appId, appInsId, null);
		if (servers != null && !servers.isEmpty()) {
			List<Integer> serverIds = new ArrayList<Integer>();
			List<Integer> ifaceIds = new ArrayList<Integer>();
			List<IfaceServer> ifaceServerList = new ArrayList<IfaceServer>();
			for (Server server : servers) {
				ifaceIds.add(server.getInterfaceId());
				serverIds.add(server.getId());
				ifaceServerList.add(getIfaceServer(server));
			}
			logger.info("deploy.online.serverIds: {}", serverIds);
			serverDao.updateServerToOnline(serverIds);
			interfaceDataVersionDao.update(ifaceIds, date);
			logger.info("online ifaceAlias list:" + aliasVersionService.updateByServerList(ifaceServerList, date));
			List<IfaceServer> relaAliasServerList = aliasVersionService.getRelaIfaceServerList(ifaceServerList);
			logger.info("自动部署调用: 上线服务端成功, appId:{}, appInsId:{}, serverIds: {}", appId, appInsId, serverIds.toString());
			mergeServers(servers, relaAliasServerList);
			return servers;
		}
		return null;
	}

	@Override
	@Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED, readOnly = false)
	public List<Server> offlineByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception {
		List<Server> servers = serverDao.getServersByApp(appId, appInsId, pid);
		if (servers != null && !servers.isEmpty()) {
			List<Integer> serverIds = new ArrayList<Integer>();
			List<Integer> ifaceIds = new ArrayList<Integer>();
			List<IfaceServer> ifaceServerList = new ArrayList<IfaceServer>();
			for (Server server : servers) {
				ifaceIds.add(server.getInterfaceId());
				serverIds.add(server.getId());
				ifaceServerList.add(getIfaceServer(server));
			}
			logger.info("deploy.offline.serverIds: {}", serverIds);
			serverDao.updateServerToOffline(serverIds);
			interfaceDataVersionDao.update(ifaceIds, date);
			logger.info("offline ifaceAlias list:" + aliasVersionService.updateByServerList(ifaceServerList, date));
			List<IfaceServer> relaAliasServerList = aliasVersionService.getRelaIfaceServerList(ifaceServerList);
			logger.info("自动部署调用: 下线服务端成功, appId:{}, appInsId:{}, serverIds: {}", appId, appInsId, serverIds.toString());
			mergeServers(servers, relaAliasServerList);
			return servers;
		}
		return null;
	}

	@Override
	public List<Server> delByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception {
		List<Server> servers = serverDao.getServersByApp(appId, appInsId, pid);
		if (servers != null && !servers.isEmpty()) {
			List<Integer> serverIds = new ArrayList<Integer>();
			List<Integer> ifaceIds = new ArrayList<Integer>();
			List<IfaceServer> ifaceServerList = new ArrayList<IfaceServer>();
			for (Server server : servers) {
				ifaceIds.add(server.getInterfaceId());
				serverIds.add(server.getId());
				ifaceServerList.add(getIfaceServer(server));
			}
			logger.info("deploy.tagtodel.serverIds: {}", serverIds);
			//逻辑删除server
			scanServerDao.batchTagServerToDelByIds(serverIds, date.getTime(), InstanceStatus.deleted.value().intValue());
			logger.info("tagtodel ifaceAlias list:" + aliasVersionService.updateByServerList(ifaceServerList, date));
			List<IfaceServer> relaAliasServerList = aliasVersionService.getRelaIfaceServerList(ifaceServerList);
			logger.info("自动部署调用: 删除服务端成功, appId:{}, appInsId:{}, serverIds: {}", appId, appInsId, serverIds.toString());
			mergeServers(servers, relaAliasServerList);
			ScanServerManager.recordLog_Tagdel(servers, logCreator);
			return servers;
		}
		return null;
	}

	private void mergeServers(List<Server> list, List<IfaceServer> relaAliasServerList) {
		if (relaAliasServerList != null) {
			List<Server> tmpList = new ArrayList<Server>();
			for (IfaceServer ifaceServer : relaAliasServerList) {
				for (Server server : list) {
					if (server.getUniqKey().equals(ifaceServer.getUniqKey())) {
						tmpList.add(cloneServer(server, ifaceServer.getAlias()));
					}
				}
			}
			list.addAll(tmpList);
		}
	}

	private IfaceServer getIfaceServer(Server server) {
		IfaceServer ifaceServer = new IfaceServer();
		ifaceServer.setAlias(server.getAlias());
		ifaceServer.setInterfaceId(server.getInterfaceId());
		ifaceServer.setUniqKey(server.getUniqKey());
		return ifaceServer;
	}

	private Server cloneServer(Server server, String alias) {
		Server result = new Server();
		result.setIp(server.getIp());
		result.setPid(server.getPid());
		result.setInterfaceId(server.getInterfaceId());
		result.setPort(server.getPort());
		result.setProtocol(server.getProtocol());
		result.setStartTime(server.getStartTime());
		result.setAlias(alias);
		result.setInterfaceId(server.getInterfaceId());
		result.setSafVer(server.getSafVer());
		return result;
	}


}
