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
package com.ipd.jsf.registry.server;

import io.netty.channel.ChannelHandlerContext;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.gd.config.MethodConfig;
import com.ipd.jsf.gd.config.ProviderConfig;
import com.ipd.jsf.gd.config.ServerConfig;
import com.ipd.jsf.gd.monitor.MonitorFactory;
import com.ipd.jsf.gd.msg.ConnectListener;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.registry.berkeley.dao.BerkeleyDb;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.recoder.ConnectionRecoder;
import com.ipd.jsf.registry.service.EventSynService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.threadpool.WorkerThreadPool;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryCtrlService;
import com.ipd.jsf.service.RegistryHttpService;
import com.ipd.jsf.service.RegistryQueryService;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.service.RegistryStatusService;
import com.ipd.jsf.service.impl.EventBusCallback;
import com.ipd.jsf.vo.Heartbeat;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.worker.service.vo.RegistryInfo;

/**
 * 注册中心服务
 * 1. 服务发布
 * 2. 注册当前的注册中心
 * 3. 自我心跳
 */
public class RegistryServer {

    /**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(RegistryServer.class);
    private String ip = "";
    /** 默认端口 **/
    private int port = 40660;
    //pid
    private int pid = 0;
    /** 心跳间隔 **/
    private int hbInterval = 60;
    /** 获取配置间隔 **/
    private int configInterval = 60 * 10;
    /** 协议名 **/
    private String protocol = "jsf";
    /** 应用路径 **/
    private String appPath = System.getProperty("user.dir");
    /** alias **/
    private String alias = "reg";
    /**线程池大小 **/
    private int threads = 400;
    /**并发数**/
    private int concurrents = 400;
    /**线程池队列大小 **/
    private int queues = 5000;
    /**线程池类型**/
    private String threadPool = "fixed";
    /** httpToken **/
    private String httpToken = null;
    /** 语言 **/
    private String language = "java";
    /** 权重**/
    private String weight = "200";
    /** 连接数阈值 **/
    private int conThreshold = 10000;
    /** 达到连接数阈值后，一分钟允许增长的连接数 **/
    private int conDelta = 200;
    /** ump jvm key **/
    private String alarmUmpJvmKey;
    /** ump alive key **/
    private String alarmUmpAliveKey;
    /** 接口配置信息 **/
    private Map<String, String> interfaceConfig;
    /** 接口配置版本号 **/
    private long interfaceConfigDataversion = 0;
    /** 全局配置版本号 **/
    private long globalConfigDataversion = 0;
    /** 实例key **/
    private String insKey = null;
    private ConnectionSecurityHelper connectionSecurityHelper = null;
    /** 黑白名单结果列表 */
    private ConcurrentHashMap<String, List<String>> wbCacheMap = new ConcurrentHashMap<String, List<String>>();
    /** 黑白名单检查结果 key=ip, value=check result */
    private ConcurrentHashMap<String, Boolean> wbCheckRecordCache = new ConcurrentHashMap<String, Boolean>();
    //通知线程池
    private WorkerThreadPool eventThreadPool = new WorkerThreadPool(1, 1, "conn-event", 50000);

    @Autowired
    private RegistryService registryServiceImpl;

    @Autowired
    private RegistryHttpService registryHttpServiceImpl;
    
    @Autowired
    private RegistryStatusService registryStatusServiceImpl;

    @Autowired
    private RegistryCtrlService registryCtrlServiceImpl;

    @Autowired
    private RegistryQueryService registryQueryServiceImpl;

    @Autowired
    private EventSynService eventSynServiceImpl;

    @Autowired
    private SubscribeService subscribeServiceImpl;

    @Autowired
    private BerkeleyDb berkeleyDb;

    private EventBusCallback eventBusCallback = new EventBusCallback();

    /**
     * 1.发布服务
     * 2.注册自己
     * 当RegistryCacheLoader加载完缓存后，调用start
     */
    public void start() throws Exception {
        //将获取配置提前，主要是改下monitor配置
        insKey = getGlobalConfig(null);
        getInterfaceConfig(insKey);
        configMonitor();
        //发布注册中心服务
        deployRegistryService();
        //注册自己
        registerMyself();
        jvmDestroy();
    }

