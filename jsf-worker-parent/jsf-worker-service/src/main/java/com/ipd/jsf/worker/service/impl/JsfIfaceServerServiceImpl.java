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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.ipd.jsf.common.util.SafTelnetClient;
import com.ipd.jsf.worker.dao.InterfaceInfoDao;
import com.ipd.jsf.worker.service.JsfIfaceServerService;
import com.ipd.jsf.worker.service.vo.JSONResult;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.worker.dao.JsfIfaceServerDAO;
import com.ipd.jsf.worker.domain.JsfIfaceServer;

public class JsfIfaceServerServiceImpl implements JsfIfaceServerService {
	
	private static final Logger logger = LoggerFactory.getLogger(JsfIfaceServerServiceImpl.class);
	
	@Autowired
	private JsfIfaceServerDAO jsfIfaceInfoDAO;

	@Autowired
	private InterfaceInfoDao interfaceInfoDao;
	@Autowired
	private JsfIfaceServerDAO jsfIfaceServerDAO;

	
	private Random rand = new Random();

	public String getServiceInfo(String erp, String interfaceName) {
		logger.info("请求的 erp:[" + erp + "] 接口名:[" + interfaceName + "]");
		JSONResult result = new JSONResult();
		result.setStatus("1");
		if(erp == null || "".equals(erp.trim()) || interfaceName == null || "".equals(interfaceName.trim())){
			result.setResult("The erp or interfaceName is required.");
			return JSON.toJSONString(result);
		}
		interfaceName = interfaceName.trim();
		erp = erp.trim();
		List<JsfIfaceServer> list = this.jsfIfaceInfoDAO.getServers(interfaceName);
		if(list == null || list.isEmpty()){
			result.setResult("The "+interfaceName +" of server is not running.");
			return JSON.toJSONString(result);
		}
		List<JsfIfaceServer> serverList = new ArrayList<JsfIfaceServer>();
		for(JsfIfaceServer info : list){
			if(info.getOwnerUser().equals(erp)){
				serverList.add(info);
			}
		}
		if(serverList.isEmpty()){
			result.setResult("The "+interfaceName +" of server is NOT JSF or NOT running.");
			return JSON.toJSONString(result);
		}

		int index = rand.nextInt(serverList.size());
		JsfIfaceServer info = serverList.get(index);
		SafTelnetClient client = null;
		try {
			client = new SafTelnetClient(info.getServerIp(), info.getServerPort(), 5000, 5000);
			String telnetMsg = client.send("info " + interfaceName);
			result.setStatus("0");
			result.setResult(telnetMsg.replaceAll("jsf>", ""));
		} catch (IOException e) {
			logger.error("", e);
			result.setResult(e.getMessage());
		} finally {
			if(client != null){
				client.close();
			}
		}
		String serviceInfo = JSON.toJSONString(result);
		logger.info("请求的 erp:[" + erp + "] 接口名:[" + interfaceName + "]; 结果:" + serviceInfo);
		return serviceInfo;
	}

	@Override
	public List<String> getInterfacesWithErp(String erp) throws Exception {
		logger.info("请求取得 erp:[" + erp + "] 的所有接口");
		JSONResult result = new JSONResult();
		result.setStatus("1");
		if(erp == null || "".equals(erp.trim())){
			logger.error("erp must be not null!");
			throw new Exception("erp must be not null!");
		}
		erp = erp.trim();
		List<JsfIfaceServer> list = this.jsfIfaceInfoDAO.getInterfaces(erp);
		if(list == null || list.isEmpty()){
			logger.info("erp[" + erp + "] 名下没有接口！");
			return new ArrayList<String>();
		}
		List<String> interfaceList = new ArrayList<String>();
		for(JsfIfaceServer info : list){
			if(!interfaceList.contains(info.getInterfaceName())){
				interfaceList.add(info.getInterfaceName());
			}
		}

		return interfaceList;
	}

	@Override
	public List<InterfaceInfo> listAllInterface() throws Exception {
		return interfaceInfoDao.getAll();
	}

	@Override
	public List<JsfIfaceServer> getOneServer(String interfaceName) throws Exception {
		return jsfIfaceServerDAO.getOneServer(interfaceName);
	}

}
