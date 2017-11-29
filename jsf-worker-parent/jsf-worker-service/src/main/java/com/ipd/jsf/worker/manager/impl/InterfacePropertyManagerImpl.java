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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.dao.InterfacePropertyDao;
import com.ipd.jsf.worker.domain.InterfaceProperty;
import com.ipd.jsf.worker.manager.InterfacePropertyManager;

@Service
public class InterfacePropertyManagerImpl implements InterfacePropertyManager {

    @Autowired
    private InterfacePropertyDao interfacePropertyDao;

    /**
     * @param list
     */
    private Map<String, Map<String, String>> convertListToMap(List<InterfaceProperty> list) {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String,String>>();
        if (list != null && list.size() > 0) {
            for (InterfaceProperty property : list) {
                if (map.get(property.getInterfaceName()) == null) {
                    map.put(property.getInterfaceName(), new HashMap<String, String>());
                }
                if (map.get(property.getInterfaceName()).get(property.getParamKey()) == null) {
                    map.get(property.getInterfaceName()).put(property.getParamKey(), property.getParamValue());
                }
            }
        }
        return map;
    }

    @Override
    public Map<String, Map<String, String>> getList(int type) throws Exception {
        List<InterfaceProperty> result = interfacePropertyDao.getList(type);
        return convertListToMap(result);
    }
}