    /**
     * 获取监听器
     * @return
     */
    private ConnectListener getConnectLisener() {
    	// 服务提供者协议配置（必须）
        ConnectListener listener = new ConnectListener() {
            @Override
            public void connected(final ChannelHandlerContext ctx) {
                try {
                    eventThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                        	String remoteIp = null;
                            try {
                                remoteIp = NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()).split(RegistryConstants.SPLITSTR_COLON)[0];
                            } catch (Exception e) {
                                logger.error("parse remote ip error:" + e.getMessage());
                            }
                            try {
                            	//连接数加1
                            	ConnectionRecoder.increaseConnection();
                            	//黑白名单验证
                            	if (remoteIp != null && !checkCanVisit(remoteIp)) {
                            		ctx.channel().close();
                            		logger.warn("remote ip is deny:" + remoteIp);
                            		return;
                            	}
                            	//连接数保护策略
                            	if (connectionSecurityHelper.connectionProtect()) {
                            		ctx.channel().close();
                            		logger.warn("connections is up to limit, remoteIp:" + remoteIp + " connection is closed. total connection is " + ConnectionRecoder.getConnectionTotalCount());
                            		return;
                            	}
                                //记录长连接信息
                                ConnectionRecoder.putConnContext(NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress()), 
                                        NetUtils.channelToString(ctx.channel().remoteAddress(), ctx.channel().localAddress()));
                                if (logger.isDebugEnabled()) {
                                    logger.debug("all connection: {}", ConnectionRecoder.getAllConnContext());
                                }
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
                        }
                    });
                } catch (Exception e) {
                    logger.error("connected error:" + e.getMessage(), e);
                }
            }

            @Override
            public void disconnected(final ChannelHandlerContext ctx) {
            	try {
            		eventThreadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                            	// 连接数减1
                            	ConnectionRecoder.decreaseConnection();
                                String connKey = NetUtils.toAddressString((InetSocketAddress) ctx.channel().remoteAddress());
                                // 删除长连接信息
                                ConnectionRecoder.removeConnContext(connKey);
                                JsfIns ins = ConnectionRecoder.removeConnIns(connKey);
                                logger.info("---------------disconnected, remove connKey<{}>", connKey);
                                //连接断开，要删除缓存中的实例对象
                                if (ins != null) {
                                    subscribeServiceImpl.removeInstanceCache(ins.getInsKey());
                                }
                                if (logger.isDebugEnabled()) {
                                    logger.debug("all connection: {}", ConnectionRecoder.getAllConnContext());
                                }
                            } catch (Exception e) {
                                logger.error("disconnected error:" + e.getMessage(), e);
                            }
                        }
                    });
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
                
            }
        };
        return listener;
    }
    
    /**
     * 发布服务
     */
    private void deployRegistryService() {
    	//初始化
    	connectionSecurityHelper = new ConnectionSecurityHelper(conThreshold, conDelta);
        List<ConnectListener> listenerList = new ArrayList<ConnectListener>();
        listenerList.add(getConnectLisener());

        ServerConfig serverConfig = new ServerConfig();
        serverConfig.setProtocol(protocol);
        serverConfig.setPort(port);
        serverConfig.setOnconnect(listenerList);
        serverConfig.setThreads(threads);
        serverConfig.setQueues(queues);
        serverConfig.setThreadpool(threadPool);
        List<ServerConfig> servers = new ArrayList<ServerConfig>();
        servers.add(serverConfig);
        // 服务提供者连接注册中心，设置属性
        ProviderConfig<RegistryService> serviceConfig = new ProviderConfig<RegistryService>();
        serviceConfig.setInterfaceId(RegistryUtil.getRegistryIfaceName());
        serviceConfig.setServer(servers);
        serviceConfig.setRef(registryServiceImpl);
        serviceConfig.setAlias(alias);
        serviceConfig.setRegister(false);
        serviceConfig.setConcurrents(concurrents);
        // 暴露及注册服务
        serviceConfig.export();
        logger.info("发布服务完成-----------registry port----------:" + port);
        //设置端口
        RegistryUtil.setRegistryPort(port);

        /**发布注册中心http协议的lookup服务**/
        ProviderConfig<RegistryHttpService> registryHttpServiceConfig = new ProviderConfig<RegistryHttpService>();
        registryHttpServiceConfig.setInterfaceId(RegistryHttpService.class.getName());
        registryHttpServiceConfig.setServer(servers);
        registryHttpServiceConfig.setRef(registryHttpServiceImpl);
        registryHttpServiceConfig.setAlias(alias);
        registryHttpServiceConfig.setRegister(false);
        registryHttpServiceConfig.setParameter(Constants.HIDDEN_KEY_TOKEN, httpToken);
        // 暴露及注册服务
        registryHttpServiceConfig.export();

        /**发布注册中心状态监控服务**/
        ProviderConfig<RegistryStatusService> statusServiceConfig = new ProviderConfig<RegistryStatusService>();
        statusServiceConfig.setInterfaceId(RegistryStatusService.class.getName());
        statusServiceConfig.setServer(servers);
        statusServiceConfig.setRef(registryStatusServiceImpl);
        statusServiceConfig.setAlias(alias);
        statusServiceConfig.setRegister(false);
        // 暴露及注册服务
        statusServiceConfig.export();
        
        /**发布注册中心状态远程控制服务**/
        ProviderConfig<RegistryCtrlService> controlServiceConfig = new ProviderConfig<RegistryCtrlService>();
        controlServiceConfig.setInterfaceId(RegistryCtrlService.class.getName());
        controlServiceConfig.setServer(servers);
        controlServiceConfig.setRef(registryCtrlServiceImpl);
        controlServiceConfig.setAlias(alias);
        controlServiceConfig.setRegister(false);
        List<MethodConfig> methods = new ArrayList<MethodConfig>();
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("customCallback");
        methodConfig.setParameter(Constants.HIDDEN_KEY_TOKEN, "jsf2015niu");
        methods.add(methodConfig);
        controlServiceConfig.setMethods(methods);
        // 暴露及注册服务
        controlServiceConfig.export();

        /**发布注册中心简易管理端服务**/
        ProviderConfig<RegistryQueryService> queryServiceConfig = new ProviderConfig<RegistryQueryService>();
        queryServiceConfig.setInterfaceId(RegistryQueryService.class.getName());
        queryServiceConfig.setServer(servers);
        queryServiceConfig.setRef(registryQueryServiceImpl);
        queryServiceConfig.setAlias(alias);
        queryServiceConfig.setRegister(false);
        // 暴露及注册服务
        queryServiceConfig.export();
    }

    /**
     * 将自己注册进去, 然后心跳
     */
    private void registerMyself() {
        //注册自己
        register(insKey);
        logger.info("registry myself... insKey:{}", insKey);
        //心跳定时任务
        doHbSchedule(insKey);
        //获取全局配置和接口配置  定时任务
        getConfigSchedule(insKey);
        //注册到事件同步worker
        eventRegister();
    }

    /**
     * 注册当前的注册中心。
     * 因为注册中心也是一种jsf的provider，所以需要单独写注册方法
     * @return
     */
    private void register(String insKey) {
        JsfUrl jsfUrl = getRegisterJsfUrl(insKey, RegistryUtil.getRegistryIfaceName());
        JsfUrl jsfUrl_http = getRegisterJsfUrl(insKey, RegistryUtil.getRegistryHttpIfaceName());

        try {
            registryServiceImpl.doRegister(jsfUrl);
            registryServiceImpl.doRegister(jsfUrl_http);
            logger.info("registry:" + ip + ":" + port + " - insKey:" + insKey);
        } catch (Exception e) {
            logger.error("registry:" + ip + ":" + port, e);
        }
    }

	/**
	 * @param insKey
	 * @return
	 */
	private JsfUrl getRegisterJsfUrl(String insKey, String iface) {
		JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp(RegistryUtil.getRegistryIP());
        jsfUrl.setPort(port);
        jsfUrl.setAlias(alias);
        jsfUrl.setPid(getPid());
        jsfUrl.setStTime(JSFContext.START_TIME);
        jsfUrl.setIface(iface);
        jsfUrl.setProtocol(ProtocolType.jsf.value());
        jsfUrl.setRandom(false);
        jsfUrl.setInsKey(insKey);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(RegistryConstants.APPPATH, appPath);
        attrs.put(RegistryConstants.LANGUAGE, language);
        attrs.put(RegistryConstants.SAFVERSION, String.valueOf(RegistryConstants.SAFVERSION_VALUE));
        attrs.put(RegistryConstants.JSFVERSION, String.valueOf(Constants.JSF_VERSION));
        attrs.put(RegistryConstants.WEIGHT, weight);
        jsfUrl.setAttrs(attrs);
		return jsfUrl;
	}

    /**
     * 自我心跳
     * @param insKey
     */
    private void doHbSchedule(final String insKey) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                	Heartbeat heartbeat = new Heartbeat();
                	heartbeat.setInsKey(insKey);
                	heartbeat.setConfig(new HashMap<String, String>());
                	heartbeat.getConfig().put("uncheck", "1");
                    registryServiceImpl.doHeartbeat(heartbeat);
                } catch (Exception e) {
                    logger.error("insKey: {} 注册中心自我心跳异常，等待下次执行. ", insKey);
                    logger.error(e.getMessage(), e);
                }
            }
        }, 10, hbInterval, TimeUnit.SECONDS);
    }

    private void getConfigSchedule(final String insKey) {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
            	try {
            		getGlobalConfig(insKey);
            		getInterfaceConfig(insKey);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
            }
        }, 10, configInterval, TimeUnit.SECONDS);
    }

    private String getGlobalConfig(String insKey) {
        JsfUrl jsfUrl = new JsfUrl();
        try {
            jsfUrl.setIp(RegistryUtil.getRegistryIP());
            jsfUrl.setPort(port);
            jsfUrl.setAlias(alias);
            jsfUrl.setPid(getPid());
            jsfUrl.setStTime(JSFContext.START_TIME);
            jsfUrl.setRandom(false);
            jsfUrl.setDataVersion(globalConfigDataversion);
            Map<String, String> attrs = new HashMap<String, String>();
            attrs.put(RegistryConstants.LANGUAGE, language);
            attrs.put(RegistryConstants.SAFVERSION, String.valueOf(RegistryConstants.SAFVERSION_VALUE));
            jsfUrl.setAttrs(attrs);
            if (insKey != null) {
                jsfUrl.setInsKey(insKey);
            }
            jsfUrl = registryServiceImpl.getConfig(jsfUrl);
            if (globalConfigDataversion != jsfUrl.getDataVersion() && jsfUrl.getAttrs() != null) {
                globalConfigDataversion = jsfUrl.getDataVersion();
            }
        } catch (Exception e) {
            logger.error("insKey: {},  getGlobalConfig error:  {}", jsfUrl.getInsKey(), e.getMessage());
        }
        if (insKey == null || insKey.isEmpty()) {
            jsfUrl.setInsKey(UniqkeyUtil.getInsKey(RegistryUtil.getRegistryIP(), getPid(), JSFContext.START_TIME));
            this.insKey = jsfUrl.getInsKey();
        }
        return jsfUrl.getInsKey();
    }

    private String getInterfaceConfig(String insKey) {
        JsfUrl jsfUrl = new JsfUrl();
        try {
            jsfUrl.setIp(RegistryUtil.getRegistryIP());
            jsfUrl.setPort(port);
            jsfUrl.setAlias(alias);
            jsfUrl.setPid(getPid());
            jsfUrl.setStTime(JSFContext.START_TIME);
            jsfUrl.setIface(RegistryUtil.getRegistryIfaceName());
            jsfUrl.setProtocol(ProtocolType.jsf.value());
            jsfUrl.setRandom(false);
            jsfUrl.setDataVersion(interfaceConfigDataversion);
            jsfUrl = registryServiceImpl.getConfig(jsfUrl);
            if (interfaceConfigDataversion != jsfUrl.getDataVersion() && jsfUrl.getAttrs() != null) {
                interfaceConfigDataversion = jsfUrl.getDataVersion();
                interfaceConfig = jsfUrl.getAttrs();
                
                //更新黑白名单
                String white = interfaceConfig.get(Constants.SETTING_INVOKE_WHITELIST);
                String black = interfaceConfig.get(Constants.SETTING_INVOKE_BLACKLIST);
                if (white != null && !white.equals("")) {
                    wbCacheMap.put(Constants.SETTING_INVOKE_WHITELIST, Arrays.asList(white.split(RegistryConstants.SPLITSTR_COMMA)));
                }
                if (black != null && !black.equals("")) {
                    wbCacheMap.put(Constants.SETTING_INVOKE_BLACKLIST, Arrays.asList(black.split(RegistryConstants.SPLITSTR_COMMA)));
                }
                wbCheckRecordCache.clear();
            }
            if (insKey != null) {
                jsfUrl.setInsKey(insKey);
            }
        } catch (Exception e) {
            logger.error("insKey: {},  getGlobalConfig error:  {}", jsfUrl.getInsKey(), e.getMessage());
        }
        return jsfUrl.getInsKey();
    }

    private void configMonitor() {
    	//更新monitor配置
        JSFContext.putGlobalVal(Constants.SETTING_MONITOR_GLOBAL_OPEN, "true");
        JSFContext.putGlobalVal(Constants.SETTING_MONITOR_WHITELIST, "*");
        JSFContext.putInterfaceVal(RegistryUtil.getRegistryIfaceName(), Constants.SETTING_MONITOR_WHITELIST, "*");
        JSFContext.putInterfaceVal(RegistryUtil.getRegistryIfaceName(), Constants.SETTING_MONITOR_OPEN, "true");
        MonitorFactory.invalidateCache(RegistryUtil.getRegistryIfaceName());
        JSFContext.putInterfaceVal(RegistryUtil.getRegistryHttpIfaceName(), Constants.SETTING_MONITOR_WHITELIST, "*");
        JSFContext.putInterfaceVal(RegistryUtil.getRegistryHttpIfaceName(), Constants.SETTING_MONITOR_OPEN, "true");
        MonitorFactory.invalidateCache(RegistryUtil.getRegistryHttpIfaceName());
    }

    /**
     * 检查黑白名单. 返回值为true，说明clientIp可以访问，false，clientIp不能访问
     * 
     * 1、白名单为空，下一步。白名单不为空时，如果白名单中不包括，直接返回false；如果包括，下一步
     * 2、黑名单为空，下一步。如果黑名单包括，直接返回false；如果黑名单不包括，下一步
     * 3、返回true
     * @param ip
     * @return
     */
    private boolean checkCanVisit(String ip) {
        //先检查缓存
        if (this.wbCheckRecordCache.get(ip) != null) {
            return wbCheckRecordCache.get(ip).booleanValue();
        }

        if (wbCacheMap == null || wbCacheMap.size() == 0 || ip == null || "".equals(ip)) {
            return true;
        }
        if (this.wbCacheMap.size() == 0) {
            return true;
        }

        boolean result = false;
        try {
            // 检查白名单
            List<String> whiteList = this.wbCacheMap.get(Constants.SETTING_INVOKE_WHITELIST);
            if (whiteList != null && whiteList.size() > 0) {
                for (String white : whiteList) {
                    if ("*".equals(white) || match(white, ip)) {
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
            List<String> blackList = this.wbCacheMap.get(Constants.SETTING_INVOKE_BLACKLIST);
            if (blackList != null && blackList.size() > 0) {
                for (String black : blackList) {
                    if (!"".equals(black) && match(black, ip)) {
                        result = false;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("checkCanVisit error!", e);
        } finally {
            wbCheckRecordCache.put(ip, result);
        }
        return result;
    }
    
    private boolean match(String expression, String str){
        Pattern p = Pattern.compile(expression);
        Matcher m = p.matcher(str);
        boolean b = m.find();
        return b;
    }

    /**
     * 将注册中心注册到事件同步worker上
     */
    private void eventRegister() {
        RegistryInfo info = new RegistryInfo();
        info.setIp(RegistryUtil.getRegistryIP());
        info.setPort(RegistryUtil.getRegistryPort());
        eventBusCallback.setSubscribeService(subscribeServiceImpl);
        try {
            eventSynServiceImpl.register(info, eventBusCallback);
        } catch (Exception e) {
            logger.error("注册中心访问事件同步worker，注册方法 error:{}", e.getMessage());
        }
    }

    /**
     * 向同步事件worker发送注册中心取消注册事件
     */
    private void eventUnRegister() {
        try {
            RegistryInfo info = new RegistryInfo();
            info.setIp(RegistryUtil.getRegistryIP());
            info.setPort(RegistryUtil.getRegistryPort());
            eventSynServiceImpl.unregister(info);
        } catch (Exception e) {
            logger.error("event unRegister error: {}", e.getMessage());
        }
    }

    /**
     * jvm退出前
     * 1.关闭berkeleyDB
     * 2.将注册中心从事件同步worker中删除掉
     */
    private void jvmDestroy() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                try {
                    berkeleyDb.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                try {
                    eventUnRegister();
                } catch (Exception e) {
                	throw new RuntimeException(e);
                }
            }
        });
    }
    
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
        RegistryUtil.setRegistryPort(port);
    }

    /**
     * @return the hbInterval
     */
    public int getHbInterval() {
        return hbInterval;
    }

    /**
     * @param hbInterval the hbInterval to set
     */
    public void setHbInterval(int hbInterval) {
        this.hbInterval = hbInterval;
    }

    /**
	 * @return the configInterval
	 */
	public int getConfigInterval() {
		return configInterval;
	}

	/**
	 * @param configInterval the configInterval to set
	 */
	public void setConfigInterval(int configInterval) {
		this.configInterval = configInterval;
	}

	/**
	 * @return the protocol
	 */
	public String getProtocol() {
		return protocol;
	}

	/**
	 * @param protocol the protocol to set
	 */
	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**
	 * @return the alias
	 */
	public String getAlias() {
		return alias;
	}

	/**
	 * @param alias the alias to set
	 */
	public void setAlias(String alias) {
		this.alias = alias;
	}

	/**
	 * @return the threads
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * @param threads the threads to set
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}

	/**
	 * @return the concurrents
	 */
	public int getConcurrents() {
		return concurrents;
	}

	/**
	 * @param concurrents the concurrents to set
	 */
	public void setConcurrents(int concurrents) {
		this.concurrents = concurrents;
	}

	/**
	 * @return the queues
	 */
	public int getQueues() {
		return queues;
	}

	/**
	 * @param queues the queues to set
	 */
	public void setQueues(int queues) {
		this.queues = queues;
	}

	/**
	 * @return the threadPool
	 */
	public String getThreadPool() {
		return threadPool;
	}

	/**
	 * @param threadPool the threadPool to set
	 */
	public void setThreadPool(String threadPool) {
		this.threadPool = threadPool;
	}

	/**
	 * @return the language
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * @param language the language to set
	 */
	public void setLanguage(String language) {
		this.language = language;
	}

	/**
	 * @return the weight
	 */
	public String getWeight() {
		return weight;
	}

	/**
	 * @param weight the weight to set
	 */
	public void setWeight(String weight) {
		this.weight = weight;
	}

	/**
     * 获取当前进程的pid
     * @return
     */
    private int getPid() {
        if (pid == 0) {
            pid = Integer.parseInt(ManagementFactory.getRuntimeMXBean().getName().split("@")[0]);
        }
        return pid;
    }

    /**
     * @return the alarmUmpJvmKey
     */
    public String getAlarmUmpJvmKey() {
        return alarmUmpJvmKey;
    }

    /**
     * @param alarmUmpJvmKey the alarmUmpJvmKey to set
     */
    public void setAlarmUmpJvmKey(String alarmUmpJvmKey) {
        this.alarmUmpJvmKey = alarmUmpJvmKey;
    }

    /**
     * @return the alarmUmpAliveKey
     */
    public String getAlarmUmpAliveKey() {
        return alarmUmpAliveKey;
    }

    /**
     * @param alarmUmpAliveKey the alarmUmpAliveKey to set
     */
    public void setAlarmUmpAliveKey(String alarmUmpAliveKey) {
        this.alarmUmpAliveKey = alarmUmpAliveKey;
    }

    /**
     * @return the httpToken
     */
    public String getHttpToken() {
        return httpToken;
    }

    /**
     * @param httpToken the httpToken to set
     */
    public void setHttpToken(String httpToken) {
        this.httpToken = httpToken;
    }

	/**
	 * @return the conThreshold
	 */
	public int getConThreshold() {
		return conThreshold;
	}

	/**
	 * @param conThreshold the conThreshold to set
	 */
	public void setConThreshold(int conThreshold) {
		this.conThreshold = conThreshold;
	}

	/**
	 * @return the conDelta
	 */
	public int getConDelta() {
		return conDelta;
	}

	/**
	 * @param conDelta the conDelta to set
	 */
	public void setConDelta(int conDelta) {
		this.conDelta = conDelta;
	}
}
