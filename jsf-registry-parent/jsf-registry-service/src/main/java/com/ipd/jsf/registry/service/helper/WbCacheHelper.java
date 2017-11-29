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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.registry.domain.Ipwblist;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.gd.util.Constants;

@Service
public class WbCacheHelper {
	private Logger logger = LoggerFactory.getLogger(WbCacheHelper.class);
    /** 黑白名单结果列表 key1=interfaceName, key2=黑白名单类型 */
    private ConcurrentHashMap<String, Map<String, List<String>>> wbCacheMap = new ConcurrentHashMap<String, Map<String, List<String>>>();

    /** 黑白名单检查结果  key1=interfaceName, key2=ip, value=check result */
    private ConcurrentHashMap<String, Map<String, Boolean>> wbCheckRecordCache = new ConcurrentHashMap<String, Map<String, Boolean>>();

    /**
     * 检查黑白名单. 返回值为true，说明clientIp可以访问，false，clientIp不能访问
     * 
     * 1、白名单为空，下一步。白名单不为空时，如果白名单中不包括，直接返回false；如果包括，下一步
     * 2、黑名单为空，下一步。如果黑名单包括，直接返回false；如果黑名单不包括，下一步
     * 3、返回true
     * @param ifaceName
     * @param ip
     * @return
     */
    public boolean checkCanVisit(String ifaceName, String ip) {
    	//先检查缓存
        if (wbCacheMap == null || wbCacheMap.size() == 0 || ip == null || "".equals(ip)) {
            return true;
        }
        if (CollectionUtils.isEmpty(this.wbCacheMap.get(ifaceName))) {
        	return true;
        }
        if (this.wbCheckRecordCache.get(ifaceName) != null && this.wbCheckRecordCache.get(ifaceName).get(ip) != null) {
            return wbCheckRecordCache.get(ifaceName).get(ip).booleanValue();
        }

        // 初始化黑白名单缓存
        if (this.wbCheckRecordCache.get(ifaceName) == null) {
            wbCheckRecordCache.put(ifaceName, new HashMap<String, Boolean>());
        }

    	boolean result = false;
    	try {
	    	// 检查白名单
            List<String> whiteList = this.wbCacheMap.get(ifaceName).get(Constants.SETTING_INVOKE_WHITELIST);
            if (whiteList != null && !whiteList.isEmpty()) {
                for (String white : whiteList) {
                    if (RegistryUtil.match(white, ip)) {
                        result = true;
                        break;
                    }
                }
                if (!result) {
                    return result;
                }
            } else {
                result = true;
            }

	    	//黑名单检查
	    	List<String> blackList = this.wbCacheMap.get(ifaceName).get(Constants.SETTING_INVOKE_BLACKLIST);
            if (blackList != null && !blackList.isEmpty()) {
                for (String black : blackList) {
                    if (!"".equals(black) && RegistryUtil.match(black, ip)) {
                        result = false;
                        break;
                    }
                }
            }
    	} catch (Exception e) {
    	    logger.error("checkCanVisit error!", e);
    	} finally {
    		wbCheckRecordCache.get(ifaceName).put(ip, result);
    	}
    	return result;
    }


