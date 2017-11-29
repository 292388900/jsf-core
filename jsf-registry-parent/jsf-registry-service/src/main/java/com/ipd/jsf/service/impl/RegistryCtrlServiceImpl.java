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
package com.ipd.jsf.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.pool.DruidDataSource;
import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.registry.context.RegistryContext;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.service.PinpointService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryCtrlService;
import com.ipd.jsf.service.vo.PCNodeInfo;
import com.ipd.jsf.vo.SubscribeUrl;

@Service
public class RegistryCtrlServiceImpl implements RegistryCtrlService {
    private static Logger logger = LoggerFactory.getLogger(RegistryCtrlServiceImpl.class);
    private final String umpAppLimitKey = "ump.applimit.key";
    private String umpKey = null;
    private long lastResetTime = 0;
    private DruidDataSource druidDataSource;
    
    @Autowired
    private SubscribeService subscribeService;

    @Autowired
    private PinpointService pinpointService;

    @PostConstruct
    public void init() {
        try {
            umpKey = (String) PropertyFactory.getProperty(umpAppLimitKey);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryCtrlService#refreshCache(java.lang.String)
     */
    @Override
    public boolean refreshCache(String interfaceName) {
        try {
            if (interfaceName == null || interfaceName.isEmpty()) {
                //强制刷新所有接口的服务列表缓存
                subscribeService.forceReloadAllProvider();
                subscribeService.forceReloadAllInterfaceConfig();
                return true;
            } else {
                InterfaceInfo iface = subscribeService.getByName(interfaceName);
                if (iface != null && iface.getInterfaceId() != 0) {
                    List<Integer> list = new ArrayList<Integer>();
                    list.add(iface.getInterfaceId());
                    //强制刷新指定接口的服务列表缓存
                    subscribeService.forceReloadProvider(list, true);
                    subscribeService.forceReloadInterfaceConfig(list, true);
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    @Override
    public boolean pinpointCallbackProvider(String interfaceName, List<String> insKeyList) {
        return pinpointService.pinpointCallbackProvider(interfaceName, insKeyList);
    }

    @Override
    public boolean pinpointCallbackConfig(String interfaceName, List<String> insKeyList) {
        return pinpointService.pinpointCallbackConfig(interfaceName, insKeyList);
    }

    @Override
    public boolean pinpointCallbackInterfaceConfig(String interfaceName, List<String> insKey, Map<String, String> attribute, boolean isProvider) {
        return pinpointService.pinpointCallbackInterfaceConfig(interfaceName, insKey, attribute, isProvider);
    }

    @Override
    public boolean pinpointCallbackInterfaceConfigExt(String interfaceName, List<PCNodeInfo> pcNodeInfoList, Map<String, String> attribute, boolean isProvider) {
        return pinpointService.pinpointCallbackInterfaceConfigExt(interfaceName, pcNodeInfoList, attribute, isProvider);
    }

    @Override
    public String consumerConfig(String interfaceName, String alias, int protocol, String insKey, byte type) {
        return pinpointService.consumerConfig(interfaceName, alias, protocol, insKey, type);
    }


    @Override
    public boolean updateProvider(String interfaceName, Map<String, String> provider, int type) {
        return  subscribeService.updateProvider(interfaceName, provider, type);
    }

    @Override
    public boolean updateInterfaceWBList(String interfaceName, Map<String, String> bwList, int type) {
        return  subscribeService.updateInterfaceWBList(interfaceName, bwList, type);
    }

    @Override
    public boolean insCtrl(String insKey, byte type) {
        return subscribeService.insCtrl(insKey, type);
    }

    /**
     * 指定InstanceKey自定义Callback
     * @param insKey
     *         InstanceKey
     * @param subscribeUrl
     *         自定义对象
     * @return callback返回值
     */
    @Override
    public String customCallback(String insKey, SubscribeUrl subscribeUrl) throws Exception {
        try {
            return pinpointService.customCallback(insKey, subscribeUrl);
        } catch (Exception e) {
            //如果是限流异常就报警
            if (subscribeUrl.getType() == SubscribeUrl.SWITCH_APP_LIMIT) {
                try {
                    logger.info("[JSF]限流通知异常. alarmkey:{}, insKey:{} ", umpKey, insKey);
                } catch (Exception e1) {
                    logger.error(e.getMessage(), e);
                }
            }
            throw e;
        }
    }

	@Override
	public String resetDruidDS() throws Exception {
		if (druidDataSource == null) {
			druidDataSource = (DruidDataSource)RegistryContext.context.getBean("dataSource");
		}
		int retry = 3;
		int i = 0;
		while (i++ < retry) {
			try {
	    		if (druidDataSource != null) {
	//    			if ((System.currentTimeMillis() - lastResetTime) < 300000) {
	//    				return "5分钟内已经重启了，不能再次重启";
	//    			}
	    			long start = System.currentTimeMillis();
	    			druidDataSource.close();
		    		druidDataSource.restart();
		    		lastResetTime = System.currentTimeMillis();
		    		logger.info("重新启动druid驱动，{}ms, reset druid++++++++++++++===================", (lastResetTime - start));
		    		break;
	    		}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				if (i >= retry) {
					return "请查看注册中心日志 " + RegistryUtil.getRegistryIP() + " ,重启：" + retry + "次，异常" + e.getMessage();
				}
				Thread.sleep(100);
			}
		}
		return "重启成功:" + RegistryUtil.getRegistryIP();
	}
}
