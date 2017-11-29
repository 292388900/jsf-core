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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.domain.CallbackLog;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.recoder.CallbackRecoder;
import com.ipd.jsf.registry.service.CallbackLogService;
import com.ipd.jsf.registry.service.PinpointService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.service.vo.PCNodeInfo;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.error.CallbackStubException;
import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.util.ExceptionUtils;

@Service
public class PinpointServiceImpl implements PinpointService {
    private Logger logger = LoggerFactory.getLogger(PinpointServiceImpl.class);
    
    @Autowired
    private SubscribeHelper subscribeHelper;

    @Autowired
    private CallbackLogService callbackLogServiceImpl;

    @Override
    public boolean pinpointCallbackProvider(String interfaceName, List<String> insKeyList) {
        try {
            if (interfaceName == null || insKeyList == null || insKeyList.size() == 0 || subscribeHelper.getInterfaceVo(interfaceName) == null) return false;
            Map<String, List<Server>> aliasServers = subscribeHelper.getInterfaceVo(interfaceName).aliasServersMap;
            //同一接口中，将各组的provider变化，通知给客户端
            for (Map.Entry<String, List<Server>> aliasServerEntry : aliasServers.entrySet()) {
                String alias = aliasServerEntry.getKey();
                List<Server> serverList = aliasServerEntry.getValue();
                // 遍历每个实例的callback
                for (String insKey : insKeyList) {
                    if (subscribeHelper.getInterfaceVo(interfaceName).getInsKeySet().contains(insKey)) {
                    	subscribeHelper.notifyInstanceToUpdateProviders(interfaceName, SubscribeUrl.PROVIDER_UPDATE_ALL, alias, serverList, insKey);
                    }
                }
            }
            logger.info("iface:{}, insKey:{}, aliasServers:{}", interfaceName, insKeyList, aliasServers);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean pinpointCallbackConfig(String ifaceName, List<String> insKeyList) {
        try {
            if (ifaceName == null || insKeyList == null || insKeyList.size() == 0 || subscribeHelper.getInterfaceVo(ifaceName) == null) return false;
            long configDataVersion = subscribeHelper.getInterfaceConfigDataVersion(ifaceName);
            JsfUrl jsfUrl = new JsfUrl();
            jsfUrl.setIface(ifaceName);
            jsfUrl.setDataVersion(configDataVersion);
            jsfUrl.setAlias(null);
            jsfUrl.setProtocol(0);
            SubscribeUrl url = new SubscribeUrl();
            url.setSourceUrl(jsfUrl);
            Map<String, String> config = new HashMap<String, String>();
            config.putAll(subscribeHelper.getInterfaceVo(ifaceName).propertyMap);
            for (String insKey : insKeyList) {
                if (subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet().contains(insKey)) {
                    if (subscribeHelper.isCallbackNull(insKey)) {
                        continue;
                    }
                    // 通知实例更新接口配置
                    subscribeHelper.notifyInstanceToUpdateConfig(ifaceName, config, url, insKey, SubscribeUrl.CONFIG_UPDATE);
                }
            }
            logger.info("iface:{}, insKey:{}, config:{}", ifaceName, insKeyList, config);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean pinpointCallbackInterfaceConfig(String interfaceName, List<String> insKeyList, final Map<String, String> attribute, boolean isProvider) {
        try {
            if (interfaceName == null || insKeyList == null || insKeyList.size() == 0 || subscribeHelper.getInterfaceVo(interfaceName) == null) return false;
            logger.info("iface:{}, insKey:{}, attribute:{}", interfaceName, insKeyList, attribute);
            long configDataVersion = subscribeHelper.getInterfaceConfigDataVersion(interfaceName);
            JsfUrl jsfUrl = new JsfUrl();
            jsfUrl.setIface(interfaceName);
            jsfUrl.setDataVersion(configDataVersion);
            jsfUrl.setAlias(null);
            jsfUrl.setProtocol(0);
            SubscribeUrl url = new SubscribeUrl();
            url.setSourceUrl(jsfUrl);
            int eventType = isProvider ? SubscribeUrl.ATTRIBUTE_P_UPDATE : SubscribeUrl.ATTRIBUTE_C_UPDATE;
            for (String insKey : insKeyList) {
                if (subscribeHelper.getInterfaceVo(interfaceName).getInsKeySet().contains(insKey)) {
                    if (subscribeHelper.isCallbackNull(insKey)) {
                        continue;
                    }
                    // 将接口的配置下发给实例
                    subscribeHelper.notifyInstanceToUpdateConfig(interfaceName, attribute, url, insKey, eventType);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public boolean pinpointCallbackInterfaceConfigExt(String interfaceName, List<PCNodeInfo> pcNodeInfoList, Map<String, String> attribute, boolean isProvider) {
        try {
            if (interfaceName == null || pcNodeInfoList == null || pcNodeInfoList.isEmpty() || subscribeHelper.getInterfaceVo(interfaceName) == null) return false;
            logger.info("iface:{}, attribute:{}, pcNodeInfoList:{}", interfaceName, attribute, pcNodeInfoList);
            long configDataVersion = subscribeHelper.getInterfaceConfigDataVersion(interfaceName);
            JsfUrl jsfUrl = new JsfUrl();
            jsfUrl.setIface(interfaceName);
            jsfUrl.setDataVersion(configDataVersion);
            SubscribeUrl url = new SubscribeUrl();
            url.setSourceUrl(jsfUrl);
            int eventType = isProvider ? SubscribeUrl.ATTRIBUTE_P_UPDATE : SubscribeUrl.ATTRIBUTE_C_UPDATE;
            String insKey = null;
            for (PCNodeInfo node : pcNodeInfoList) {
                insKey = node.getInsKey();
                if (insKey == null) {
                    insKey = UniqkeyUtil.getInsKey(node.getIp(), node.getPid(), node.getStTime());
                }
                if (subscribeHelper.getInterfaceVo(interfaceName).getInsKeySet().contains(insKey)) {
                    if (subscribeHelper.isCallbackNull(insKey)) {
                        continue;
                    }
                    // 将接口的配置下发给实例
                    jsfUrl.setIp(node.getIp());
                    jsfUrl.setAlias(node.getAlias());
                    jsfUrl.setProtocol(node.getProtocol());
                    jsfUrl.setInsKey(insKey);
                    jsfUrl.setPid(node.getPid());
                    jsfUrl.setIp(node.getIp());
                    subscribeHelper.notifyInstanceToUpdateConfig(interfaceName, attribute, url, insKey, eventType);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
        return true;
    }

    @Override
    public String consumerConfig(String interfaceName, String alias, int protocol, String insKey, byte type) {
        if (insKey == null) return null;
        SubscribeUrl subscribeUrl = new SubscribeUrl();
        long start = 0;
        try {
            JsfUrl jsfUrl = new JsfUrl();
            jsfUrl.setIface(interfaceName);
            jsfUrl.setAlias(alias);
            jsfUrl.setProtocol(protocol);
            subscribeUrl.setSourceUrl(jsfUrl);
            subscribeUrl.setType(subscribeHelper.getSubscribeType(type));
            if (subscribeUrl.getType() == -1) {
            	return "wrong type:" + type;
            }
            if (subscribeHelper.isCallbackNull(insKey) == false) {
                start = System.currentTimeMillis();
                String result = subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(subscribeUrl);
                CallbackRecoder.increaseCallbackCount();
                logger.info("interfaceName:{}, alias:{}, protocol:{}, insKey:{}, type:{}, callback result:{}", interfaceName, alias, protocol, insKey, type, result);
                //记录callback日志
                callbackLogServiceImpl.saveCallbackLog(interfaceName, insKey, null, CallbackLog.LOGTYPE_OTHER, subscribeUrl, (System.currentTimeMillis() - start));
                return result;
            }
        } catch (Exception e) {
            catchException(insKey, interfaceName, e, subscribeUrl, "consumer config notify error: ");
            return ExceptionUtils.toString(e);
        }
        return "no find callback";
    }


    @Override
    public String customCallback(String insKey, SubscribeUrl subscribeUrl) throws Exception {
    	logger.info("insKey:{}, subscribeUrl:{}", insKey, subscribeUrl);
        if (insKey == null || insKey.isEmpty() || subscribeUrl == null) return null;
        String ifaceName = "";
        if (subscribeUrl.getSourceUrl() != null && subscribeUrl.getSourceUrl().getIface() != null && !subscribeUrl.getSourceUrl().getIface().isEmpty()) {
            ifaceName = subscribeUrl.getSourceUrl().getIface();
        }
        int i = 2;
        long start = 0;
        while (i-- > 0) {
            try {
                if (!subscribeHelper.isCallbackNull(insKey)) {
                    start = System.currentTimeMillis();
                    //远程调用
                    String result = subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(subscribeUrl);
                    CallbackRecoder.increaseCallbackCount();
                    logger.info("notify {}, url: {}, result: {}", insKey, subscribeUrl.toString(), result);
                    //记录callback日志
                    callbackLogServiceImpl.saveCallbackLog("", insKey, null, CallbackLog.LOGTYPE_OTHER, subscribeUrl, (System.currentTimeMillis() - start));
                    return result;
                }
                logger.info("no callback , insKey:{}, subscribeUrl:{}", insKey, subscribeUrl);
                break;
            } catch (Exception e) {
            	catchException(insKey, ifaceName, e, subscribeUrl, "customCallback notify ");
            }
        }
        return null;
    }

    private void catchException(String insKey, String ifaceName, Exception e, SubscribeUrl subscribeUrl, String logStr) {
	    if (e instanceof CallbackStubException) {
	        logger.warn(logStr + insKey + " url:" + subscribeUrl.toString() + ", error:" + e.getMessage());
	        callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_STUB_EXCEPTION, subscribeUrl, 0);
	        subscribeHelper.handleCallbackException(insKey);
	    } else if (e instanceof ClientTimeoutException) {
	        logger.error(logStr + insKey + " url:" + subscribeUrl.toString() + ", error:" + e.getMessage(), e);
	        callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_TIMEOUT_EXCEPTION, subscribeUrl, 0);
	    	subscribeHelper.handleCallbackException(insKey);
	    } else {
	        CallbackRecoder.increaseCallbackFailTotalCount();
	        logger.error(logStr + insKey + " url:" + subscribeUrl.toString() + ", error:" + e.getMessage(), e);
	        callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_EXCEPTION, subscribeUrl, 0);
	    }
    }
}
