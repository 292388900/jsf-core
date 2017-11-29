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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.callback.SubscribeCallback;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.recoder.ConnectionRecoder;
import com.ipd.jsf.registry.recoder.IpRequestHandler;
import com.ipd.jsf.registry.recoder.RequestRecoder;
import com.ipd.jsf.registry.service.AppIfaceInvokeService;
import com.ipd.jsf.registry.service.ClientService;
import com.ipd.jsf.registry.service.ConfigService;
import com.ipd.jsf.registry.service.HeartbeatService;
import com.ipd.jsf.registry.service.PinpointService;
import com.ipd.jsf.registry.service.RegisterIpLimitService;
import com.ipd.jsf.registry.service.ServerService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.threadpool.WorkerThreadPool;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.vo.HbResult;
import com.ipd.jsf.vo.Heartbeat;

/**
 * 注册中心对外发布服务
 */
@Service
public class RegistryServiceImpl implements RegistryService {
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceImpl.class);

    //通知线程池
    private WorkerThreadPool eventThreadPool = new WorkerThreadPool(6, 6, "reg-event", 50000);

    @Autowired
    private ServerService serverServiceImpl;

    @Autowired
    private SubscribeHelper subscribeHelper;

    @Autowired
    private ClientService clientServiceImpl;

    @Autowired
    private HeartbeatService heartbeatServiceImpl;

    @Autowired
    private SubscribeService subscribeServiceImpl;

    @Autowired
    private ConfigService configServiceImpl;

    @Autowired
    private RegisterIpLimitService registerIpLimitService;

    @Autowired
    private AppIfaceInvokeService appIfaceInvokeServiceImpl;

    @Autowired
    private PinpointService pinpointService;
    
    @PostConstruct
    public void init() {
        try {
            Object tmp = PropertyFactory.getProperty("env.istest");
            RegistryUtil.isTest = (tmp == null) ? false : Boolean.parseBoolean(String.valueOf(tmp));
            logger.info("environment isTest is : {}", RegistryUtil.isTest);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 注册服务节点，包括：provider和consumer
     */
    @Override
    public JsfUrl doRegister(JsfUrl jsfUrl) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateRegister(jsfUrl);
            checkIpLimit(jsfUrl);
        } catch (Exception e) {
            throw e;
        }

        try {
        	InterfaceInfo ifaceInfo = checkInterface(jsfUrl, "doRegister");
            //填补下inskey
            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
                jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
            }
            //检查jsf版本
            checkVersion(jsfUrl);
            Date date = DateTimeZoneUtil.getTargetTime();
            JsfIns ins = RegisterHelper.getInsFromJsfUrl(jsfUrl, date, configServiceImpl.getRoomByIp(jsfUrl.getIp()));
            //保存实例信息
            heartbeatServiceImpl.register(ins);
            if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, RegisterHelper.PROVIDER_VALUE).equals(RegisterHelper.PROVIDER_VALUE)) {
            	Server server = RegisterHelper.getServerFromRegister(jsfUrl, ifaceInfo, date, configServiceImpl.getRoomByIp(jsfUrl.getIp()));
            	serverServiceImpl.registerServer(server, ins);
            	//请求计数
            	RequestRecoder.recodeProviderRegistryCount();
            } else {
                Client client = RegisterHelper.getClientFromRegister(jsfUrl, ifaceInfo, date);
                //保存consumer到db
                clientServiceImpl.registerClient(client, ins);
                //请求计数
                RequestRecoder.recodeConsumerRegistryCount();
            }

            //给客户端返回interfaceId
            jsfUrl.getAttrs().put("ifaceId", String.valueOf(ifaceInfo.getInterfaceId()));
            //统计ip注册数量
            IpRequestHandler.registryRecord(getClientIp(jsfUrl.getIp()));
            //保存连接信息
            putConnection(jsfUrl);
        } catch (RpcException e) {
            RegisterHelper.registerFailRecord();
            throw e;
        } catch (InitErrorException e) {
            RegisterHelper.registerFailRecord();
            throw e;
        } catch (Exception e) {
            RegisterHelper.registerFailRecord();
            handleException("doRegister is failed." + jsfUrl.toString(), e);
        }

        long end = System.currentTimeMillis();
        logger.info("doRegister is successful. insKey:{} . elapse: {}ms . {}", jsfUrl.getInsKey(), (end - start), jsfUrl.toString());
        return jsfUrl;
    }

    /**
     * 检查jsf版本。对新注册的接口，需要升级到最新版本的JSF客户端
     * @param jsfUrl
     */
    private void checkVersion(JsfUrl jsfUrl) {
		//注册中心自己不做验证
		if (isMySelf(jsfUrl.getIp(), jsfUrl.getPid())) {
			return;
		}
		try {
			onlineEnvCheckVersion(jsfUrl);
		} catch (Exception e) {
			//这里只输出异常，不做其他处理
			logger.error(e.getMessage(), e);
		}
    }

	/**
	 * 对线上环境检查jsf版本
	 * 测试环境
	 * @param jsfUrl
	 */
	private void onlineEnvCheckVersion(final JsfUrl jsfUrl) {
		if (!"true".equals(RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.REREG, ""))) { //如果是第一次注册
		    //获取客户端的jsf版本
		    final String jsfVer = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
		    int clientJsfVer = Integer.parseInt(jsfVer);
		    //比较客户端的jsf版本和最新的版本号
		    if (clientJsfVer < RegistryUtil.jsfVersionInt) {
		    	try {
		    		eventThreadPool.execute(new Runnable() {
			            @Override
			            public void run() {
			                try {
			                    SubscribeUrl url = new SubscribeUrl();
			                    url.setType(SubscribeUrl.INSTANCE_NOTIFICATION);
			                    url.setSourceUrl(new JsfUrl());
			                    url.getSourceUrl().setAttrs(new HashMap<String, String>());
			                    url.getSourceUrl().getAttrs().put("msg", "来自注册中心的提示: 您当前应用的JSF版本是" + jsfVer + ", 建议升级到最新版本" + RegistryUtil.jsfVersionString + "。");
			                    url.getSourceUrl().getAttrs().put("level", "warn");
			                    pinpointService.customCallback(jsfUrl.getInsKey(), url);
			                } catch (Exception e) {
			                    logger.error("jsfUrl:" + jsfUrl.toString() + ",error:" + e.getMessage(), e);
			                }
			            }
			        });
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
		    }
		}
	}

    /**
     * 检查下是不是自己
     * @param ip
     * @param pid
     * @return
     */
    private boolean isMySelf(String ip, int pid) {
        if (RegistryUtil.getRegistryIP().equals(ip) && RegistryUtil.getPid() == pid) {
            return true;
        }
        return false;
    }

    /**
     * 验证注册参数
     * @param jsfUrl
     * @throws RpcException
     */
    private void validateRegister(JsfUrl jsfUrl) throws RpcException {
        if (jsfUrl == null) {
            handleException("validateRegistry, JsfUrl is null. ");
        }
        if (jsfUrl.getAlias() == null || jsfUrl.getAlias().isEmpty() || !RegistryUtil.checkAlias(jsfUrl.getAlias())) {
            handleException("validateRegistry, JsfUrl->alias is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getAlias().length() > RegistryConstants.MAX_ALIAS_LENGTH) {
            handleException("validateRegistry, JsfUrl->alias's length too long, alias max length = " + RegistryConstants.MAX_ALIAS_LENGTH + ". " + jsfUrl.toString());
        }
        if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
            handleException("validateRegistry, JsfUrl->iface is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
            handleException("validateRegistry, JsfUrl->ip is not correct. " + jsfUrl.toString());
        }
        if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, RegisterHelper.PROVIDER_VALUE).equals(RegisterHelper.PROVIDER_VALUE)) { //如果是provider,验证port不为0
        	if (jsfUrl.getPort() == 0) {
        		handleException("validateRegistry, JsfUrl->port is not correct. " + jsfUrl.toString());
        	}
        }
        if (jsfUrl.getStTime() == 0L) {
            handleException("validateRegistry, JsfUrl->sttime is not correct. " + jsfUrl.toString());
        }
        checkIp(jsfUrl.getIp(), "validateRegister");
    }

    private InterfaceInfo checkInterface(JsfUrl jsfUrl, String method) {
    	try {
        	InterfaceInfo ifaceInfo = subscribeServiceImpl.getByName(jsfUrl.getIface());
        	if (ifaceInfo == null || ifaceInfo.getInterfaceId() == 0
        			|| ifaceInfo.getInterfaceName() == null
        			|| ifaceInfo.getInterfaceName().isEmpty()) {
        		handleInitException(method + ": " + jsfUrl.getIface() + "接口未注册，请登录JSF管理端录入接口，并提交审核。" + jsfUrl.toString());
        	}
        	return ifaceInfo;
		} catch (InitErrorException e) {
			throw e;
		} catch (Exception e) {
        	RegisterHelper.registerFailRecord();
            handleException(method + " is failed." + jsfUrl.toString(), e);
        }
    	return null;
    }

    private void checkIpLimit(JsfUrl jsfUrl) {
    	//ip  访问限制
        if (!registerIpLimitService.checkIpVisitLimit(jsfUrl.getIp())) {
            handleException("validateRegistry, JsfUrl->ip exceed register limit. 5分钟内超过访问次数上限，请稍候重试. " + jsfUrl.toString());
        }
    }

    /**
     * 验证ip是否合法; true-- 合法，false--非法
     * @param ip
     * @param msg
     */
    private boolean checkIp(String ip, String msg) {
        try {
            if (RegistryUtil.isTest) {
                return true;
            }
            if (RegistryUtil.getRegistryIP().equals(ip)) {
                return true;
            }
            String jsfContextIp = RpcContext.getContext().getRemoteAddress().getAddress().getHostAddress();
            if (ip == null) {   //非法
                logger.warn("jsfUrl->ip is null, JsfContext->remoteIp:{}", jsfContextIp);
                return false;
            } else if (!ip.equals(jsfContextIp)) {   //非法
                logger.warn("{}, jsfUrl ip and remote ip are not same. jsfUrl->ip:{}, JsfContext->remoteIp:{}", msg, ip, jsfContextIp);
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return true;
    }

    /**
     * 记录连接串和对应的实例
     * @param jsfUrl
     */
    private void putConnection(JsfUrl jsfUrl) {
        JsfIns ins = new JsfIns();
        ins.setIp(jsfUrl.getIp());
        ins.setPid(jsfUrl.getPid());
        ins.setStartTime(jsfUrl.getStTime());
        ins.setInsKey(jsfUrl.getInsKey());
        ConnectionRecoder.setConnInsMap(NetUtils.toAddressString(RpcContext.getContext().getRemoteAddress()), ins);
    }

    @Override
    public boolean doUnRegister(JsfUrl jsfUrl) throws RpcException {
        try {
            validateUnRegister(jsfUrl);
        } catch (Exception e) {
            throw e;
        }
        long start = System.currentTimeMillis();
        try {
            InterfaceInfo ifaceInfo = checkInterface(jsfUrl, "doUnRegister");
            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
                jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
            }
            Date date = DateTimeZoneUtil.getTargetTime();
            JsfIns ins = RegisterHelper.getInsFromJsfUrl(jsfUrl, date, configServiceImpl.getRoomByIp(jsfUrl.getIp()));
            if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, RegisterHelper.PROVIDER_VALUE).equals(RegisterHelper.PROVIDER_VALUE)) {
            	Server server = RegisterHelper.getServerFromUnregister(jsfUrl, ifaceInfo, date);
            	serverServiceImpl.unRegisterServer(server, ins);
            	RegisterHelper.afterUnregistry(true);
            } else {
            	Client client = RegisterHelper.getClientFromUnregister(jsfUrl, ifaceInfo, date);
                clientServiceImpl.unRegisterClient(client, ins);
                subscribeServiceImpl.unRegistryConsumer(jsfUrl.getIface(), jsfUrl.getAlias(), jsfUrl.getProtocol(), jsfUrl.getInsKey());
                RegisterHelper.afterUnregistry(false);
            }
            long end = System.currentTimeMillis();
            logger.info("doUnRegister is successful. insKey:{} . elapse: {}ms . jsfUrl: {}", jsfUrl.getInsKey(), (end - start), jsfUrl.toString());
            return true;
        } catch (RpcException e) {
            RegisterHelper.unregisterFailRecord();
            handleException("doUnRegister is failed. " + jsfUrl.toString(), e);
        } catch (Exception e) {
            RegisterHelper.unregisterFailRecord();
            handleException("doUnRegister is failed. " + jsfUrl.toString(), e);
        }
        return false;
    }

    /**
     * 验证取消注册的参数
     * @param jsfUrl
     * @throws RpcException
     */
    private void validateUnRegister(JsfUrl jsfUrl) throws RpcException {
        if (jsfUrl == null) {
            handleException("validateUnRegister, JsfUrl is not correct. ");
        }
        if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
            handleException("validateUnRegister, JsfUrl->iface is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
        	if (jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
                handleException("validateUnRegister, JsfUrl->ip is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getPid() <= 0) {
                handleException("validateUnRegister, JsfUrl->pid is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getStTime() <= 0) {
                handleException("validateUnRegister, JsfUrl->starttime is not correct. " + jsfUrl.toString());
            }
        }
        if (jsfUrl.getAlias() == null || jsfUrl.getAlias().isEmpty() || !RegistryUtil.checkAlias(jsfUrl.getAlias())) {
            handleException("validateUnRegister, JsfUrl->alias is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getProtocol() == ProtocolType.consumer.value()) {
            handleException("validateUnRegister, JsfUrl->protocol is not correct. " + jsfUrl.toString());
        }
        if (!checkIp(jsfUrl.getIp(), "validateUnRegister")) {
            handleException("validateUnRegister checkIp, JsfUrl->ip is not correct. " + jsfUrl.toString());
        }
    }

    @Override
    public SubscribeUrl doSubscribe(JsfUrl jsfUrl, Callback<SubscribeUrl, String> subscribeData) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateSubscribe(jsfUrl);
        } catch (Exception e) {
            throw e;
        }
        try {
            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
                jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
            }
            String ifaceName = jsfUrl.getIface();
            String alias = jsfUrl.getAlias();
            int protocol = jsfUrl.getProtocol();
            String jsfVersion = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
            String serialization = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.SERIALIZATION, Constants.CodecType.msgpack.name());
            int appId = RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE);
            //app访问接口权限限制，只在doSubscribe做验证
            if (!appIfaceInvokeServiceImpl.check(ifaceName, appId)) {
                handleInitException("doSubscribe is failed. app访问接口权限限制，请找接口负责人授权。" + jsfUrl.toString());
            }
            //获取真实的连接ip
        	String clientIp = getClientIp(jsfUrl.getIp());
        	SubscribeCallback<SubscribeUrl> callback = new SubscribeCallback<SubscribeUrl>(subscribeData, clientIp);
        	callback.setClientIp(clientIp);
        	//获取provider列表
            List<JsfUrl> providerList = subscribeServiceImpl.subscribe(ifaceName, alias, protocol, jsfVersion, serialization, jsfUrl.getInsKey(), clientIp, callback, appId);
            //获取版本号
            long dataVersion = getDataVersion(jsfUrl.getIface(), jsfUrl.getAlias());
            jsfUrl.setDataVersion(dataVersion);
            //生成返回结果
            SubscribeUrl url = RegisterHelper.getSubscribeUrl(jsfUrl, providerList);
            //请求计数
            RequestRecoder.recodeSubscribeTotalCount();
            //统计ip的订阅数量
            IpRequestHandler.subscribeRecord(getClientIp(jsfUrl.getIp()));
            logger.info("doSubscribe is successful. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", jsfUrl.getInsKey(), System.currentTimeMillis() - start, jsfUrl.toString(), url.toString());
            return url;
        } catch (InitErrorException e) {
            RequestRecoder.recodeRequestFailTotalCount();
            throw e;
        } catch (Exception e) {
            //请求失败加1
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("doSubscribe is failed. " + jsfUrl.toString(), e);
        }
        return null;
    }

    /**
     * 验证 订阅参数
     * @param jsfUrl
     * @throws RpcException
     */
    private void validateSubscribe(JsfUrl jsfUrl) throws RpcException {
        if(jsfUrl == null) {
            handleException("validateSubscribe JsfUrl is not correct. ");
        }
        if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
            handleException("validateSubscribe JsfUrl->iface is not correct. " + jsfUrl.toString());
        }
        if(jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
        	handleException("validateSubscribe, JsfUrl->ip is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
            if (jsfUrl.getPid() == 0) {
                handleException("validateSubscribe JsfUrl->pid is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getStTime() == 0) {
                handleException("validateSubscribe JsfUrl->starttime is not correct. " + jsfUrl.toString());
            }
        }
        if(jsfUrl.getAlias() == null || jsfUrl.getAlias().isEmpty() || !RegistryUtil.checkAlias(jsfUrl.getAlias())) {
            handleException("validateSubscribe, JsfUrl->alias is not correct. " + jsfUrl.toString());
        }
        if(jsfUrl.getProtocol() <= 0) {
            handleException("validateSubscribe, JsfUrl->protocol is not correct. " + jsfUrl.toString());
        }
    }

    @Override
    public boolean doUnSubscribe(JsfUrl jsfUrl) throws RpcException {
        try {
            validateUnSubscribe(jsfUrl);
        } catch (Exception e) {
            throw e;
        }
        try {
        	if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
                jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
            }
            subscribeServiceImpl.unSubscribe(jsfUrl.getIface(), jsfUrl.getInsKey());
            //请求计数
            RequestRecoder.recodeUnSubscribeTotalCount();
            if (logger.isDebugEnabled()) {
                logger.debug("doUnSubscribe is successful. {}", jsfUrl.toString());
            }
            return true;
        } catch (Exception e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("doUnSubscribe is error ," + jsfUrl.toString(), e);
        }
        return false;
    }

    /**
     * 验证取消订阅参数
     * @param jsfUrl
     * @throws RpcException
     */
    private void validateUnSubscribe(JsfUrl jsfUrl) throws RpcException {
        if(jsfUrl == null) {
            handleException("validateUnSubscribe JsfUrl is not correct. ");
        }
        if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
            handleException("validateUnSubscribe JsfUrl->iface is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
            if (jsfUrl.getPid() == 0) {
                handleException("validateUnSubscribe JsfUrl->pid is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getStTime() == 0) {
                handleException("validateUnSubscribe JsfUrl->starttime is not correct. " + jsfUrl.toString());
            }
        }
    }

    /**
     * 获取server列表
     * 判断接口版本号，如果版本号一致，返回null；如果版本号不一致且server列表为空，返回空list
     * 
     */
    @Override
    public SubscribeUrl lookup(JsfUrl jsfUrl) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateSubscribe(jsfUrl);
        } catch (Exception e) {
            throw e;
        }
        SubscribeUrl url = null;
        try {
            String ifaceName = jsfUrl.getIface();
            //如果版本号一致，providerList返回null
            long srcDataVersion = jsfUrl.getDataVersion();
            long dataVersion = getDataVersion(ifaceName, jsfUrl.getAlias());
            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
            	jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
            }
            if (dataVersion == srcDataVersion) {
                //生成返回结果
                url = RegisterHelper.getSubscribeUrl(jsfUrl, null);
            } else {
                String jsfVersion = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
                String serialization = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.SERIALIZATION, Constants.CodecType.msgpack.name());
                int appId = RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE);
                //获取真实ip
            	String clientIp = getClientIp(jsfUrl.getIp());
            	//获取provider列表
                List<JsfUrl> providerList = subscribeServiceImpl.subscribe(ifaceName, jsfUrl.getAlias(), jsfUrl.getProtocol(), jsfVersion, serialization, jsfUrl.getInsKey(), clientIp, null, appId);
                jsfUrl.setDataVersion(dataVersion);
                //生成返回结果
                url = RegisterHelper.getSubscribeUrl(jsfUrl, providerList);
                logger.info("lookup dataversion is changed. insKey:{}, elapse:{}, srcDataVersion:{}, resultUrl:{}", jsfUrl.getInsKey(), System.currentTimeMillis() - start, srcDataVersion, url.toString());
            }
            //请求计数
            RequestRecoder.recodeLookupTotalCount();
            //统计ip的订阅数量
            IpRequestHandler.subscribeRecord(getClientIp(jsfUrl.getIp()));
        } catch (Exception e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("lookup is failed. " + jsfUrl.toString(), e);
        }
        return url;
    }

    /**
     * 获取server列表
     * 判断接口版本号，如果版本号一致，返回null；如果版本号不一致且server列表为空，返回空list
     */
    @Override
    public List<SubscribeUrl> lookupList(List<JsfUrl> list) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateLookupList(list);
        } catch (Exception e) {
            throw e;
        }
        List<SubscribeUrl> urlList = new ArrayList<SubscribeUrl>();
        try {
            SubscribeUrl url = null;
            String ifaceName = null;
            long dataVersion = 0;
            long srcDataVersion = 0;
            String jsfVersion = null;
            String serialization = null;
            int appId = 0;
            String clientIp = null;
            List<JsfUrl> providerList = null;
            for (JsfUrl jsfUrl : list) {
                ifaceName = jsfUrl.getIface();
                srcDataVersion = jsfUrl.getDataVersion();
                //如果版本号一致，providerList返回null
                dataVersion = getDataVersion(ifaceName, jsfUrl.getAlias());
                if (dataVersion == srcDataVersion) {
                    //生成返回结果
                    url = RegisterHelper.getSubscribeUrl(jsfUrl, null);
                } else {
                    jsfVersion = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.JSFVERSION, RegistryConstants.JSFVERSION_VALUE);
                    serialization = RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.SERIALIZATION, Constants.CodecType.msgpack.name());
                    appId = RegistryUtil.getIntValueFromMap(jsfUrl.getAttrs(), RegistryConstants.APPID, RegistryConstants.APPID_VALUE);
                    //填补下inskey
                    if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
                    	jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
                    }
                    //获取真实ip
                    clientIp = getClientIp(jsfUrl.getIp());
                    //获取provider列表
                    providerList = subscribeServiceImpl.subscribe(ifaceName, jsfUrl.getAlias(), jsfUrl.getProtocol(), jsfVersion, serialization, jsfUrl.getInsKey(), clientIp, null, appId);
                    jsfUrl.setDataVersion(dataVersion);
                    //生成返回结果
                    url = RegisterHelper.getSubscribeUrl(jsfUrl, providerList);
                    logger.info("lookupList dataversion is changed. insKey:{}, elapse:{}, srcDataVersion:{}, resultUrl:{}", jsfUrl.getInsKey(), System.currentTimeMillis() - start, srcDataVersion, url.toString());
                }
                urlList.add(url);
            }
            //请求计数
            RequestRecoder.recodeLookupTotalCount();
            //统计ip的订阅数量
            IpRequestHandler.subscribeRecord(getClientIp(list.get(0).getIp()));
