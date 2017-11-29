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

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.common.enumtype.ServerOptType;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.recoder.RequestRecoder;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

public class RegisterHelper {

	private static Logger logger = LoggerFactory.getLogger(RegisterHelper.class);
    public static String PROVIDER_VALUE = "0";
    public static String CONSUMER_VALUE_SINGLE = "1";
    public static String CONSUMER_VALUE_GROUP = "2";

    /**
     * 是否开启JST(分布式跟踪)
     */
    public static final String CG_OPEN = "cg.on";

    /**
     * 是否开启JST(分布式跟踪)增强
     */
    public static final String CG_ENHANCE = "cg.eh";

    /**
     * 将jsfUrl转为Instance对象
     * @param jsfUrl
     * @return
     */
    public static JsfIns getInsFromJsfUrl(JsfUrl jsfUrl, Date date, int room) {
        JsfIns instance = new JsfIns();
        instance.setInsKey(jsfUrl.getInsKey());
        instance.setIp(jsfUrl.getIp());
        instance.setPort(jsfUrl.getPort());
        instance.setPid(jsfUrl.getPid());
        instance.setStartTime(jsfUrl.getStTime());
        instance.setSafVer(RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_IVALUE));
        instance.setLanguage(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.LANGUAGE, ""));
        instance.setInsRoom(room);
        instance.setAppName(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPNAME, RegistryConstants.APPNAME_VALUE));
        instance.setAppId(RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE));
        instance.setAppInsId(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPINSID, RegistryConstants.APPINSID_VALUE));
        instance.setHb(date);
        instance.setCreateTime(date);
        instance.setRegIp(RegistryUtil.getRegistryIPPort());
        //callgraph
        String cgOpen = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), CG_OPEN, null);
        if (null != cgOpen) {
            instance.setCgOpen(Integer.valueOf(cgOpen));
        }
        jsfUrl.getAttrs().remove(CG_OPEN);
        String cgEnhance = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), CG_ENHANCE, null);
        if (null != cgEnhance) {
            instance.setCgEnhance(Integer.valueOf(cgEnhance));
        }
        jsfUrl.getAttrs().remove(CG_ENHANCE);
        return instance;
    }

    /**
     * 将jsfUrl转为server对象
     * @param jsfUrl
     * @param iface
     * @return
     */
    public static Server getServerFromRegister(JsfUrl jsfUrl, InterfaceInfo iface, Date date, int room) {
        String appPath = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPPATH, "");
        String dynamic = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.DYNAMIC, "");
        int safVer = RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.SAFVERSION, RegistryConstants.SAFVERSION_VALUE);
        boolean reReg = "true".equals(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.REREG, ""));
        // 如果是http或rest，获取context
        String protocolContext = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONTEXTPATH, "");
        int weight = 0;
        Map<String, String> attrs = jsfUrl.getAttrs();
        if (attrs != null && attrs.get(RegistryConstants.WEIGHT) != null) {
            weight = Integer.parseInt(attrs.get(RegistryConstants.WEIGHT));
        }
        Server server = new Server();
        server.setIp(jsfUrl.getIp());
        server.setPort(jsfUrl.getPort());
        server.setPid(jsfUrl.getPid());
        server.setInterfaceId(iface.getInterfaceId());
        server.setInterfaceName(iface.getInterfaceName());
        server.setAlias(jsfUrl.getAlias());
        server.setProtocol(jsfUrl.getProtocol());
        server.setTimeout(jsfUrl.getTimeout());
        server.setWeight(weight);
        server.setRandom(jsfUrl.isRandom());
        server.setSrcType(SourceType.registry.value());
        server.setInsKey(jsfUrl.getInsKey());
        server.setSafVer(safVer);
        server.setAppPath(appPath);
        server.setContextPath(protocolContext);
        try {
            //去除多余字段后，将attrs转为string放入server中
            server.setAttrUrl(RegistryUtil.copyEntries(
                            jsfUrl.getAttrs(),
                            false,
                            Arrays.asList(new String[] {
                                    RegistryConstants.APPPATH,
                                    RegistryConstants.SAFVERSION,
                                    RegistryConstants.CONTEXTPATH,
                                    RegistryConstants.WEIGHT })).toString());
        } catch (Exception e) {
        	logger.error(e.getMessage(), e);
        }
        try {
        	server.setOptType(ServerOptType.online.value());
            //如果dynamic为false, 并且是第一次上线，就将状态设置为下线
            if ("false".equals(dynamic) && !reReg) {
                server.setOptType(ServerOptType.offline.value());
            }
	    } catch (Exception e) {
	    	logger.error(e.getMessage(), e);
	    }
        server.setRoom(room);
        server.setStartTime(jsfUrl.getStTime());
        server.setUrlDesc(jsfUrl.toString());
        server.setCreateTime(date);
        server.setUpdateTime(date);
        server.setUniqKey(UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId()));
        server.setReReg(reReg);
        return server;
    }

    /**
     * 将jsfurl转为client对象
     * @param jsfUrl
     * @param iface
     * @return
     */
    public static Client getClientFromRegister(JsfUrl jsfUrl, InterfaceInfo iface, Date date) {
        String appPath = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPPATH, "");
        int safVer = RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.SAFVERSION, RegistryConstants.SAFVERSION_VALUE);
        Client client = new Client();
        client.setAppPath(appPath);
        client.setIp(jsfUrl.getIp());
        client.setSafVer(safVer);
        client.setInterfaceId(iface.getInterfaceId());
        client.setInterfaceName(iface.getInterfaceName());
        client.setPid(jsfUrl.getPid());
        client.setAlias(jsfUrl.getAlias());
        client.setProtocol(jsfUrl.getProtocol());
        client.setSrcType(SourceType.registry.value());
        client.setInsKey(jsfUrl.getInsKey());
        client.setUrlDesc(jsfUrl.toString());
        client.setStartTime(jsfUrl.getStTime());
        client.setCreateTime(date);
        client.setUpdateTime(date);
        client.setUniqKey(UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), client.getAlias(), client.getProtocol(), client.getInterfaceId()));
        if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, PROVIDER_VALUE).equals(CONSUMER_VALUE_SINGLE)) {
        	client.setcType(Integer.valueOf(CONSUMER_VALUE_SINGLE).intValue());
        } else if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, PROVIDER_VALUE).equals(CONSUMER_VALUE_GROUP)) {
        	client.setcType(Integer.valueOf(CONSUMER_VALUE_GROUP).intValue());
        }
        client.setcId(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMERID, ""));
        return client;
    }

    /**
     * 将jsfUrl转为server对象
     * @param jsfUrl
     * @param ifaceInfo
     * @param date
     * @return
     */
    public static Server getServerFromUnregister(JsfUrl jsfUrl, InterfaceInfo ifaceInfo, Date date) {
    	Server server = new Server();
    	server.setIp(jsfUrl.getIp());
    	server.setPort(jsfUrl.getPort());
    	server.setPid(jsfUrl.getPid());
    	server.setInterfaceId(ifaceInfo.getInterfaceId());
    	server.setInterfaceName(ifaceInfo.getInterfaceName());
    	server.setProtocol(jsfUrl.getProtocol());
    	server.setAlias(jsfUrl.getAlias());
    	server.setInsKey(jsfUrl.getInsKey());
    	server.setUpdateTime(date);
    	server.setStartTime(jsfUrl.getStTime());
    	server.setUniqKey(UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId()));
    	return server;
    }

    /**
     * 将jsfurl转为client对象
     * @param jsfUrl
     * @param ifaceInfo
     * @param date
     * @return
     */
    public static Client getClientFromUnregister(JsfUrl jsfUrl, InterfaceInfo ifaceInfo, Date date) {
	    Client client = new Client();
	    client.setInterfaceId(ifaceInfo.getInterfaceId());
	    client.setInterfaceName(ifaceInfo.getInterfaceName());
	    client.setIp(jsfUrl.getIp());
	    client.setPid(jsfUrl.getPid());
	    client.setAlias(jsfUrl.getAlias());
	    client.setProtocol(jsfUrl.getProtocol());
	    client.setUpdateTime(date);
	    client.setStartTime(jsfUrl.getStTime());
	    client.setUniqKey(UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), client.getAlias(), client.getProtocol(), client.getInterfaceId()));
	    return client;
    }

    /**
     * 记录失败次数
     */
    public static void registerFailRecord() {
        RequestRecoder.recodeRequestFailTotalCount();
    }

    /**
     * 记录失败次数
     */
    public static void unregisterFailRecord() {
        RequestRecoder.recodeRequestFailTotalCount();
    }

    /**
     * @param isProvider
     * @throws Exception
     */
    public static void afterUnregistry(boolean isProvider) throws Exception {
    	try {
    		if (isProvider) {
                //请求计数
                RequestRecoder.recodeProviderUnRegistryTotalCount();
                //刷新缓存
//                List<Integer> list = new ArrayList<Integer>();
//                list.add(ifaceInfo.getInterfaceId());
//                subscribeServiceImpl.forceReloadProvider(list, true);
	        } else {
	            //请求计数
	            RequestRecoder.recodeConsumerUnRegistryTotalCount();
	        }
    	} catch (Exception e) {
    		logger.error("force to reload providers after registry, " + e.getMessage(), e);
    	}
    }

    /**
     * @param jsfUrl
     * @param providerList
     * @return
     */
    public static SubscribeUrl getSubscribeUrl(JsfUrl jsfUrl, List<JsfUrl> providerList) {
        SubscribeUrl url = new SubscribeUrl();
        url.setSourceUrl(jsfUrl);
        url.setProviderList(providerList);
        url.setType(SubscribeUrl.PROVIDER_UPDATE_ALL);
        return url;
    }

    /**
     * 克隆
     * @param jsfUrl
     * @return
     */
    public static JsfUrl cloneJsfUrl(JsfUrl jsfUrl) {
    	//如果是相同ip就克隆，避免injvm的属性覆盖
    	if (jsfUrl.getIp().equals(RegistryUtil.getRegistryIP())) {
	        JsfUrl result = new JsfUrl();
	        result.setAlias(jsfUrl.getAlias());
	        result.setDataVersion(jsfUrl.getDataVersion());
	        result.setIface(jsfUrl.getIface());
	        result.setInsKey(jsfUrl.getInsKey());
	        result.setIp(jsfUrl.getIp());
	        result.setPid(jsfUrl.getPid());
	        result.setPort(jsfUrl.getPort());
	        result.setProtocol(jsfUrl.getProtocol());
	        result.setRandom(jsfUrl.isRandom());
	        result.setStTime(jsfUrl.getStTime());
	        result.setTimeout(jsfUrl.getTimeout());
	        if (jsfUrl.getAttrs() != null) {
	            Map<String, String> map = new HashMap<String, String>();
	            map.putAll(jsfUrl.getAttrs());
	            result.setAttrs(map);
	        }
	        return result;
    	} else {
    		return jsfUrl;
    	}
    }

}
