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

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.ipd.fastjson.JSONObject;
import com.ipd.jsf.common.constant.RouterConstants;
import com.ipd.jsf.registry.dao.ServerRouterDao;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.ServerRouter;
import com.ipd.jsf.registry.manager.ServerRouterManager;

@Service
public class ServerRouterManagerImpl implements ServerRouterManager {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Resource
    private ServerRouterDao serverRouterDao;

    @Override
    public Map<String,Map<String,List<String>>> getServerRouterByInterfaceIdList(List<InterfaceInfo> list,int type) throws Exception {
        Map<String, Map<String, List<String>>> routerMap = new HashMap<String, Map<String, List<String>>>();

        if ( list != null && list.size() > 0 ){
            List<Integer> ifaceIds = Lists.transform(list,new Function<InterfaceInfo, Integer>() {
                @Override
                public Integer apply(InterfaceInfo input) {
                    return input.getInterfaceId();
                }
            });
            List<ServerRouter> serverRouters = serverRouterDao.getListByInterfaceIdList(ifaceIds,type);
            if ( serverRouters != null && serverRouters.size() > 0 ){
                for ( ServerRouter serverRouter : serverRouters ){
                    Map<String,List<String>> routerItems = null;
                    if ( routerMap.containsKey(serverRouter.getInterfaceName())){
                        routerItems = routerMap.get(serverRouter.getInterfaceName());
                        try {
                            JSONObject value = JSONObject.parseObject(serverRouter.getValue());
                            String itemValue = value.getString(RouterConstants.ROUTER_VALUE_V);
                            String itemKey = value.getString(RouterConstants.ROUTER_VALUE_K);
                            if ( itemValue != null && !"".equals(itemValue)){
                                if( routerItems.containsKey(itemKey)){
                                    //一个客户端ip表达式可能配置多个ip路由
                                    routerItems.get(itemKey).add(itemValue);
                                } else {
                                    routerItems.put(itemKey, Lists.newArrayList(itemValue));
                                }
                            }
                        } catch (Exception e) {
                            logger.warn("{} server router config value is not valid json",serverRouter.getInterfaceName(),e);
                        }
                    } else {
                        routerMap.put(serverRouter.getInterfaceName(),createRouterItemMap(serverRouter));
                    }
                }
            }
        }
        return routerMap;
    }

    private Map<String,List<String>> createRouterItemMap(ServerRouter serverRouter){
        Map<String, List<String>> routerItems = new HashMap<String, List<String>>();
        try {
            JSONObject value = JSONObject.parseObject(serverRouter.getValue());
            String itemValue = value.getString(RouterConstants.ROUTER_VALUE_V);
            String itemKey = value.getString(RouterConstants.ROUTER_VALUE_K);
            if ( itemValue != null && !"".equals(itemValue)){
                routerItems.put(itemKey, Lists.newArrayList(itemValue));
            }
        } catch (Exception e) {
            logger.warn("{} server router config value is not valid json",serverRouter.getInterfaceName(),e);
        }
        return routerItems;
    }
}
