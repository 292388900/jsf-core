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
package com.ipd.jsf.registry.manager.impl;

import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.dao.ServerAliasDao;
import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.ServerAlias;
import com.ipd.jsf.registry.manager.ServerAliasManager;

import org.springframework.util.CollectionUtils;

@Service
public class ServerAliasManagerImpl implements ServerAliasManager {

    @Autowired
    private ServerAliasDao serverAliasDao;

    @Override
    public List<ServerAlias> getAliasServersByInterfaceIdList(
            List<Integer> interfaceIdList) throws Exception {
        if (interfaceIdList == null || interfaceIdList.size() == 0) {
            return new ArrayList<ServerAlias>();
        }
        return serverAliasDao.getAliasServerByInterfaceIdList(interfaceIdList);
    }

    /**
     * 查找接口下的动态分组，并根据ifaceIdAliasList中alias找到一级关联动态分组alias的动态分组记录，并返回
     * @param ifaceIdAliasList
     * @return
     * @throws Exception
     */
	@Override
	public List<ServerAlias> getAliasServersByIfaceIdAliasList(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception {
        List<ServerAlias> result = new ArrayList<>();
		if (ifaceIdAliasList == null || ifaceIdAliasList.size() == 0) {
            return result;
        }
        List<ServerAlias> tmpList = serverAliasDao.getAliasServersByIfaceIdAliasList(ifaceIdAliasList);
        if (!CollectionUtils.isEmpty(tmpList)) {
            Map<Integer, Set<String>> newAliasSet = new HashMap<>();   //key：interfaceId
            for (ServerAlias serverAlias : tmpList) {
                for (IfaceAliasVersion ifaceAliasVersion : ifaceIdAliasList) {
                    //在List<ServerAlias>中，找到相同接口下与ifaceAliasVersion中的alias相关动态分组alias，放到newAliasSet中
                    if (ifaceAliasVersion.getInterfaceId() == serverAlias.getInterfaceId()
                            && (serverAlias.getSrcAlias().equals(ifaceAliasVersion.getAlias())
                            || serverAlias.getTargetAlias().equals(ifaceAliasVersion.getAlias()))) {
                        if (newAliasSet.get(ifaceAliasVersion.getInterfaceId()) ==  null) {
                            newAliasSet.put(ifaceAliasVersion.getInterfaceId(), new HashSet<>());
                        }
                        newAliasSet.get(ifaceAliasVersion.getInterfaceId()).add(serverAlias.getSrcAlias());
                        newAliasSet.get(ifaceAliasVersion.getInterfaceId()).add(serverAlias.getTargetAlias());
                    }
                }
            }
            //下面循环要单独做，因为要找到ifaceIdAliasList中alias和相关动态分组的alias，匹配的ServerAlias
            for (ServerAlias serverAlias : tmpList) {
                if (newAliasSet.get(serverAlias.getInterfaceId()) != null
                        && (newAliasSet.get(serverAlias.getInterfaceId()).contains(serverAlias.getSrcAlias())
                        || newAliasSet.get(serverAlias.getInterfaceId()).contains(serverAlias.getTargetAlias()))) {
                    result.add(serverAlias);
                }
            }
        }

        return result;
	}
}
