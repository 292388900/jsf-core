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

package com.ipd.jsf.version.common.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ipd.jsf.version.common.dao.AliasVersionDAO;
import com.ipd.jsf.version.common.dao.ServerAliasDAO;
import com.ipd.jsf.version.common.domain.IfaceAlias;
import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.version.common.domain.IfaceAliasVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.ipd.jsf.version.common.service.AliasVersionService;

@Service
public class AliasVersionServiceImpl implements AliasVersionService {

	@Autowired
	@Qualifier("versionServerAliasDAO")
	private ServerAliasDAO serverAliasDAO;

	@Autowired
	private AliasVersionDAO aliasVersionDAO;

	public List<IfaceAlias> updateVersionAndConfVersion(int interfaceId, Date time) throws Exception {
		List<IfaceAlias> list = updateByInterfaceId(interfaceId, time);
		aliasVersionDAO.updateConfVersion(interfaceId, time);
		return list;
	}

	public List<IfaceAlias> updateByServerList(List<IfaceServer> serverList, Date time) throws Exception {
		if (serverList == null || serverList.isEmpty()) return null;
		List<IfaceAlias> list = getUpdateIfaceAlias(serverList);
		aliasVersionDAO.batchCreate(list, time.getTime(), time);
		return list;
	}

	public void updateByServerListForCancelAlias(List<IfaceServer> serverList, String cancelAlias, Date time) throws Exception {
		if (serverList == null || serverList.isEmpty()) return;
		List<IfaceAlias> list = getUpdateIfaceAlias(serverList);
		//对取消的分组，也要刷新版本
		if (cancelAlias != null) {
			list.add(getIfaceAlias(serverList.get(0).getInterfaceId(), cancelAlias));
		}
		aliasVersionDAO.batchCreate(list, time.getTime(), time);
	}

	private List<IfaceAlias> getUpdateIfaceAlias(List<IfaceServer> serverList) {
		List<IfaceAlias> list = serverAliasDAO.getTargetAliasList(serverList);
		if (list == null) {
			list = new ArrayList<IfaceAlias>();
		}
		//原始别名
		for (IfaceServer server : serverList) {
			list.add(getIfaceAlias(server.getInterfaceId(), server.getAlias()));
		}
		return list;
	}

	private IfaceAlias getIfaceAlias(int interfaceId, String alias) {
		IfaceAlias tmp = new IfaceAlias();
		tmp.setInterfaceId(interfaceId);
		tmp.setAlias(alias);
		return tmp;
	}

	public List<IfaceAlias> updateByInterfaceIdList(List<Integer> interfaceIdList, Date time) throws Exception {
		if (interfaceIdList == null || interfaceIdList.isEmpty()) return null;
		List<IfaceAlias> list = serverAliasDAO.getTargetAliasListByIfaceIds(interfaceIdList);
		if (list == null || list.isEmpty()) {
			return list;
		}
		aliasVersionDAO.batchCreate(list, time.getTime(), time);
		return list;
	}

	public List<IfaceAlias> updateByInterfaceId(int interfaceId, Date time) throws Exception {
		List<Integer> ids = new ArrayList<Integer>();
		ids.add(interfaceId);
		return updateByInterfaceIdList(ids, time);
	}

	public List<IfaceServer> getRelaIfaceServerList(List<IfaceServer> serverList) throws Exception {
		return serverAliasDAO.getRelaAliasServerList(serverList);
	}

	public List<IfaceAliasVersion> getAliasVersionByIfaceId(int interfaceId) throws Exception{
		return aliasVersionDAO.getAliasVersionByIfaceId(interfaceId);
	}

}
