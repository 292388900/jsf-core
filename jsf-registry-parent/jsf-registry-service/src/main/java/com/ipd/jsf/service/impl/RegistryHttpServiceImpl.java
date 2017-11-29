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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.recoder.IpRequestHandler;
import com.ipd.jsf.registry.recoder.RequestRecoder;
import com.ipd.jsf.registry.service.InterfaceVisitService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryHttpService;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.LookupParam;
import com.ipd.jsf.vo.LookupResult;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.jsf.vo.HbResult;
import com.ipd.jsf.vo.Heartbeat;

/**
 * 注册中心对外发布服务
 */
@Service
public class RegistryHttpServiceImpl implements RegistryHttpService {
    private static Logger logger = LoggerFactory.getLogger(RegistryHttpServiceImpl.class);

    @Autowired
    private RegistryService registryServiceImpl;

    @Autowired
    private SubscribeService subscribeServiceImpl;

    @Autowired
    private SubscribeHelper subscribeHelper;

    @Autowired
    private InterfaceVisitService interfaceVisitServiceImpl;

    @Override
    public JsfUrl doRegister(JsfUrl jsfUrl) throws RpcException {
        return registryServiceImpl.doRegister(jsfUrl);
    }

    @Override
    public boolean doUnRegister(JsfUrl jsfUrl) throws RpcException {
        return registryServiceImpl.doUnRegister(jsfUrl);
    }

