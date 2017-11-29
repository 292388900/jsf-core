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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ipd.jsf.registry.dao.ServerSetDao;
import com.ipd.jsf.registry.domain.ServerSet;
import com.ipd.jsf.registry.manager.ServerSetManager;

@Service
public class ServerSetManagerImpl implements ServerSetManager {
    @Autowired
    private ServerSetDao serverSetDao;

    @Override
    public Map<Integer, Map<Integer, Map<String, String>>> getListByInterfaceIdList(List<Integer> interfaceIdList) throws Exception {
        List<ServerSet> list = serverSetDao.getListByInterfaceIdList(interfaceIdList);
        if (list != null && !list.isEmpty()) {
            Map<Integer, Map<Integer, Map<String, String>>> map = new HashMap<Integer, Map<Integer, Map<String, String>>>();
            for (ServerSet serverSet : list) {
                if (map.get(serverSet.getInterfaceId()) == null) {
                    map.put(serverSet.getInterfaceId(), new HashMap<Integer, Map<String, String>>());
                }
                if (map.get(serverSet.getInterfaceId()).get(serverSet.getServerId()) == null && serverSet.getValue() != null) {
                    map.get(serverSet.getInterfaceId()).put(serverSet.getServerId(), JSON.parseObject(serverSet.getValue(), new TypeReference<Map<String, String>>(){}));
                }
            }
            return map;
        }
        return null;
    }
}