//            logger.info("lookupList is successful. insKey:{}, elapse:{}, resultUrl:{}", list.get(0).getInsKey(), System.currentTimeMillis() - start, urlList.toString());
        } catch (Exception e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("lookupList is failed. " + list.toString(), e);
        }
        return urlList;
    }

    /**
     * 验证 订阅参数
     * @param list
     * @throws RpcException
     */
    private void validateLookupList(List<JsfUrl> list) throws RpcException {
        if(list == null || list.isEmpty()) {
            handleException("validateLookupList JsfUrl list is not correct. ");
        }
        for (JsfUrl jsfUrl : list) {
            if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
                handleException("validateLookupList JsfUrl->iface is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {
                handleException("validateLookupList JsfUrl->iface is not correct. " + jsfUrl.toString());
            }
            if(jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
            	handleException("validateLookupList, JsfUrl->ip is not correct. " + jsfUrl.toString());
            }
            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().isEmpty()) {
                if (jsfUrl.getPid() == 0) {
                    handleException("validateLookupList JsfUrl->pid is not correct. " + jsfUrl.toString());
                }
                if (jsfUrl.getStTime() == 0) {
                    handleException("validateLookupList JsfUrl->starttime is not correct. " + jsfUrl.toString());
                }
            }
            if(jsfUrl.getAlias() == null || jsfUrl.getAlias().isEmpty() || !RegistryUtil.checkAlias(jsfUrl.getAlias())) {
                handleException("validateLookupList, JsfUrl->alias is not correct. " + jsfUrl.toString());
            }
            if(jsfUrl.getProtocol() <= 0) {
                handleException("validateLookupList, JsfUrl->protocol is not correct. " + jsfUrl.toString());
            }
        }
    }

    @Override
    public HbResult doHeartbeat(Heartbeat heartbeat) throws RpcException {
        try {
            validateHeartbeat(heartbeat);
        } catch (Exception e) {
            throw e;
        }

        String insKey = heartbeat.getInsKey();
        HbResult hbResult = new HbResult();
        try {
            hbResult.setInsKey(insKey);
            //放入缓存中, 并检查实例是否在数据库中，如果result为false，需要客户端重新recover
            boolean result = heartbeatServiceImpl.putHbCache(insKey);
            //如果heartbeat对象中包含HB_UNCHECK，就不用处理result. HB_UNCHECK只在注册中心自己心跳时会用到。
            if (heartbeat.getConfig() == null || heartbeat.getConfig().get(RegistryConstants.HB_UNCHECK) == null) {
                if (!result) {
                    hbResult.setConfig(new ArrayList<String>());
                    hbResult.getConfig().add(RegistryConstants.HB_RECOVER);
                    logger.info("{} should recover.", insKey);
                } else {
                    //检查callback，如果callback失效，需要客户端重新给注册中心一个callback对象
                    if (!subscribeServiceImpl.checkCallback(insKey, false)) {
                        if (hbResult.getConfig() == null) {
                            hbResult.setConfig(new ArrayList<String>());
                        }
                        hbResult.getConfig().add(RegistryConstants.HB_CALLBACK);
                        logger.info("{} loss a callback.", insKey);
                    }
                }
            }
            //心跳计数
            RequestRecoder.recodeHeartbeatTotalCount();
            //统计ip的心跳数
            IpRequestHandler.hbRecord(getClientIp(RegistryUtil.getIpFormInsKey(heartbeat.getInsKey())));
        } catch (Exception e) {
            //失败计数
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("doHeartbeat is failed. " + heartbeat.toString(), e);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("doHeartbeat is successful. {}, {}", heartbeat.toString(), hbResult.toString());
        }
        return hbResult;
    }

    /**
     * 验证心跳参数
     * @param heartbeat
     * @throws RpcException
     */
    private void validateHeartbeat(Heartbeat heartbeat) throws RpcException {
        if (heartbeat == null) {
            handleException("validateHeartbeat heartbeat is not correct. ");
        }
        if (heartbeat.getInsKey() == null || heartbeat.getInsKey().isEmpty()) {
            handleException("validateHeartbeat heartbeat->insKey is not correct. " + heartbeat.toString());
        }
    }

    /**
     * 获取客户端配置信息： 返回客户端配置信息和版本号
     * 获取接口配置信息： 获取  接口  配置信息和版本号
     */
    @Override
    public JsfUrl subscribeConfig(JsfUrl jsfUrl, Callback<SubscribeUrl, String> callback) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            //验证参数
            validateGlobalConfig(jsfUrl);
            JsfUrl resultUrl = RegisterHelper.cloneJsfUrl(jsfUrl);
            String insKey = UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime());
            String clientIp = getClientIp(jsfUrl.getIp());
            SubscribeCallback<SubscribeUrl> subscribeCallback = callback == null ? null : new SubscribeCallback<SubscribeUrl>(callback, clientIp);
            if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {   //获取客户端配置
                if (!insKey.equals(jsfUrl.getInsKey())) {
                	jsfUrl.setInsKey(insKey);
                	resultUrl.setInsKey(insKey);
                }
                resultUrl.setAttrs(subscribeServiceImpl.getConfig(insKey, subscribeCallback));
                //设置客户端配置版本号
                resultUrl.setDataVersion(getGlobalConfigDataVersion());
                //请求计数
                RequestRecoder.recodeSubscribeGlobalConfigTotalCount();
            } else {    //获取接口配置
            	resultUrl.setAttrs(subscribeServiceImpl.getInterfaceProperty(jsfUrl.getIface(), insKey, subscribeCallback, clientIp));
                //设置接口配置版本号
            	resultUrl.setDataVersion(getInterfaceConfigDataVersion(jsfUrl.getIface()));
                //请求计数
                RequestRecoder.recodeSubscribeInterfaceConfigTotalCount();
            }
            logger.info("subscribeConfig is successful. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", insKey, System.currentTimeMillis() - start, jsfUrl.toString(), resultUrl.toString());
            return resultUrl;
        } catch (RpcException e) {
            RequestRecoder.recodeRequestFailTotalCount();
            logger.error("subscribeConfig is failed. " + jsfUrl.toString(), e);
            throw e;
        } catch (Throwable e) {
            RequestRecoder.recodeRequestFailTotalCount();
            handleException("subscribeConfig is failed. " + jsfUrl.toString(), e);
        }
        logger.info("subscribeConfig fail >>>>. {}", jsfUrl.toString());
        return null;
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
     * 获取配置的版本号
     * @param
     * @return
     */
    private long getGlobalConfigDataVersion() {
        return subscribeServiceImpl.getGlobalConfigDataVersion(null);
    }

    /**
     * 获取接口配置的版本号
     * @param ifaceName
     * @return
     */
    private long getInterfaceConfigDataVersion(String ifaceName) {
        try {
            return subscribeHelper.getInterfaceConfigDataVersion(ifaceName);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return 0;
    }

    /**
     * 获取客户端配置信息： 检查版本号，如果相同，则返回版本号。如果不同，先验证insKey，insKey不一致就新增实例信息，并且返回客户端配置信息和版本号
     * 获取接口配置信息： 检查版本号，如果相同，则返回版本号。如果不同，获取  接口  配置信息
     */
    @Override
    public JsfUrl getConfig(JsfUrl jsfUrl) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            //clone的目录是防止injvm调用时，jsfUrl对象被修改
            JsfUrl resultUrl = RegisterHelper.cloneJsfUrl(jsfUrl);
            String clientIp = getClientIp(jsfUrl.getIp());
            if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {    //获取客户端配置
                //验证参数
                validateGlobalConfig(jsfUrl);
                //获取客户端配置版本号
                long dataVersion = getGlobalConfigDataVersion();
                if (jsfUrl.getDataVersion() != dataVersion) {    //检查版本号，如果不一样，获取配置信息
                    String insKey = UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime());
                    if (!insKey.equals(jsfUrl.getInsKey())) {
                        resultUrl.setInsKey(insKey);
                    }
                    //获取客户端配置
                    resultUrl.setAttrs(subscribeServiceImpl.getConfig(insKey, null));
                    resultUrl.setDataVersion(dataVersion);
                    logger.info("getConfig-global dataversion is changed. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", jsfUrl.getInsKey(), System.currentTimeMillis() - start, jsfUrl.toString(), resultUrl.toString());
                }
                RequestRecoder.recodeGetGlobalConfigTotalCount();
            } else {    //获取接口配置
                //获取接口配置版本号
                long dataVersion = getInterfaceConfigDataVersion(jsfUrl.getIface());
                if (dataVersion != jsfUrl.getDataVersion()) {//检查版本号，如果不一样，获取配置信息
                    //获取接口配置
                    resultUrl.setAttrs(subscribeServiceImpl.getInterfaceProperty(jsfUrl.getIface(), jsfUrl.getInsKey(), null, clientIp));
                    resultUrl.setDataVersion(dataVersion);
                    logger.info("getConfig-iface dataversion is changed. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", jsfUrl.getInsKey(), System.currentTimeMillis() - start, jsfUrl.toString(), resultUrl.toString());
                }
                RequestRecoder.recodeGetInterfaceConfigCount();
            }
            return resultUrl;
        } catch (RpcException e) {
            getConfigFailRecord();
            logger.error("getConfig is failed. " + jsfUrl.toString(), e);
            throw e;
        } catch (Exception e) {
            getConfigFailRecord();
            handleException("getConfig is failed. " + jsfUrl.toString(), e);
        }
        return null;
    }

    @Override
    public List<JsfUrl> getConfigList(List<JsfUrl> list) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateConfigList(list);
            JsfUrl firstUrl = list.get(0);
            String insKey = UniqkeyUtil.getInsKey(firstUrl.getIp(), firstUrl.getPid(), firstUrl.getStTime());
            String clientIp = getClientIp(firstUrl.getIp());
            long dataVersion = 0;
            long srcDataVersion = 0;
            //给第一个jsfurl赋值inskey
            if (!insKey.equals(firstUrl.getInsKey())) {
                firstUrl.setInsKey(insKey);
            }
            JsfUrl resultUrl = null;
            List<JsfUrl> resultList = new ArrayList<JsfUrl>();
            for (JsfUrl jsfUrl : list) {
            	srcDataVersion = jsfUrl.getDataVersion();
            	resultUrl = RegisterHelper.cloneJsfUrl(jsfUrl);
                if (jsfUrl.getIface() == null || jsfUrl.getIface().isEmpty()) {    //获取客户端配置
                    //获取客户端配置版本号
                    dataVersion = getGlobalConfigDataVersion();
                    if (jsfUrl.getDataVersion() != dataVersion) {    //检查版本号，如果不一样，获取配置信息
                        //获取客户端配置
                    	resultUrl.setAttrs(subscribeServiceImpl.getConfig(jsfUrl.getInsKey(), null));
                    	resultUrl.setDataVersion(dataVersion);
                        logger.info("getConfigList globalconfig dataversion {} is changed. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", srcDataVersion, insKey, System.currentTimeMillis() - start, jsfUrl.toString(), resultUrl.toString());
                    }
                    RequestRecoder.recodeGetGlobalConfigTotalCount();
                } else {    //获取接口配置
                    //获取接口配置版本号
                    dataVersion = getInterfaceConfigDataVersion(jsfUrl.getIface());
                    if (dataVersion != jsfUrl.getDataVersion()) {//检查版本号，如果不一样，获取配置信息
                        //获取接口配置
                    	resultUrl.setAttrs(subscribeServiceImpl.getInterfaceProperty(jsfUrl.getIface(), jsfUrl.getInsKey(), null, clientIp));
                    	resultUrl.setDataVersion(dataVersion);
                        logger.info("getConfigList interface {} dataversion {} is changed. insKey:{}, elapse:{}, jsfUrl:{}, resultUrl:{}", jsfUrl.getIface(), srcDataVersion, insKey, System.currentTimeMillis() - start, jsfUrl.toString(), resultUrl.toString());
                    }
                    RequestRecoder.recodeGetInterfaceConfigCount();
                }
                resultList.add(jsfUrl);
            }
            return resultList;
        } catch (RpcException e) {
            getConfigFailRecord();
            logger.error("getConfigList is failed. " + list.toString(), e);
            throw e;
        } catch (Exception e) {
            getConfigFailRecord();
            handleException("getConfigList is failed. " + list.toString(), e);
        }
        return null;
    }

    private void validateConfigList(List<JsfUrl> list) throws RpcException {
        if (list == null || list.isEmpty()) {
            handleException("validateConfigList jsfUrl list is not correct. ");
        }
        JsfUrl jsfUrl = list.get(0);
        if (jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
            handleException("validateConfigList jsfUrl->ip is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getPid() == 0) {
            handleException("validateConfigList jsfUrl->pid is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getStTime() == 0l) {
            handleException("validateConfigList jsfUrl->starttime is not correct. " + jsfUrl.toString());
        }
    }

    /**
     * @param ip
     * @return
     */
    private String getClientIp(String ip) {
        if (RegistryUtil.isTest) {
            //如果是测试环境，就返回客户端传递过来的ip
            return ip;
        }

        String clientIp = null;
        try {
            clientIp = RpcContext.getContext().getRemoteAddress().getAddress().getHostAddress();
        } catch (Exception e) {
        }
        if (StringUtils.isEmpty(clientIp) || NetUtils.isLocalHost(clientIp)) {
            clientIp = ip;
        }
        return clientIp;
    }

    private void validateGlobalConfig(JsfUrl jsfUrl) throws RpcException {
        if(jsfUrl == null) {
            handleException("validateGlobalConfig jsfUrl is not correct. ");
        }
        if (jsfUrl.getIp() == null || jsfUrl.getIp().isEmpty()) {
            handleException("validateGlobalConfig jsfUrl->ip is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getPid() == 0) {
            handleException("validateGlobalConfig jsfUrl->pid is not correct. " + jsfUrl.toString());
        }
        if (jsfUrl.getStTime() == 0l) {
            handleException("validateGlobalConfig jsfUrl->starttime is not correct. " + jsfUrl.toString());
        }
    }

    /**
     * 获取配置失败计数
     */
    private void getConfigFailRecord() {
        RequestRecoder.recodeRequestFailTotalCount();
    }

    private void handleException(String message, Exception e) throws RpcException {
        logger.error(message + ",error:" + e.getMessage(), e);
        throw new RpcException(RegistryUtil.messageHandler(message));
    }

    private void handleException(String message, Throwable t) throws RpcException {
        logger.error(message + ",error:" + t.getMessage(), t);
        throw new RpcException(RegistryUtil.messageHandler(message));
    }

    private void handleException(String message) throws RpcException {
        logger.error(message);
        throw new RpcException(RegistryUtil.messageHandler(message));
    }

    private void handleInitException(String message) throws InitErrorException {
        logger.error(message);
        throw new InitErrorException(RegistryUtil.messageHandler(message));
    }

	@Override
	public List<JsfUrl> doRegisterList(List<JsfUrl> jsfUrlList) throws RpcException {
		long start = System.currentTimeMillis();
        try {
            validateRegistryList(jsfUrlList);
            checkIpLimit(jsfUrlList.get(0));
        } catch (Exception e) {
            throw e;
        }
        try {
            List<Server> serverList = null;
        	List<Client> clientList = null;
        	JsfIns ins = null;
        	boolean isCheckVersion = false;
        	for (JsfUrl jsfUrl : jsfUrlList) {
	            InterfaceInfo ifaceInfo = checkInterface(jsfUrl, "doRegisterList");
	            //填补下inskey
	            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
	                jsfUrl.setInsKey(UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime()));
	            }
	            //检查jsf版本
	            if (isCheckVersion == false) {
		            checkVersion(jsfUrl);
		            isCheckVersion = true;
	            }
	            Date date = DateTimeZoneUtil.getTargetTime();
	            if (ins == null) {
	            	ins = RegisterHelper.getInsFromJsfUrl(jsfUrl, date, configServiceImpl.getRoomByIp(jsfUrl.getIp()));
	            }
	            if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, RegisterHelper.PROVIDER_VALUE).equals(RegisterHelper.PROVIDER_VALUE)) {
	                Server server = RegisterHelper.getServerFromRegister(jsfUrl, ifaceInfo, date, configServiceImpl.getRoomByIp(jsfUrl.getIp()));
	                if (serverList == null) serverList = new ArrayList<Server>();
	                serverList.add(server);
	            } else {
	                Client client = RegisterHelper.getClientFromRegister(jsfUrl, ifaceInfo, date);
	                if (clientList == null) clientList = new ArrayList<Client>();
	                clientList.add(client);
	            }
	            //给客户端返回interfaceId
	            jsfUrl.getAttrs().put("ifaceId", String.valueOf(ifaceInfo.getInterfaceId()));
        	}
        	//保存实例信息
            heartbeatServiceImpl.register(ins);
            //保存provider信息
            if (serverList != null) {
            	serverServiceImpl.registerServer(serverList, ins);
            	//请求计数
                RequestRecoder.recodeProviderRegistryCount();
            }
            //保存consumer信息
            if (clientList != null) {
            	clientServiceImpl.registerClient(clientList, ins);
            	//请求计数
                RequestRecoder.recodeConsumerRegistryCount();
            }
            //统计ip注册数量
            IpRequestHandler.registryRecord(getClientIp(jsfUrlList.get(0).getIp()));
            //保存连接信息
            putConnection(jsfUrlList.get(0));
            long end = System.currentTimeMillis();
            logger.info("doRegisterList is successful. insKey:{} . elapse: {}ms . {}", jsfUrlList.get(0).getInsKey(), (end - start), jsfUrlList.toString());
        } catch (RpcException e) {
            RegisterHelper.registerFailRecord();
            throw e;
        } catch (InitErrorException e) {
        	RegisterHelper.registerFailRecord();
            throw e;
        } catch (Exception e) {
        	RegisterHelper.registerFailRecord();
            handleException("doRegisterList is failed." + jsfUrlList.toString(), e);
        }
        return jsfUrlList;
	}

	/**
	 * 验证注册的jsfurl是否正确
	 * @param jsfUrlList
	 */
	private void validateRegistryList(List<JsfUrl> jsfUrlList) {
		if (jsfUrlList != null && !jsfUrlList.isEmpty()) {
			for (JsfUrl jsfUrl : jsfUrlList) {
				validateRegister(jsfUrl);
			}
		} else {
			handleException("validateRegistryList, jsfUrlList is null. ");
		}
	}

	@Override
	public boolean doCheckRegister(JsfUrl jsfUrl) throws RpcException {
		validateRegister(jsfUrl);
		checkInterface(jsfUrl, "doCheckRegister");
		return true;
	}

	@Override
	public boolean doUnRegisterList(List<JsfUrl> jsfUrlList) throws RpcException {
        long start = System.currentTimeMillis();
        try {
            validateUnRegisterList(jsfUrlList);
        } catch (Exception e) {
            throw e;
        }
        try {
        	List<Server> serverList = null;
        	List<Client> clientList = null;
        	String insKey = null;
        	JsfIns ins = null;
        	for (JsfUrl jsfUrl : jsfUrlList) {
        		//检查是否注册接口
	            InterfaceInfo ifaceInfo = checkInterface(jsfUrl, "doUnRegister");
	            if (jsfUrl.getInsKey() == null || jsfUrl.getInsKey().equals("")) {
	            	if (insKey == null) {
	            		insKey = UniqkeyUtil.getInsKey(jsfUrl.getIp(), jsfUrl.getPid(), jsfUrl.getStTime());
	            	}
	                jsfUrl.setInsKey(insKey);
	            }
	            if (ins == null) {
	            	ins = RegisterHelper.getInsFromJsfUrl(jsfUrl, new Date(), configServiceImpl.getRoomByIp(jsfUrl.getIp()));
	            }
	            Date date = DateTimeZoneUtil.getTargetTime();
	            //判断是否是provider
	            if (RegistryUtil.getValueFromMap(jsfUrl.getAttrs(), RegistryConstants.CONSUMER, RegisterHelper.PROVIDER_VALUE).equals(RegisterHelper.PROVIDER_VALUE)) {
	            	Server server = RegisterHelper.getServerFromUnregister(jsfUrl, ifaceInfo, date);
	            	if (serverList == null) serverList = new ArrayList<Server>();
	            	serverList.add(server);
	            } else {
	            	Client client = RegisterHelper.getClientFromUnregister(jsfUrl, ifaceInfo, date);
	            	if (clientList == null) clientList = new ArrayList<Client>();
	            	clientList.add(client);
	            }
        	}
        	//保存provider
        	if (serverList != null) {
        		serverServiceImpl.unRegisterServer(serverList, ins);
        		RegisterHelper.afterUnregistry(true);
        	}
        	//保存consumer
        	if (clientList != null) {
        		clientServiceImpl.unRegisterClient(clientList, ins);
        		for (JsfUrl jsfUrl : jsfUrlList) {
        			subscribeServiceImpl.unRegistryConsumer(jsfUrl.getIface(), jsfUrl.getAlias(), jsfUrl.getProtocol(), jsfUrl.getInsKey());
        		}
        		RegisterHelper.afterUnregistry(false);
        	}
        	logger.info("doUnRegisterList is successful. insKey:{} . elapse: {}ms . jsfUrlList: {}", jsfUrlList.get(0).getInsKey(), (System.currentTimeMillis() - start), jsfUrlList.toString());
        	return true;
        } catch (RpcException e) {
            RegisterHelper.unregisterFailRecord();
            handleException("doUnRegisterList is failed. " + jsfUrlList.toString(), e);
            throw e;
        } catch (Exception e) {
        	RegisterHelper.unregisterFailRecord();
            handleException("doUnRegisterList is failed. " + jsfUrlList.toString(), e);
        }
        return false;
	}

	/**
	 * @param jsfUrlList
	 */
	private void validateUnRegisterList(List<JsfUrl> jsfUrlList) {
		if (jsfUrlList != null && !jsfUrlList.isEmpty()) {
			for (JsfUrl jsfUrl : jsfUrlList) {
				validateUnRegister(jsfUrl);
			}
		} else {
			handleException("validateUnRegistryList, jsfUrlList is null. ");
		}
	}

	@Override
	public boolean doCheckUnRegister(JsfUrl jsfUrl) throws RpcException {
		validateUnRegister(jsfUrl);
		checkInterface(jsfUrl, "doCheckUnRegister");
		return true;
	}
}