	/**
	 * 设置黑白名单
	 * @param ipwbMap
	 * @param wbTempMap
	 * @param interfaceName
	 */
    public void putWbMap(Map<String, Map<String, List<Ipwblist>>> ipwbMap, Map<String, Map<String, String>> wbTempMap, String interfaceName) {
		Map<String, List<String>> ipwbTempMap;
		Map<String, List<String>> cacheValue;
		String ipstring;
		//将黑白名单转为Map<String, List<String>>格式
		ipwbTempMap = convertIpwbMap(ipwbMap.get(interfaceName));

		//放到wbCacheMap中
		cacheValue = wbCacheMap.get(interfaceName);
		if (!CollectionUtils.isEmpty(cacheValue) || !CollectionUtils.isEmpty(ipwbTempMap)) {
		    cacheValue = new HashMap<String, List<String>>();
		    if (!CollectionUtils.isEmpty(ipwbTempMap)) {
		        cacheValue.putAll(ipwbTempMap);
		        wbCacheMap.put(interfaceName, cacheValue);
		    } else {
		    	wbCacheMap.remove(interfaceName);
		    }
		}

		//再转为saf客户端的格式Map<String, Map<String, String>>
		if (wbTempMap.get(interfaceName) == null) {
		    wbTempMap.put(interfaceName, new HashMap<String, String>());
		}

		if (cacheValue != null && cacheValue.get(Constants.SETTING_INVOKE_WHITELIST) != null && !cacheValue.get(Constants.SETTING_INVOKE_WHITELIST).isEmpty()) {
		    ipstring = RegistryUtil.getStringFromList(cacheValue.get(Constants.SETTING_INVOKE_WHITELIST));
		    wbTempMap.get(interfaceName).put(Constants.SETTING_INVOKE_WHITELIST, ipstring);
		} else {//如果白名单没有，就设置*
		    wbTempMap.get(interfaceName).put(Constants.SETTING_INVOKE_WHITELIST, "*");
		}
		if (cacheValue != null && cacheValue.get(Constants.SETTING_INVOKE_BLACKLIST) != null && !cacheValue.get(Constants.SETTING_INVOKE_BLACKLIST).isEmpty()) {
		    ipstring = RegistryUtil.getStringFromList(cacheValue.get(Constants.SETTING_INVOKE_BLACKLIST));
		    wbTempMap.get(interfaceName).put(Constants.SETTING_INVOKE_BLACKLIST, ipstring);
		} else {//如果黑名单没有，就设置空
		    wbTempMap.get(interfaceName).put(Constants.SETTING_INVOKE_BLACKLIST, "");
		}
	}

	/**
	 * @param result
	 * @param wbTempMap
	 * @return
	 */
	public Map<String, Map<String, Boolean>> mergeConfigWb(Map<String, Map<String, String>> result, Map<String, Map<String, String>> wbTempMap) {
		Map<String, Map<String, Boolean>> wbCheckRecordOldCache = new HashMap<String, Map<String,Boolean>>();
        //将接口配置和黑白名单合并
        Map<String, String> value = null;
        for (Map.Entry<String, Map<String, String>> entry : result.entrySet()) {
            value = wbTempMap.get(entry.getKey());
            if (value != null && !value.isEmpty()) {
                result.get(entry.getKey()).putAll(value);
            }
            //将wbCheckRecordCache相应接口的以前的记录保留到wbCheckRecordOldCache，然后从wbCheckRecordCache删除
            if (wbCheckRecordCache.get(entry.getKey()) != null) {
                wbCheckRecordOldCache.put(entry.getKey(), wbCheckRecordCache.get(entry.getKey()));
                //consumer访问记录删除
                wbCheckRecordCache.remove(entry.getKey());
            }
        }
		return wbCheckRecordOldCache;
	}

    private Map<String, List<String>> convertIpwbMap(Map<String, List<Ipwblist>> ipwbValue) {
        Map<String, List<String>> ipwbTempMap = null;
        if (ipwbValue != null) {
            ipwbTempMap = new HashMap<String, List<String>>();
            for (Map.Entry<String, List<Ipwblist>> valueEntry : ipwbValue.entrySet()) {
                if (ipwbTempMap.get(valueEntry.getKey()) == null) {
                    ipwbTempMap.put(valueEntry.getKey(), new ArrayList<String>());
                }
                for (Ipwblist ipwb : valueEntry.getValue()) {
                    if (ipwb.getRegular() != null && !ipwb.getRegular().equals("")) {
                        ipwbTempMap.get(valueEntry.getKey()).add(ipwb.getRegular());
                    }
                }
            }
        }
        return ipwbTempMap;
    }

    public Map<String, List<String>> getWbCache(String interfaceName) {
    	return wbCacheMap.get(interfaceName);
    }

    public void putWbCache(String interfaceName, Map<String, List<String>> config) {
    	wbCacheMap.put(interfaceName, config);
    }

    public Map<String, Boolean> getWbRecordCache(String interfaceName) {
    	return wbCheckRecordCache.get(interfaceName);
    }

    public void removeWbRecordCache(String interfaceName) {
    	wbCheckRecordCache.remove(interfaceName);
    }
}
