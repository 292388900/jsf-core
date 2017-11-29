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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.dao.AppIfaceDao;
import com.ipd.jsf.registry.domain.AppIface;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.manager.AppIfaceManager;

@Service
public class AppIfaceManagerImpl implements AppIfaceManager {
    @Autowired
    private AppIfaceDao appIfaceDao;
    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.manager.AppIfaceManager#getListByInterfaceIdList(java.util.List)
     */
    @Override
    public Map<String, Map<String, String>> getListByInterfaceIdList(
            List<InterfaceInfo> list) throws Exception {
        if (list != null && list.size() > 0) {
            List<Integer> ifaceIdList = new ArrayList<Integer>();
            for (InterfaceInfo iface : list) {
                if (iface != null && iface.getInterfaceId() != 0) {
                    ifaceIdList.add(iface.getInterfaceId());
                }
            }
            if (!ifaceIdList.isEmpty()) {
                List<AppIface> result = appIfaceDao.getListByInterfaceIdList(ifaceIdList);
                return convertListToMap(result);
            }
        }
        return null;
    }

    /**
     * @param list
     * @return
     */
    private Map<String, Map<String, String>> convertListToMap(List<AppIface> list) {
        if (list != null && list.isEmpty() == false) {
            Map<String, List<AppIface>> tempMap = new HashMap<String, List<AppIface>>();
            for (AppIface appIface : list) {
                if (tempMap.get(appIface.getInterfaceName()) == null) {
                    tempMap.put(appIface.getInterfaceName(), new ArrayList<AppIface>());
                }
                tempMap.get(appIface.getInterfaceName()).add(appIface);
            }
            Map<String, Map<String, String>> resultMap = new HashMap<String, Map<String, String>>();
            for (Map.Entry<String, List<AppIface>> entry : tempMap.entrySet()) {
                if (resultMap.get(entry.getKey()) == null) {
                    resultMap.put(entry.getKey(), new HashMap<String, String>());
                }
                resultMap.get(entry.getKey()).put(RegistryConstants.APPIFACE_KEY, JSON.toJSONString(entry.getValue()));
            }
            return resultMap;
        }
        return null;
    }

    public static void main(String[] args) {
        AppIface app = new AppIface();
        app.setInterfaceId(123);
        app.setInterfaceName("com.ipd.helloservice");
        app.setMethod("getEntr");
        app.setAlias("asdf");
        app.setAppId(444);
        app.setType(1);
        List<AppIface> list = new ArrayList<AppIface>();
        list.add(app);
        System.out.println(JSON.toJSONString(list));
    }
}
