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
package com.ipd.jsf.registry.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.domain.InterfaceVisit;
import com.ipd.jsf.registry.manager.InterfaceVisitManager;
import com.ipd.jsf.registry.service.InterfaceVisitService;

@Service
public class InterfaceVisitServiceImpl implements InterfaceVisitService {
    private Logger logger = LoggerFactory.getLogger(InterfaceVisitServiceImpl.class);
    @Autowired
    private InterfaceVisitManager interfaceVisitManager;

    //缓存，每个接口对应的visit
    private ConcurrentMap<String, List<String>> ifaceVisitMap = new ConcurrentHashMap<String, List<String>>(1, 0.75f, 1);

    @Override
    public boolean check(String interfaceName, String visitorName) {
        if (ifaceVisitMap.get(interfaceName) != null && ifaceVisitMap.get(interfaceName).contains(visitorName)) {
            return true;
        }
        return false;
    }

    @Override
    public void refreshCache() throws Exception {
        //加载接口访问者数据
        loadVisitData();
    }

    /**
     * 获取变化的接口与应用关系，并转为map
     */
    private void loadVisitData() {
        try {
            List<InterfaceVisit> list = interfaceVisitManager.getAllList();
            convertListToMap(list);
            logger.info(ifaceVisitMap.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 转换list为map
     * @param list
     */
    private void convertListToMap(List<InterfaceVisit> list) {
        Map<String, List<String>> tempMap = new HashMap<String, List<String>>();
        if (list != null && !list.isEmpty()) {
            for (InterfaceVisit visitor : list) {
                if (tempMap.get(visitor.getInterfaceName()) == null) {
                    tempMap.put(visitor.getInterfaceName(), new ArrayList<String>());
                }
                if (visitor.getVisitorName() != null && !visitor.getVisitorName().isEmpty()) {
                    tempMap.get(visitor.getInterfaceName()).add(visitor.getVisitorName());
                }
            }
        }

        //把tempMap中的值，放到ifaceVisitMap里
        for (Map.Entry<String, List<String>> entry : tempMap.entrySet()) {
            if (ifaceVisitMap.get(entry.getKey()) == null) {
                ifaceVisitMap.put(entry.getKey(), entry.getValue());
            } else {
                ifaceVisitMap.get(entry.getKey()).clear();
                ifaceVisitMap.get(entry.getKey()).addAll(entry.getValue());
            }
        }
        //清除掉没用的
        for (String key : ifaceVisitMap.keySet()) {
            if (tempMap.get(key) == null) {
                ifaceVisitMap.remove(key);
            }
        }
    }
}
