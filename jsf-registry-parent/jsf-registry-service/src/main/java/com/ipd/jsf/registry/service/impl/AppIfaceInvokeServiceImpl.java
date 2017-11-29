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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.AppIfaceInvoke;
import com.ipd.jsf.registry.manager.AppIfaceInvokeManager;
import com.ipd.jsf.registry.service.AppIfaceInvokeService;

@Service
public class AppIfaceInvokeServiceImpl implements AppIfaceInvokeService {
    private Logger logger = LoggerFactory.getLogger(AppIfaceInvokeServiceImpl.class);
    private ConcurrentHashMap<String, List<Integer>> appIfaceInvokeMap = new ConcurrentHashMap<String, List<Integer>>(4, 0.75f, 4);
    private List<String> ifaceList = Collections.synchronizedList(new ArrayList<String>());

    @Autowired
    private AppIfaceInvokeManager appIfaceInvokeManager;

    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.service.AppIfaceInvokeService#check(java.lang.String, int)
     */
    @Override
    public boolean check(String interfaceName, int appId) {
        try {
            //如果接口没有打开app限制开关，就直接返回true
            if (ifaceList.contains(interfaceName) == false || appId == RegistryConstants.APPID_VALUE) {
                return true;
            }
            //如果接口打开了app限制开关，就判断接口是否配置了此appId，如果没有就返回false，如果有就返回true
            if (appIfaceInvokeMap.get(interfaceName) != null && appIfaceInvokeMap.get(interfaceName).contains(appId)) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            //如果抛异常，先按照通过处理
            return true;
        }
        logger.info("interfaceName : {}, appId : {} is restricted...", interfaceName, appId);
        return false;
    }

    @Override
    public void refreshCache() {
        //加载接口
        loadIfaceData();
        //加载接口与appid
        loadAppIfaceInvokeData();
    }

    /**
     * 加载接口与appid
     */
    private void loadAppIfaceInvokeData() {
        try {
            List<AppIfaceInvoke> list = appIfaceInvokeManager.getList();
            convertAppIfaceInvokeListToMap(list);
            logger.info(appIfaceInvokeMap.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void convertAppIfaceInvokeListToMap(List<AppIfaceInvoke> list) {
        Map<String, List<Integer>> tempMap = new HashMap<String, List<Integer>>();
        if (list != null && !list.isEmpty()) {
            for (AppIfaceInvoke invoke : list) {
                if (tempMap.get(invoke.getInterfaceName()) == null) {
                    tempMap.put(invoke.getInterfaceName(), new ArrayList<Integer>());
                }
                if (invoke.getAppId() != 0) {
                    tempMap.get(invoke.getInterfaceName()).add(invoke.getAppId());
                }
            }
        }

        //把tempMap中的值，放到appIfaceInvokeMap里
        for (Map.Entry<String, List<Integer>> entry : tempMap.entrySet()) {
            if (appIfaceInvokeMap.get(entry.getKey()) == null) {
                appIfaceInvokeMap.put(entry.getKey(), entry.getValue());
            } else {
                appIfaceInvokeMap.get(entry.getKey()).clear();
                appIfaceInvokeMap.get(entry.getKey()).addAll(entry.getValue());
            }
        }

        //清除掉不存在的key
        for (String key : appIfaceInvokeMap.keySet()) {
            if (tempMap.get(key) == null) {
                appIfaceInvokeMap.remove(key);
            }
        }
    }

    /**
     * 加载接口
     */
    private void loadIfaceData() {
        try {
            List<String> list = appIfaceInvokeManager.getAppInvokeIfaceList();
            putToList(list);
            logger.info(ifaceList.toString());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void putToList(List<String> list) {
        Set<String> tempSet = new HashSet<String>();
        if (list != null && !list.isEmpty()) {
            tempSet.addAll(list);
        }

        //把tempSet中的值，放到ifaceList里
        for (String iface : tempSet) {
            if (ifaceList.contains(iface) == false) {
                ifaceList.add(iface);
            }
        }

        Set<String> removeSet = new HashSet<String>();
        //清除掉不存在的key
        for (String key : ifaceList) {
            if (tempSet.contains(key) == false) {
                removeSet.add(key);
            }
        }

        if (!removeSet.isEmpty()) {
            ifaceList.removeAll(removeSet);
        }
    }
}
