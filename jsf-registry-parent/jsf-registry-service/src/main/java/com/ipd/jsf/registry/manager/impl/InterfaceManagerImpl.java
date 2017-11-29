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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.registry.dao.InterfaceAliasVersionDao;
import com.ipd.jsf.registry.dao.InterfaceInfoDao;
import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.manager.InterfaceManager;
import com.ipd.jsf.registry.util.ListPaging;

@Service
public class InterfaceManagerImpl implements InterfaceManager {
	private int versionLoadLimit = 5000;
	private int itemLimit = 50;
    @Autowired
    private InterfaceInfoDao interfaceInfoDao;

    @Autowired
    private InterfaceAliasVersionDao interfaceAliasVersionDao;

    @Override
    public InterfaceInfo getByName(String ifaceName) throws Exception {
        return interfaceInfoDao.getByInterfaceName(ifaceName);
    }

    @Override
    public List<InterfaceInfo> getByIdListForProvider(List<Integer> ifaceIdList) throws Exception {
    	List<IfaceAliasVersion> versionList = interfaceAliasVersionDao.getListByIfaceIdList(ifaceIdList);
		return getInterfaceListFromIfaceAliasList(versionList);
    }

    @Override
    public List<InterfaceInfo> getByIdListForConfig(List<Integer> ifaceIdList) throws Exception {
        return interfaceInfoDao.getByIdListForConfig(ifaceIdList);
    }

    @Override
    public List<InterfaceInfo> getListForProvider() throws Exception {
        return interfaceInfoDao.getListForProvider();
    }

    @Override
    public List<InterfaceInfo> getChangeListByUpdateTime(Date lastTime) throws Exception {
        return interfaceInfoDao.getChangeListByUpdateTime(lastTime);
    }

    @Override
    public List<InterfaceInfo> getChangeListByConfigUpdateTime(Date lastTime) throws Exception {
        return interfaceInfoDao.getChangeListByConfigUpdateTime(lastTime);
    }

    @Override
    public Set<String> getInvalidList() throws Exception {
        List<InterfaceInfo> list = interfaceInfoDao.getInvalidList();
        Set<String> result = new HashSet<String>();
        if (list != null && !list.isEmpty()) {
            for (InterfaceInfo iface : list) {
                result.add(iface.getInterfaceName());
            }
        }
        return result;
    }

	@Override
	public List<InterfaceInfo> getAllListForProvider() throws Exception {
		Map<Integer, InterfaceInfo> map = new HashMap<Integer, InterfaceInfo>();
		List<InterfaceInfo> ifaceList = interfaceInfoDao.getAllValidList();
		if (!CollectionUtils.isEmpty(ifaceList)) {
			convertListToMap(map, ifaceList);
			putVersion(map, getIfaceAliasVersionList(0));
		}
		return new ArrayList<InterfaceInfo>(map.values());
	}

	@Override
	public List<InterfaceInfo> getChangeListByUpdateTimeForProvider(long lastTime) throws Exception {
		List<IfaceAliasVersion> versionList = getIfaceAliasVersionList(lastTime);
		return getInterfaceListFromIfaceAliasList(versionList);
	}

	/**
	 * @param versionList
	 * @throws Exception
	 */
	private List<InterfaceInfo> getInterfaceListFromIfaceAliasList(List<IfaceAliasVersion> versionList) throws Exception {
		Map<Integer, InterfaceInfo> map = new HashMap<Integer, InterfaceInfo>();
		if (!versionList.isEmpty()) {
			Set<Integer> interfaceIdSet = new HashSet<Integer>();
			for (IfaceAliasVersion version : versionList) {
				interfaceIdSet.add(version.getInterfaceId());
			}
			ListPaging<Integer> page = new ListPaging<Integer>(new ArrayList<Integer>(interfaceIdSet), itemLimit);
			List<Integer> subList = null;
			while(page.hasNext()) {
				subList = page.nextPageList();
				if (subList.isEmpty()) break;
				List<InterfaceInfo> list = interfaceInfoDao.getListByIds(subList);
				if (!CollectionUtils.isEmpty(list)) {
					convertListToMap(map, list);
				} else {
					break;
				}
			}
			putVersion(map, versionList);
		}
		return new ArrayList<InterfaceInfo>(map.values());
	}

	/**
	 * 取出所有的接口+alias的版本号
	 * @return
	 * @throws Exception
	 */
	private List<IfaceAliasVersion> getIfaceAliasVersionList(long lastTime) throws Exception {
		List<IfaceAliasVersion> result = new ArrayList<IfaceAliasVersion>();
		int start = 0;
		List<IfaceAliasVersion> list = null;
		while (true) {
			if (lastTime == 0) {
				list = interfaceAliasVersionDao.getAllList(start, versionLoadLimit);
			} else {
				list = interfaceAliasVersionDao.getChangeList(lastTime, start, versionLoadLimit);
			}
			start += versionLoadLimit;
			if (!list.isEmpty()) {
				result.addAll(list);
			} else {
				break;
			}
		}
		return result;
	}

	/**
	 * @param map
	 * @param list
	 */
	private void convertListToMap(Map<Integer, InterfaceInfo> map, List<InterfaceInfo> list) {
		for (InterfaceInfo iface : list) {
			if (map.get(iface.getInterfaceId()) == null) {
				map.put(iface.getInterfaceId(), iface);
			}
		}
	}

	/**
	 * @param map
	 * @param versionList
	 */
	private void putVersion(Map<Integer, InterfaceInfo> map, List<IfaceAliasVersion> versionList) {
		if (!CollectionUtils.isEmpty(versionList)) {
			for (IfaceAliasVersion version : versionList) {
				if (map.get(version.getInterfaceId()) != null) {
					map.get(version.getInterfaceId()).getVersionList().add(version);
				}
			}
		}
	}

	@Override
	public List<InterfaceInfo> getChangeListByIfaceIdAliasForProvider(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception {
		List<IfaceAliasVersion> versionList = interfaceAliasVersionDao.getListByIfaceIdAliasList(ifaceIdAliasList);
		return getInterfaceListFromIfaceAliasList(versionList);
	}

	@Override
	public List<InterfaceInfo> getInterfaceListFromVersionList(List<IfaceAliasVersion> versionList) throws Exception {
		Map<Integer, InterfaceInfo> map = new HashMap<Integer, InterfaceInfo>();
		if (!versionList.isEmpty()) {
			List<InterfaceInfo> list = new ArrayList<InterfaceInfo>();
			for (IfaceAliasVersion version : versionList) {
				InterfaceInfo info = new InterfaceInfo();
				info.setInterfaceId(version.getInterfaceId());
				list.add(info);
			}
			convertListToMap(map, list);
			putVersion(map, versionList);
		}
		return new ArrayList<InterfaceInfo>(map.values());
	}
}