    /**
     * 获取server列表, 用于http协议
     * 判断接口版本号，如果版本号一致，返回null；如果版本号不一致且server列表为空，返回空list
     */
    @Override
    public LookupResult lookup(LookupParam param) throws RpcException {
        long start = System.currentTimeMillis();
        //验证数据是否正确
        validateLookup(param);

        LookupResult result = new LookupResult();
        try {
            String ifaceName = param.getIface();
            //如果版本号一致，providerList返回null
            long dataVersion = getDataVersion(ifaceName, param.getAlias());
            if (dataVersion == param.getDataVer()) {
                //生成返回结果
                result.setDataVer(dataVersion);
            } else {
                String jsfVersion = RegistryUtil.getValueFromMap(param.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
                String serialization = RegistryUtil.getValueFromMap(param.getAttrs(), RegistryConstants.SERIALIZATION, Constants.CodecType.msgpack.name());
                int appId = RegistryUtil.getIntValueFromMap(param.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE);
                //获取真实ip
                String clientIp = getClientIp(param.getIp());
                //获取provider列表
                List<JsfUrl> providerList = subscribeServiceImpl.subscribe(ifaceName, param.getAlias(), param.getProtocol(), jsfVersion, serialization, param.getInsKey(), clientIp, null, appId);
                //生成返回结果
                result.setList(RegistryUtil.getProviderUrlFromJsfUrl(providerList));
                result.setDataVer(dataVersion);
                logger.info("lookup is changed. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", param.getInsKey(), System.currentTimeMillis() - start, param.toString(), result.toString());
            }
            //请求计数
            RequestRecoder.recodeLookupTotalCount();
            //统计ip的订阅数量
            IpRequestHandler.subscribeRecord(param.getIp());
        } catch (Exception e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("lookup is failed. " + param.toString(), e);
        }
        return result;
    }

    /**
     * 验证 订阅参数
     * @param param
     * @throws RpcException
     */
    private void validateLookup(LookupParam param) throws RpcException {
        if(param == null) {
            handleException("validateLookup LookupParam is not correct. ");
        }
        if (param.getIface() == null || param.getIface().isEmpty()) {
            handleException("validateLookup LookupParam->iface is not correct. " + param.toString());
        }
        if (param.getInsKey() == null || param.getInsKey().isEmpty()) {
            handleException("validateLookup LookupParam->insKey is not correct. " + param.toString());
        }
        if(param.getAlias() == null || param.getAlias().isEmpty() || !RegistryUtil.checkAlias(param.getAlias())) {
            handleException("validateLookup, LookupParam->alias is not correct. " + param.toString());
        }
        if(param.getIp() == null || param.getIp().isEmpty()) {
            handleException("validateLookup, LookupParam->ip is not correct. " + param.toString());
        }
        if(param.getProtocol() <= 0) {
            handleException("validateLookup, LookupParam->protocol is not correct. " + param.toString());
        }
        //TODO 需要管理端增加数据录入，暂时先屏蔽此功能
        String visitorName = RegistryUtil.getValueFromMap(param.getAttrs(), RegistryConstants.VISITNAME, "");
        if (visitorName == null || interfaceVisitServiceImpl.check(param.getIface(), visitorName) == false) {
        	handleWarnException("validateLookup, LookupParam->visitname is not correct. " + param.toString());
        }
    }

    /**
     * 获取接口server列表版本号
     * @param interfaceName
     * @return
     */
    private long getDataVersion(String interfaceName, String alias) {
        try {
            return subscribeHelper.getInterfaceDataVersion(interfaceName, alias);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

    /**
     * @param ip
     * @return
     */
    private String getClientIp(String ip) {
        String clientIp = null;
        try {
            clientIp = RpcContext.getContext().getRemoteAddress().getAddress().getHostAddress();
        } catch (NullPointerException e) {
            clientIp = ip;
            return clientIp;
        }
        if (clientIp == null || "".equals(clientIp) || NetUtils.isLocalHost(clientIp)) {
            clientIp = ip;
        }
        return clientIp;
    }

    @Override
    public HbResult doHeartbeat(Heartbeat heartbeat) throws RpcException {
        return registryServiceImpl.doHeartbeat(heartbeat);
    }

    @Override
    public List<LookupResult> lookuplist(List<LookupParam> list) throws RpcException {
        long start = System.currentTimeMillis();
        validateLookupList(list);
        List<LookupResult> resultList = new ArrayList<LookupResult>();
        try {
            LookupResult result = null;
            String ifaceName;
            long dataVersion;
            long srcDataVersion;
            String jsfVersion;
            String serialization;
            int appId;
            String clientIp = null;
            List<JsfUrl> providerList;
            for (LookupParam param : list) {
                ifaceName = param.getIface();
                srcDataVersion = param.getDataVer();
                //如果版本号一致，providerList返回null
                dataVersion = getDataVersion(ifaceName, param.getAlias());
                result = new LookupResult();
                result.setIface(ifaceName);
                result.setAlias(param.getAlias());
                result.setDataVer(dataVersion);
                if (dataVersion != srcDataVersion) {    //版本号不一样，再返回providerlist
                    jsfVersion = RegistryUtil.getValueFromMap(param.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
                    serialization = RegistryUtil.getValueFromMap(param.getAttrs(), RegistryConstants.SERIALIZATION, Constants.CodecType.msgpack.name());
                    appId = RegistryUtil.getIntValueFromMap(param.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE);
                    //获取真实ip
                    clientIp = getClientIp(param.getIp());
                    //获取provider列表
                    providerList = subscribeServiceImpl.subscribe(ifaceName, param.getAlias(), param.getProtocol(), jsfVersion, serialization, param.getInsKey(), clientIp, null, appId);
                    //生成返回结果
                    result.setList(RegistryUtil.getProviderUrlFromJsfUrl4List(providerList));
                    logger.info("lookupList dataversion is changed. insKey:{}, elapse:{}, srcDataVersion:{}, resultUrl:{}", param.getInsKey(), System.currentTimeMillis() - start, srcDataVersion, result.toString());
                }
                resultList.add(result);
            }
            //请求计数
            RequestRecoder.recodeLookupTotalCount();
            //统计ip的订阅数量
            IpRequestHandler.subscribeRecord(clientIp);
        } catch (Exception e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("lookupList is failed. " + list.toString(), e);
        }
        return resultList;
    }

    /**
     * 验证 订阅参数
     * @param list
     * @throws RpcException
     */
    private void validateLookupList(List<LookupParam> list) throws RpcException {
        if(list == null || list.isEmpty()) {
            handleException("validateLookupList JsfUrl list is not correct. ");
        }
        for (LookupParam param : list) {
            if(param == null) {
                handleException("validateLookup LookupParam is not correct. ");
            }
            if (param.getIface() == null || param.getIface().isEmpty()) {
                handleException("validateLookup LookupParam->iface is not correct. " + param.toString());
            }
            if (param.getInsKey() == null || param.getInsKey().isEmpty()) {
                handleException("validateLookup LookupParam->insKey is not correct. " + param.toString());
            }
            if(param.getAlias() == null || param.getAlias().isEmpty() || !RegistryUtil.checkAlias(param.getAlias())) {
                handleException("validateLookup, LookupParam->alias is not correct. " + param.toString());
            }
            if(param.getIp() == null || param.getIp().isEmpty()) {
                handleException("validateLookup, LookupParam->ip is not correct. " + param.toString());
            }
            if(param.getProtocol() <= 0) {
                handleException("validateLookup, LookupParam->protocol is not correct. " + param.toString());
            }
        }
    }

    private void handleException(String message, Throwable t) throws RpcException {
        logger.error(message, t);
        throw new RpcException(RegistryUtil.messageHandler(message));
    }

    private void handleException(String message) throws RpcException {
        logger.error(message);
        throw new RpcException(RegistryUtil.messageHandler(message));
    }

    private void handleWarnException(String message) throws RpcException {
    	logger.warn(message);
    	throw new RpcException(RegistryUtil.messageHandler(message));
    }

}
