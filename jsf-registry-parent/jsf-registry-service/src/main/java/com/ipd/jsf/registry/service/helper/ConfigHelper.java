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
package com.ipd.jsf.registry.service.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Joiner;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.registry.common.RegistryConstants;

@Service
public class ConfigHelper {
	private Logger logger = LoggerFactory.getLogger(ConfigHelper.class);

	@Autowired
	private SubscribeHelper subscribeHelper;
	
	/**
	 * @param result
	 * @param tmpParamRouterMapForUpdate
	 */
	public void mergeConfigParamRouter(Map<String, Map<String, String>> result,
			Map<String, Map<String, String>> tmpParamRouterMapForUpdate) {
		if( tmpParamRouterMapForUpdate != null && !tmpParamRouterMapForUpdate.isEmpty()){
            logger.info(" param router config " + tmpParamRouterMapForUpdate);
            //添加参数路由配置
            for (Map.Entry<String, Map<String, String>> entry : tmpParamRouterMapForUpdate.entrySet()) {
                JSONObject paramRouterJSON = (JSONObject) JSONObject.toJSON(entry.getValue());
                if (result.get(entry.getKey()) == null) {
                    result.put(entry.getKey(), new HashMap<String, String>());
                }
                result.get(entry.getKey()).put(Constants.SETTING_ROUTER_RULE, paramRouterJSON.toJSONString());
            }
        }
	}

	/**
	 * @param result
	 * @param tmpAliasRouterMapForUpdate
	 */
	public void mergeConfigAliasRouter(Map<String, Map<String, String>> result, Map<String, Map<String, String>> tmpAliasRouterMapForUpdate) {
		if( tmpAliasRouterMapForUpdate != null && !tmpAliasRouterMapForUpdate.isEmpty()){
			logger.info(" alias router config " + tmpAliasRouterMapForUpdate);
			//添加分组路由配置
			for (Map.Entry<String, Map<String, String>> entry : tmpAliasRouterMapForUpdate.entrySet()) {
				JSONObject aliasRouterJSON = (JSONObject) JSONObject.toJSON(entry.getValue());
				if (result.get(entry.getKey()) == null) {
					result.put(entry.getKey(), new HashMap<String, String>());
				}
				result.get(entry.getKey()).put(RegistryConstants.SETTING_MAP_PARAM_ALIAS, aliasRouterJSON.toJSONString());
			}
		}
	}

	/**
	 * @param result
	 * @param appIfaceMap
	 */
	public void mergeConfigApp(Map<String, Map<String, String>> result, Map<String, Map<String, String>> appIfaceMap) {
		if (appIfaceMap != null && !appIfaceMap.isEmpty()) {
//            logger.info(" put app iface setting :{}", appIfaceMap.toString());
            for (String tempKey : appIfaceMap.keySet()) {
                if (result.get(tempKey) != null) {
                    result.get(tempKey).putAll(appIfaceMap.get(tempKey));
                } else if (appIfaceMap.get(tempKey) != null) {
                    result.put(tempKey, appIfaceMap.get(tempKey));
                }
            }
        }
	}

	/**
	 * 设置参数路由
	 * @param interfaceName
	 * @param paramRouterMap
	 * @param tmpParamRouterMapForUpdate
	 */
	public void putParamRouterMap(String interfaceName, Map<String, List<String>> paramRouterMap, Map<String, Map<String, String>> tmpParamRouterMapForUpdate) {
		//add by wt 参数路由缓存更新
	    if (paramRouterMap != null && !paramRouterMap.isEmpty()) {
	    	subscribeHelper.getInterfaceVo(interfaceName).putAllParamRouterMap(paramRouterMap);
	        Map<String,String> ifaceParamRouterMap = new HashMap<String, String>();
	        for ( Map.Entry<String,List<String>> entry : paramRouterMap.entrySet() ){
	            ifaceParamRouterMap.put(entry.getKey(), Joiner.on(",").join(entry.getValue()));
	        }
	        tmpParamRouterMapForUpdate.put(interfaceName, ifaceParamRouterMap);
	    } else {
	    	subscribeHelper.getInterfaceVo(interfaceName).destroyParamRouterMap();
	    }
	}

	/**
	 * 设置分组路由
	 * @param interfaceName
	 * @param aliasRouterMap
	 * @param tmpAliasRouterMapForUpdate
	 */
	public void putAliasRouterMap(String interfaceName, Map<String, List<String>> aliasRouterMap, Map<String, Map<String, String>> tmpAliasRouterMapForUpdate) {
		//add by wt 参数路由缓存更新
		if (aliasRouterMap != null && !aliasRouterMap.isEmpty()) {
			subscribeHelper.getInterfaceVo(interfaceName).putAllAliasRouterMap(aliasRouterMap);
			Map<String,String> ifaceParamRouterMap = new HashMap<String, String>();
			for (Map.Entry<String, List<String>> entry : aliasRouterMap.entrySet()) {
				ifaceParamRouterMap.put(entry.getKey(), entry.getValue().get(0));
			}
			tmpAliasRouterMapForUpdate.put(interfaceName, ifaceParamRouterMap);
		} else {
			subscribeHelper.getInterfaceVo(interfaceName).destroyAliasRouterMap();
		}
	}

}
