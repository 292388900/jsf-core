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
import com.ipd.jsf.registry.dao.InterfaceMockDao;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.InterfaceMock;
import com.ipd.jsf.registry.domain.Mock;
import com.ipd.jsf.registry.manager.InterfaceMockManager;

@Service
public class InterfaceMockManagerImpl implements InterfaceMockManager {
    @Autowired
    private InterfaceMockDao interfaceMockDao;

    /**
     * @param list
     */
    private Map<String, Map<String, Map<String, String>>> convertListToMap(List<InterfaceMock> list) {
        Map<String, Map<String, Map<String, String>>> resultMap = new HashMap<String, Map<String, Map<String, String>>>();
        if (list != null && list.size() > 0) {
            Map<String, Map<String, List<Mock>>> tempMap = new HashMap<String, Map<String,List<Mock>>>();
            for (InterfaceMock interfaceMock : list) {
                if (tempMap.get(interfaceMock.getInterfaceName()) == null) {
                    tempMap.put(interfaceMock.getInterfaceName(), new HashMap<String, List<Mock>>());
                }
                if (tempMap.get(interfaceMock.getInterfaceName()).get(interfaceMock.getIp()) == null) {
                    tempMap.get(interfaceMock.getInterfaceName()).put(interfaceMock.getIp(), new ArrayList<Mock>());
                }
                tempMap.get(interfaceMock.getInterfaceName()).get(interfaceMock.getIp()).add(getMockFromInterfaceMock(interfaceMock));
            }
            if (!tempMap.isEmpty()) {
                String mockValueJson = null;
                for (Map.Entry<String, Map<String, List<Mock>>> entry : tempMap.entrySet()) {
                    if (resultMap.get(entry.getKey()) == null && entry.getValue() != null && !entry.getValue().isEmpty()) {
                        //设置key1:interfaceName
                        resultMap.put(entry.getKey(), new HashMap<String, Map<String, String>>());
                        for (Map.Entry<String, List<Mock>> subEntry : entry.getValue().entrySet()) {
                             if (entry.getValue().get(subEntry.getKey()) != null && subEntry.getValue() != null && !subEntry.getValue().isEmpty()) {
                                 mockValueJson = JSON.toJSONString(subEntry.getValue());
                                 if (resultMap.get(entry.getKey()).get(subEntry.getKey()) == null) {
                                     resultMap.get(entry.getKey()).put(subEntry.getKey(), new HashMap<String, String>());
                                 }
                                 resultMap.get(entry.getKey()).get(subEntry.getKey()).put(RegistryConstants.MOCK_KEY, mockValueJson);
                             }
                        }
                    }
                }
            }
        }
        return resultMap;
    }

    private Mock getMockFromInterfaceMock(InterfaceMock interfaceMock) {
        Mock mock = new Mock();
        mock.setMethod(interfaceMock.getMethod());
        mock.setAlias(interfaceMock.getAlias());
        mock.setMockValue(interfaceMock.getMockValue());
        return mock;
    }
    
    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.manager.InterfaceMockManager#getListByInterfaceIdList(java.util.List)
     */
    @Override
    public Map<String, Map<String, Map<String, String>>> getListByInterfaceIdList(List<InterfaceInfo> list) throws Exception {
        if (list != null && list.size() > 0) {
            List<Integer> ifaceIdList = new ArrayList<Integer>();
            for (InterfaceInfo iface : list) {
                if (iface != null && iface.getInterfaceId() != 0) {
                    ifaceIdList.add(iface.getInterfaceId());
                }
            }
            List<InterfaceMock> result = interfaceMockDao.getListByInterfaceIdList(ifaceIdList);
            return convertListToMap(result);
        }
        return new HashMap<String, Map<String, Map<String, String>>>();
    }

}
