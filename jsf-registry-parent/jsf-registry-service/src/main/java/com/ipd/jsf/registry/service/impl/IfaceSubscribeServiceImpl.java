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

import com.alibaba.fastjson.JSON;
import com.ipd.jsf.gd.error.CallbackStubException;
import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.common.constant.RouterConstants;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.IfacePropertyType;
import com.ipd.jsf.registry.callback.SubscribeCallback;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.*;
import com.ipd.jsf.registry.manager.*;
import com.ipd.jsf.registry.recoder.CallbackRecoder;
import com.ipd.jsf.registry.service.CallbackLogService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.CalculateHelper;
import com.ipd.jsf.registry.service.helper.ConfigHelper;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.service.helper.WbCacheHelper;
import com.ipd.jsf.registry.threadpool.WorkerThreadPool;
import com.ipd.jsf.registry.util.ListPaging;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.registry.vo.InstanceVo;
import com.ipd.jsf.registry.vo.InterfaceCacheVo;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;


/**
 * 订阅provider
 */
@Service
public class IfaceSubscribeServiceImpl implements SubscribeService {
    private Logger logger = LoggerFactory.getLogger(IfaceSubscribeServiceImpl.class);
    
    private float loadFactor = 0.75f;

    //加载接口的时间，用来比较saf_interface_version表中的update_time
    private long interfaceLoadLastTime = RegistryConstants.ORIGINAL_TIME;

    //记录上次加载接口监控配置信息的时间
    private long propertyLoadLastTime = RegistryConstants.ORIGINAL_TIME;

    //判断服务列表是否加载完成
    private volatile boolean isProviderLoading = false;

    //判断配置是否加载完成
    private volatile boolean isConfigLoading = false;

    private volatile long globalsettingVersion = 0l;

    //客户端配置缓存
    private ConcurrentHashMap<String, String> globalConfigMapCache = new ConcurrentHashMap<String, String>(1, loadFactor, 1);

    // 加载接口的provider 队列, 将接口id放入队列
//    private LinkedBlockingQueue<Integer> ifaceProviderQueue = new LinkedBlockingQueue<Integer>(60000);
    private LinkedBlockingQueue<List<IfaceAliasVersion>> ifaceAliasVersionQueue = new LinkedBlockingQueue<List<IfaceAliasVersion>>(60000);

    // 加载接口的provider 队列, 将接口id放入队列
    private LinkedBlockingQueue<Integer> ifaceConfigQueue = new LinkedBlockingQueue<Integer>(30000);

    //访问数据库的线程池
    private WorkerThreadPool fetchDBDataThreadPool = new WorkerThreadPool(6, 8, "reg_db");

    //通知线程池
    private WorkerThreadPool notifyThreadPool = new WorkerThreadPool(10, 10, "reg_notify");

    @Autowired
    private InterfaceManager interfaceInfoManagerImpl;

    @Autowired
    private ServerManager serverManagerImpl;

    @Autowired
    private ServerAliasManager serverAliasManagerImpl;

    @Autowired
    private ServerSetManager serverSetManagerImpl;

    @Autowired
    private InterfacePropertyManager interfacePropertyManager;

    @Autowired
    private IpwbManager ipwbManager;

    @Autowired
    private SysParamManager sysParamManagerImpl;

    @Autowired
    private ServerRouterManager serverRouterManagerImpl;

    @Autowired
    private CallbackLogManager callbackLogManagerImpl;
    
    @Autowired
    private InterfaceMockManager interfaceMockManagerImpl;

    @Autowired
    private AppIfaceManager appIfaceManagerImpl;

    @Autowired
    private CallbackLogService callbackLogServiceImpl;

    @Autowired
    private SubscribeHelper subscribeHelper;
    
    @Autowired
    private ConfigHelper configHelper;

    @Autowired
    private WbCacheHelper wbCacheHelper;

    @Autowired
    private CalculateHelper calculateHelper;

    @PostConstruct
    public void init() {
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.execute(new Runnable() {
            @Override
            public void run() {
                loadServerAsyn();
            }
        });
        service.execute(new Runnable() {
            @Override
            public void run() {
                loadConfigAsyn();
            }
        });
    }

    /**
     * 异步加载provider
     * 从ifaceProviderQueue中取出需要加载的IfaceAliasVersion，然后刷新相应接口的provider列表
     */
    private void loadServerAsyn() {
        List<IfaceAliasVersion> list = new ArrayList<IfaceAliasVersion>();
        int limit = 200;
        int limitTime = 2000; //2秒
        long time = 0;
        while (true) {
            try {
                list.clear();
                while (true) {
                    try {
                        if (ifaceAliasVersionQueue.isEmpty()) {
                            Thread.sleep(1000);
                        }
                        List<IfaceAliasVersion> tmpVersion = ifaceAliasVersionQueue.poll(100, TimeUnit.MILLISECONDS);
                        if (tmpVersion != null) {
                            list.addAll(tmpVersion);
                        }
                        long currentTime = System.currentTimeMillis();
                        if (list.size() > limit
                                || ((currentTime - time) > limitTime && ifaceAliasVersionQueue.isEmpty())) {
                            time = currentTime;
                            break;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (!list.isEmpty()) {
                    logger.info("+++++++++++++++++++++loadServerAsyn, interface:{}", list);
                    forceReloadByAliasVersion(list);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 异步加载配置
     * 从ifaceConfigQueue中取出需要加载的interfaceId，并去重，然后刷新相应接口的配置信息
     */
    private void loadConfigAsyn() {
        Set<Integer> set = new HashSet<Integer>();
        int limit = 50;
        int limitTime = 5000; //5秒
        long time = System.currentTimeMillis();
        while (true) {
            try {
                set.clear();
                while (true) {
                    try {
                        if (ifaceConfigQueue.isEmpty()) {
                            Thread.sleep(2000);
                        }
                        Integer interfaceId = ifaceConfigQueue.poll();
                        if (interfaceId != null) {
                            set.add(interfaceId);
                        }
                        long currentTime = System.currentTimeMillis();
                        if (set.size() > limit || (currentTime - time) > limitTime) {
                            time = currentTime;
                            break;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (!set.isEmpty()) {
                    List<InterfaceInfo> result = interfaceInfoManagerImpl.getByIdListForConfig(new ArrayList<Integer>(set));
                    loadInterfaceConfigCache(result);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 订阅provider列表
     */
    @Override
    public List<JsfUrl> subscribe(String ifaceName, String alias, int protocolType, String jsfVersion, String serialization, String insKey, String clientIp, SubscribeCallback<SubscribeUrl> callback, int appId) throws Exception {
        //获取缓存InterfaceVo对象
        InterfaceCacheVo ifaceVo = subscribeHelper.getInterfaceVo(ifaceName);
        if (ifaceVo == null) {
            //如果ifaceName对应的InterfaceVo对象不存在，就创建一个InterfaceVo对象，用于记录callback
            InterfaceInfo ifaceInfo = getByName(ifaceName);
            if (ifaceInfo != null) {
                ifaceVo = new InterfaceCacheVo(ifaceInfo);
            } else {
                //接口不存在，直接返回空列表
                return new ArrayList<JsfUrl>();
            }
        }

        //如果还没加载，就立刻加载
        if (ifaceVo.serverLoadLastTime == RegistryConstants.ORIGINAL_TIME) {
            if (ifaceVo.getInterfaceName() != null) {
                List<Integer> list = new ArrayList<Integer>();
                list.add(ifaceVo.getInterfaceId());
                //加载接口服务
                reloadByInterfaceId(list);
                //加载接口配置
                forceReloadInterfaceConfig(list);
            }
        }

        List<Server> serverList = new ArrayList<Server>();
        //从aliasServerMap中找到对应的serverList
        if (wbCacheHelper.checkCanVisit(ifaceName, clientIp)) {
            if (alias == null) {
                alias = "";
            }
            List<Server> serverCacheList = ifaceVo.aliasServersMap.get(alias);
            if (logger.isDebugEnabled()) {
                logger.debug("subscribe, interfaceName:{}, alias:{}, serverCacheList:{}", ifaceName, alias, serverCacheList);
            }
            if (!CollectionUtils.isEmpty(serverCacheList)) {
            	serverList.addAll(serverCacheList);
            }
        }

        if (callback != null && insKey != null) {
            //保存callback到缓存中
        	subscribeHelper.putInstanceCallback(ifaceName, insKey, callback, appId);
            //将订阅信息保存到instanceCache中
            subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName).add(new AliasProtocolVo(alias, protocolType, jsfVersion, serialization));
            subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet().add(insKey);
        }
        return calculateHelper.filterServers(ifaceName, serverList, clientIp, protocolType, jsfVersion, serialization, 0);
    }

    @Override
    public void unSubscribe(String ifaceName, String insKey) throws Exception {
        if (subscribeHelper.getInstanceCache().get(insKey) != null) {
            subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().remove(ifaceName);
        }
        if (subscribeHelper.getInterfaceVo(ifaceName) != null) {
            subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet().remove(insKey);
        }
    }

    /**
     * 用于刷新缓存
     * 1.加载失效接口
     * 2.加载全局配置信息
     * 3.刷新接口信息及每个接口的server信息
     * 4.刷新配置信息
     */
    @Override
    public void refreshCache(int loadInterval) throws Exception {
        subscribeHelper.loadInvalidInterface();
        loadConfig();
        loadInterfaceCacheSchedule();
    	loadInterfaceConfigCacheSchedule();
    }

    /**
     * 用于刷新缓存
     * 1.取出全部的接口信息
     * 2.取出每个接口的server信息
     * 3.如果server有变化，就通过callback 回调 客户端，将更新的server信息发送给客户端
     * 
     * 多线程取server信息，考虑数据库线程池中的线程个数限制，获取server的线程池的线程个数也要限制
     * @throws Exception
     */
    private void loadInterfaceCacheSchedule() throws Exception {
        long start = System.currentTimeMillis();
        List<InterfaceInfo> ifaceList = null;
        if (subscribeHelper.isIfaceCacheEmpty() || interfaceLoadLastTime == RegistryConstants.ORIGINAL_TIME) {
            ifaceList = interfaceInfoManagerImpl.getAllListForProvider();
        } else {
            ifaceList = interfaceInfoManagerImpl.getChangeListByUpdateTimeForProvider(getLoadDateTime());
        }
        logger.info("start loadInterface , size:{}, load version:{}, load time:{} ..............", ifaceList == null ? 0 : ifaceList.size(), getLoadDateTime(), new Date(getLoadDateTime()));
        if (loadInterfaceCacheLock(ifaceList)) {  //如果加载成功，就更新加载时间
            interfaceLoadLastTime = start;
        }
        long end = System.currentTimeMillis();
        logger.info("loadInterface finish , elapse: {}ms..............", end - start);
    }

    /**
     * 增加了加载锁，不能同时加载. 返回true说明已经加载完，如果返回
     * @param ifaceList
     * @return
     * @throws Exception 
     */
    private boolean loadInterfaceCacheLock(List<InterfaceInfo> ifaceList) throws Exception {
        if (CollectionUtils.isEmpty(ifaceList)) {
            return true;
        }
        boolean result = false;
        if (isProviderLoading) {  //其他线程正在加载，直接退出
            return result;
        }
        try {
            logger.info("loadInterface,  interface size: {}", ifaceList.size());
            isProviderLoading = true;
            loadInterfaceCache(ifaceList);
            result = true;
        } finally {  //如果有异常，先设置isProviderLoading为false，再抛
            isProviderLoading = false;
        }
        return result;
    }

    /**
     * 过滤需要加载的接口，然后分批加载
     * 1. 根据加载时间戳、版本号，筛选出必须加载的接口
     * 2. 分批放入线程池中进行加载
     * @param ifaceList
     * @throws Exception
     */
    private void loadInterfaceCache(List<InterfaceInfo> ifaceList) throws Exception {

        if (ifaceList == null || ifaceList.isEmpty()) return;
        int fetchLimitCount = 10;
        List<Future<Boolean>> taskFutureList = new ArrayList<Future<Boolean>>();
        //key:interfaceId
        Map<Integer, InterfaceInfo> interfaceMap = new HashMap<Integer, InterfaceInfo>();
        List<IfaceAliasVersion> versionList = null;
        final long currLoadTime = System.currentTimeMillis();
        for (int i = 0; i < ifaceList.size(); i++) {
            // 1. 根据加载时间戳、版本号，筛选出必须加载的接口
            InterfaceInfo iface = ifaceList.get(i);
            versionList = new ArrayList<IfaceAliasVersion>();
            //如果不存在，就往内存里添加新的vo接口
            if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()) == null) {
                subscribeHelper.putInterfaceVo(iface.getInterfaceName(), new InterfaceCacheVo(iface));
                versionList.addAll(iface.getVersionList());
            } else {
            	for (IfaceAliasVersion version : iface.getVersionList()) {
            		//比较alias版本号，如果不一样，就需要刷新
                    if (CollectionUtils.isEmpty(subscribeHelper.getInterfaceVo(iface.getInterfaceName()).versionMap) 
                    		|| subscribeHelper.getInterfaceVo(iface.getInterfaceName()).versionMap.get(version.getAlias()) == null
                    		|| subscribeHelper.getInterfaceVo(iface.getInterfaceName()).versionMap.get(version.getAlias()).longValue() < version.getDataVersion()) {
                        //验证版本号，如果不一致，就强制刷新。
                    	versionList.add(version);
                    }
            	}
            }
            //如果检查alias版本有变化的，就要加载
            if (!versionList.isEmpty()) {
        		iface.setVersionList(versionList);
            	//将需要加载的放到interfaceMap中
                interfaceMap.put(iface.getInterfaceId(), iface);
            } else {
                logger.info(">>>>>>>>>>>   不用刷新了, 已经刷过了, {}", iface.getInterfaceName());
            }

            //累积到fetchLimitCount 时，就批量放入线程池中
            if (interfaceMap.size() >= fetchLimitCount || i == ifaceList.size() - 1) {
                //key: interfaceId
                final Map<Integer, InterfaceInfo> temp = new HashMap<Integer, InterfaceInfo>();
                temp.putAll(interfaceMap);
                Future<Boolean> future = fetchDBDataThreadPool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        if (temp.keySet() != null && !temp.keySet().isEmpty()) {
                            try {
                                List<Integer> interfaceIdList = new ArrayList<Integer>(temp.keySet());
                                List<InterfaceInfo> interfaceInfoList = new ArrayList<InterfaceInfo>(temp.values());//add by wt
                                List<IfaceAliasVersion> ifaceAliasList = subscribeHelper.getIfaceAliasList(interfaceInfoList);
                                //接口下的alias和serverId的list
                                List<ServerAlias> serverAliasList = serverAliasManagerImpl.getAliasServersByIfaceIdAliasList(ifaceAliasList);
                                ifaceAliasList = subscribeHelper.mergeIfaceAliasList(ifaceAliasList, serverAliasList);
                                logger.debug("======================merge alias list:{}, ---------serverAliasList:{}", ifaceAliasList, serverAliasList);
                                //接口和server的map，key : interfaceId
                                Map<Integer, List<Server>> interfaceServersMap = serverManagerImpl.getServersByIfaceIdAliasList(ifaceAliasList);
                                //key：接口，value：alias
                                Map<Integer, Set<String>> loadIfaceIdAliasMap = subscribeHelper.getIfaceAliasMap(ifaceAliasList);
                                logger.debug("-----------------------interfaceServersMap:{}", interfaceServersMap);
                                //加载接口的server配置下发信息
                                Map<Integer, Map<Integer, Map<String, String>>> ifaceServerConfigMap = serverSetManagerImpl.getListByInterfaceIdList(interfaceIdList);
                                //加载配置信息（主要是同机房优先的配置）,key: interfaceName, key1:propertyName
                                Map<String, Map<String, String>> ifacesPropertyMap = interfacePropertyManager.getListByInterfaceIdList(IfacePropertyType.REGISTRY.value().intValue(), interfaceInfoList);  //每次根据增量接口取得
                                //接口动态分组，扩展alias的map, key1:interfaceId, key2:alias
                                Map<Integer, Map<String, List<Integer>>> extendsMap = RegistryUtil.getMapFromServerAliasList(serverAliasList, (byte)DataEnum.AliasType.Extend.getValue());
                                //接口动态分组，替换alias的map, key1:interfaceId, key2:alias
                                Map<Integer, Map<String, List<Integer>>> replaceMap = RegistryUtil.getMapFromServerAliasList(serverAliasList, (byte)DataEnum.AliasType.Roadmap.getValue());
                                // add by wt ip路由规则加载
                                Map<String, Map<String, List<String>>> routerMap = serverRouterManagerImpl.getServerRouterByInterfaceIdList(interfaceInfoList, RouterConstants.ROUTER_IP_TYPE);
                                //开始对每个接口下的server列表进行处理
                                for (InterfaceInfo ifaceInfo : temp.values()) {
                                	Set<String> loadAliasSet = loadIfaceIdAliasMap.get(ifaceInfo.getInterfaceId());
                                    //map key:serverId
                                    Map<Integer, Server> newServersMap = RegistryUtil.getMapFromList(interfaceServersMap.get(ifaceInfo.getInterfaceId()));
                                    //map key:serverId
                                    Map<Integer, Map<String, String>> serverSetConfigMap = ifaceServerConfigMap == null ? null : ifaceServerConfigMap.get(ifaceInfo.getInterfaceId());
                                    //map key:alias, value:serverId
                                    Map<String, List<Integer>> newAliasServerIds = extendsMap.get(ifaceInfo.getInterfaceId());
                                    //map key:alias, value:serverId
                                    Map<String, List<Integer>> aliasServerIdReplaceMap = replaceMap.get(ifaceInfo.getInterfaceId());
                                    //map key:ip,  value: ip
                                    Map<String, List<String>> ipRouterMap = routerMap.get(ifaceInfo.getInterfaceName());
                                    if (newAliasServerIds == null) {
                                        newAliasServerIds = new HashMap<String, List<Integer>>();
                                    }
                                    //map key:propertyName, value:value
                                    Map<String, String> ifacePropertyMap = ifacesPropertyMap.get(ifaceInfo.getInterfaceName());
                                    //替换server的属性，将数据库中保存的下发配置更新到server的属性中
                                    subscribeHelper.replaceServerConfig(newServersMap, serverSetConfigMap);
                                    //按照alias的维度，合并serverId并去除serverId
                                    subscribeHelper.mergeAliasServer(newAliasServerIds, aliasServerIdReplaceMap, newServersMap, loadAliasSet);
                                    Map<String, List<Server>> newAliasServers = subscribeHelper.convertServerIdToServer(newAliasServerIds, newServersMap);
                                    logger.debug("-----------------------------------newAliasServers:{}", newAliasServers);
                                    //更新缓存并通知客户端
                                    loadServers(ifaceInfo, newAliasServers, ipRouterMap, currLoadTime, ifacePropertyMap);
                                }
                            } catch (Exception e) {
                                logger.error("loadInterfaceCache interface id list:" + temp.values(), e);
                            }
                        }
                        return true;
                    }
                });
                taskFutureList.add(future);
                interfaceMap.clear();
            }
        }
        //循环获取任务执行结果
        for (Future<Boolean> task : taskFutureList) {
            try {
                task.get();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 获取加载时间
     * @return
     */
    private long getLoadDateTime() {
        return interfaceLoadLastTime - 2000L;
    }

    /**
     * 更新缓存中接口下的server数据
     * 1. 如果interface不在ifaceCacheHelper.ifaceCache中，就将interface放入ifaceCacheHelper.ifaceCache中
     * 2. 如果上次加载server的时间到现在，小于一个时间周期，则本次不加载，下一次加载
     * 将通知的任务放入通知队列中
     * @param iface
     * @param newAliasServersMap
     * @param newIpRouterMap
     * @param currLoadTime
     * @param ifacePropertyMap
     * @throws Exception
     */
    private void loadServers(InterfaceInfo iface, final Map<String, List<Server>> newAliasServersMap, final Map<String, List<String>> newIpRouterMap, long currLoadTime, final Map<String, String> ifacePropertyMap) throws Exception {
        //更新server列表
        if (iface.getInterfaceId() > 0) {
            if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()) == null) {
                logger.warn("ifaceName:{} is not exists, please check db", iface.getInterfaceName());
            }
            //1.获取旧的server列表，然后更新alias对应的server列表. 有一种情况需要考虑，如果oldAliasServerMap有数据，最新获取的newAliasServerMap为空，是否将通知客户端清空
            final Map<String, List<Server>> oldAliasServersMap = new HashMap<String, List<Server>>();
            if (!CollectionUtils.isEmpty(newAliasServersMap)) {
                //更新加载时间
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).serverLoadLastTime = currLoadTime;
                //更新server列表
                for (Map.Entry<String, List<Server>> entry : newAliasServersMap.entrySet()) {
                	//获取alias的旧的server列表
                	oldAliasServersMap.put(entry.getKey(), new ArrayList<Server>());
                	if (!CollectionUtils.isEmpty(subscribeHelper.getInterfaceVo(iface.getInterfaceName()).aliasServersMap.get(entry.getKey()))) {
                		//获取旧的服务列表
                		oldAliasServersMap.get(entry.getKey()).addAll(subscribeHelper.getInterfaceVo(iface.getInterfaceName()).aliasServersMap.get(entry.getKey()));
                	}
                	//更新缓存中的新的server列表
                	subscribeHelper.getInterfaceVo(iface.getInterfaceName()).aliasServersMap.put(entry.getKey(), entry.getValue());
                }
            }
//            if (logger.isDebugEnabled()) {
//                logger.info("load Server alias new list:{}", newAliasServersMap);
//                logger.info("load Server alias old list:{}", oldAliasServersMap);
//            }
            //2.取出旧的ip路由，然后更新ip路由 add by wt
            final Map<String, List<String>> oldIpRouterMap = subscribeHelper.getInterfaceVo(iface.getInterfaceName()).cloneIpRouterMap();
            if (!CollectionUtils.isEmpty(newIpRouterMap)) {
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).putAllIpRouterMap(newIpRouterMap);
            } else {
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).destroyIpRouterMap();
            }
            //4.更新同机房优先策略
            final byte newRoomStrategy = subscribeHelper.getRoomStrategy(ifacePropertyMap);
            final byte oldRoomStrategy = subscribeHelper.getInterfaceVo(iface.getInterfaceName()).roomStrategy;
            subscribeHelper.getInterfaceVo(iface.getInterfaceName()).roomStrategy = newRoomStrategy;
            //5.更新推送服务列表个数限制
            final int newProviderLimit = subscribeHelper.getProviderLimit(ifacePropertyMap);
            final int oldProviderLimit = subscribeHelper.getInterfaceVo(iface.getInterfaceName()).providerLimit;
            subscribeHelper.getInterfaceVo(iface.getInterfaceName()).providerLimit = newProviderLimit;

            for (IfaceAliasVersion version : iface.getVersionList()) {
                //更新版本号
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).versionMap.put(version.getAlias(), version.getDataVersion());
            }

            if ((!oldAliasServersMap.isEmpty() || !newAliasServersMap.isEmpty()) && !subscribeHelper.getInterfaceVo(iface.getInterfaceName()).getInsKeySet().isEmpty()) {
                final String ifaceName = iface.getInterfaceName();
                //放入通知线程池中
                notifyThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                        	logger.debug("ifaceName:{}, newAliasServersMap = {}", ifaceName, newAliasServersMap);
                        	logger.debug("ifaceName:{}, oldAliasServersMap = {}", ifaceName, oldAliasServersMap);
							calculateHelper.compareAndNotifyClients(ifaceName, newAliasServersMap, oldAliasServersMap, newIpRouterMap, oldIpRouterMap, newRoomStrategy, oldRoomStrategy, newProviderLimit, oldProviderLimit);
                        } catch (Exception e) {
                            logger.error("notifyClient error, interfaceName:" + ifaceName + ", error:" + e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }

    /**
     * 加载接口配置信息，包括monitor监控属性、ip黑白名单
     * 1.通过propertyLastTime判断，将数据库中大于propertyLastTime时间的数据取出来
     * 2.放入缓存中
     * 3.将变化的数据推送到客户端
     * @throws Exception
     */
    private void loadInterfaceConfigCacheSchedule() throws Exception {
        long currLoadTime = System.currentTimeMillis();
        List<InterfaceInfo> list = interfaceInfoManagerImpl.getChangeListByConfigUpdateTime(DateTimeZoneUtil.getTargetTime(propertyLoadLastTime));
        logger.info("start loadInterfaceConfig , size:{}..............", list == null ? 0 : list.size());
        if (loadInterfaceConfigCacheLock(list)) {
            propertyLoadLastTime = currLoadTime;
        }
        long end = System.currentTimeMillis();
        logger.info("loadInterface config  finish, elapse:{}ms ..............", end - currLoadTime);
    }

    /**
     * 增加了加载锁，不能同时加载. 返回true说明已经加载完，如果返回
     * @param ifaceList
     * @return
     * @throws Exception 
     */
    private boolean loadInterfaceConfigCacheLock(List<InterfaceInfo> ifaceList) throws Exception {
        if (ifaceList == null || ifaceList.size() == 0) {
            return true;
        }
        logger.info("loadInterfaceConfig,  interface size: {}", ifaceList.size());
        boolean result = false;
        if (isConfigLoading) {   //其他线程正在加载，直接退出
            return result;
        }
        try {
            isConfigLoading = true;
            loadInterfaceConfigCache(ifaceList);
            result = true;
        } finally {  //如果有异常，先设置isConfigLoading为false，再抛
            isConfigLoading = false;
        }
        return result;
    }

    /**
     * 获取接口配置, 包括接口监控配置，和接口的黑白名单
     * @return
     * @throws Exception
     */
    private void loadInterfaceConfigCache(List<InterfaceInfo> list) throws Exception {
        List<InterfaceInfo> refreshIfaceList = new ArrayList<InterfaceInfo>();
        //比较版本号
        for (InterfaceInfo iface : list) {
            boolean needRefresh = false;
            if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()) == null) {
                subscribeHelper.putInterfaceVo(iface.getInterfaceName(), new InterfaceCacheVo(iface));
                needRefresh = true;
            } else if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()).getInterfaceName() == null) {
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).setIface(iface);
                needRefresh = true;
            } else if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()).getConfigUpdateTime() == 0 || iface.getConfigUpdateTime() == null) { //如果没有版本号，也需要加载
                needRefresh = true;
            } else if (subscribeHelper.getInterfaceVo(iface.getInterfaceName()).getConfigUpdateTime() != iface.getConfigUpdateTime().getTime()) { //版本号不一致,需要加载
                needRefresh = true;
            }
            if (iface.getConfigUpdateTime() != null) {   //更新版本号
                subscribeHelper.getInterfaceVo(iface.getInterfaceName()).setConfigUpdateTime(iface.getConfigUpdateTime().getTime());
            }
            if (needRefresh) {
                refreshIfaceList.add(iface);
            }
        }

        //筛选后，如果refreshIfaceList为空，则返回
        if (!refreshIfaceList.isEmpty()) {
            int itemLimit = 100;
            ListPaging<InterfaceInfo> paginglist = new ListPaging<InterfaceInfo>(refreshIfaceList, itemLimit);
            final CountDownLatch latch = new CountDownLatch(paginglist.getTotalPage());
            while (paginglist.hasNext()) {
            	final List<InterfaceInfo> subList = paginglist.nextPageList();
            	if (subList.isEmpty()) break;
                //批量加载配置
                fetchDBDataThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            updateInterfaceConfigByIfaceList(subList);
                            latch.countDown();
                        } catch (Exception e) {
                            logger.error("batch update iface config error:" + e.getMessage(), e);
                        }
                    }
                });
            }
            latch.await();
        }
    }

    /**
     * 批量加载配置。
     * 此处加载的配置都是客户端需要的配置信息
     * 由于黑白名单影响到consumer端的服务列表订阅，所以在此单独更新consumer端的服务列表
     * @param refreshIfaceList
     * @throws Exception
     */
    private void updateInterfaceConfigByIfaceList(List<InterfaceInfo> refreshIfaceList) throws Exception {
        //根据refreshIfaceList加载配置
        Map<String, Map<String, String>> result = interfacePropertyManager.getListByInterfaceIdList(IfacePropertyType.MONITOR.value().intValue(), refreshIfaceList);  //每次根据增量接口取得
        Map<String, Map<String, List<Ipwblist>>> ipwbMap = ipwbManager.getListByInterfaceIdList(refreshIfaceList);  //每次根据增量接口取得
        //路由规则加载 add by wt
        Map<String, Map<String, List<String>>> ifaceParamRouterMap = serverRouterManagerImpl.getServerRouterByInterfaceIdList(refreshIfaceList,RouterConstants.ROUTER_PARAM_TYPE);
        Map<String, Map<String, List<String>>> ifaceAliasRouterMap = serverRouterManagerImpl.getServerRouterByInterfaceIdList(refreshIfaceList,RouterConstants.ROUTER_ALIAS_TYPE);
        Map<String, Map<String, Map<String, String>>> mockMap = interfaceMockManagerImpl.getListByInterfaceIdList(refreshIfaceList);
        Map<String, Map<String, String>> appIfaceMap = appIfaceManagerImpl.getListByInterfaceIdList(refreshIfaceList);
        //合并黑白名单  添加和删除
        Map<String, Map<String, String>> wbTempMap = new HashMap<String, Map<String,String>>();
        String interfaceName = null;
        Map<String,Map<String,String>> tmpParamRouterMapForUpdate = new HashMap<String, Map<String,String>>();//add by wt
        Map<String,Map<String,String>> tmpAliasRouterMapForUpdate = new HashMap<String, Map<String,String>>();
        for (InterfaceInfo iface : refreshIfaceList) {
            interfaceName = iface.getInterfaceName();
            if (result.get(interfaceName) == null) {
                result.put(interfaceName, new HashMap<String, String>());
            }
            //设置黑白名单
            wbCacheHelper.putWbMap(ipwbMap, wbTempMap, interfaceName);
            //设置参数路由
            configHelper.putParamRouterMap(interfaceName, ifaceParamRouterMap.get(interfaceName), tmpParamRouterMapForUpdate);
            //设置分组路由
            configHelper.putAliasRouterMap(interfaceName, ifaceAliasRouterMap.get(interfaceName), tmpAliasRouterMapForUpdate);
        }

        //合并黑白名单, 并获取原来的黑白名单访问记录缓存
        Map<String, Map<String, Boolean>> oldWbCheckRecordCache = wbCacheHelper.mergeConfigWb(result, wbTempMap);
        //add by wt 将接口配置和参数路由配置合并
        configHelper.mergeConfigParamRouter(result, tmpParamRouterMapForUpdate);
        //将接口配置和分组路由配置合并
        configHelper.mergeConfigAliasRouter(result, tmpAliasRouterMapForUpdate);
        //将接口配置和app的iface配置合并
        configHelper.mergeConfigApp(result, appIfaceMap);
        if (!mockMap.isEmpty())  logger.info("load mock config:{}", mockMap);
        updateInterfaceConfigMapCache(result, oldWbCheckRecordCache, mockMap);
    }

    /**
     * @param changeConfigMap   变化配置
     * @param wbCheckRecordOldCache   以前黑白名单访问ip记录
     */
    private void updateInterfaceConfigMapCache(Map<String, Map<String, String>> changeConfigMap, Map<String, Map<String, Boolean>> wbCheckRecordOldCache, Map<String, Map<String, Map<String, String>>> changeMockMap) {
        if (logger.isDebugEnabled()) {
            logger.debug(changeConfigMap.toString());
        }
        if (changeConfigMap != null && !changeConfigMap.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.info("put interface config : {}", changeConfigMap);
            }
            final Map<String, Map<String, Map<String, String>>> tempChangeMockMap = changeMockMap;
            for (Map.Entry<String, Map<String, String>> entry : changeConfigMap.entrySet()) {
                final String ifaceName = entry.getKey();
                final Map<String, String> config = entry.getValue();
                final Map<String, Boolean> wbCheckRecordOldIpMap = wbCheckRecordOldCache.get(entry.getKey());
                try {
                    if (subscribeHelper.getInterfaceVo(ifaceName) == null) {
                        logger.warn("ifaceName:{} is not exists, please check db", ifaceName);
                        continue;
                    }
                    //将配置放入缓存中
                    subscribeHelper.getInterfaceVo(ifaceName).propertyMap.putAll(config);
                    //add by wt 修改参数路由，去掉已经删除了的参数路由配置
                    if (subscribeHelper.getInterfaceVo(ifaceName).cloneParamRouterMap().isEmpty()) {
                        String paramRouter = subscribeHelper.getInterfaceVo(ifaceName).propertyMap.put(Constants.SETTING_ROUTER_RULE, "");
                        config.put(Constants.SETTING_ROUTER_RULE,"");//推送给客户端去做判断
                        if (paramRouter != null && !"".equals(paramRouter)) {
                            logger.info(" removed {} param router:{} ", ifaceName, paramRouter);
                        }
                    }
                    //修改分组路由，去掉已经删除了的分组路由配置
                    if (subscribeHelper.getInterfaceVo(ifaceName).cloneAliasRouterMap().isEmpty()) {
                    	String aliasRouter = subscribeHelper.getInterfaceVo(ifaceName).propertyMap.put(RegistryConstants.SETTING_MAP_PARAM_ALIAS, "");
                    	config.put(RegistryConstants.SETTING_MAP_PARAM_ALIAS, "");//推送给客户端去做判断
                    	if (aliasRouter != null && !"".equals(aliasRouter)) {
                    		logger.info(" removed {} alias router:{} ", ifaceName, aliasRouter);
                    	}
                    }
                    //设置mock的参数. 先清空再设置参数
                    subscribeHelper.getInterfaceVo(ifaceName).clearMockMap();
                    if (changeMockMap != null && changeMockMap.get(entry.getKey()) != null && !changeMockMap.get(entry.getKey()).isEmpty()) {
                        subscribeHelper.getInterfaceVo(ifaceName).putAllMockMap(changeMockMap.get(ifaceName));
                    }
                    if (subscribeHelper.getInterfaceVo(ifaceName) !=null && subscribeHelper.getInterfaceVo(ifaceName).cloneMockMap().isEmpty()) {
                        //如果赋值后，仍为空，就释放资源
                        subscribeHelper.getInterfaceVo(ifaceName).destroyMockMap();
                    }
                    if (!subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet().isEmpty()) {
                        notifyThreadPool.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    long configDataVersion = subscribeHelper.getInterfaceConfigDataVersion(ifaceName);
                                    //推送配置信息到客户端
                                    JsfUrl jsfUrl = new JsfUrl();
                                    jsfUrl.setIface(ifaceName);
                                    jsfUrl.setDataVersion(configDataVersion);
                                    jsfUrl.setAlias(null);
                                    jsfUrl.setProtocol(0);
                                    SubscribeUrl url = new SubscribeUrl();
                                    url.setSourceUrl(jsfUrl);
                                    List<String> aliasList = new ArrayList<String>();
                                    //遍历接口的每个实例
                                    for (String insKey : subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet()) {
                                        aliasList.clear();
                                        if (subscribeHelper.isCallbackNull(insKey)) {
                                            continue;
                                        }
                                        String clientIp = subscribeHelper.getInstanceCache().get(insKey).getCallback().getClientIp();
                                        //根据ip值，推送mock配置
                                        if (tempChangeMockMap.get(ifaceName) != null && tempChangeMockMap.get(ifaceName).get(clientIp) != null && !tempChangeMockMap.get(ifaceName).get(clientIp).isEmpty()) {
                                            config.putAll(tempChangeMockMap.get(ifaceName).get(clientIp));
                                        }
                                        //通知实例更新接口配置
                                        subscribeHelper.notifyInstanceToUpdateConfig(ifaceName, config, url, insKey, SubscribeUrl.CONFIG_UPDATE);
                                        //再次判断下callback
                                        if (subscribeHelper.isCallbackNull(insKey)) {
                                            continue;
                                        }
                                        //根据黑白名单的变化，重新判断每个consumer是否能访问服务列表，并通知consumer
                                        if (!wbCacheHelper.checkCanVisit(ifaceName, clientIp)) {
                                            logger.info("checkCanVisit is false, ifaceName:{}, clientIP:{}, insKey:{}, aliasProtocolMap:{}", ifaceName, clientIp, insKey, subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName));
                                            if (subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName) != null) {
                                                String alias = null;
                                                for (AliasProtocolVo aliasProtocolVo : subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName)) {
                                                    alias = aliasProtocolVo.getAlias();
                                                    if (!aliasList.contains(alias)) {  //防止重复通知，需要判断
                                                        aliasList.add(alias);
                                                        subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, SubscribeUrl.PROVIDER_CLEAR, alias, null, insKey);
                                                    }
                                                }
                                            }
                                        } else {
                                            //如果检查clientIp的黑白名单后，现在允许访问，但是曾经不能访问的，需要重新推送provider列表给clientIp，恢复clientIp的访问
                                            logger.info("checkCanVisit is true, ifaceName:{}, clientIP:{}, insKey:{}, wbCheckRecordOldIpMap:{}, aliasProtocolMap:{}", ifaceName, clientIp, insKey, wbCheckRecordOldIpMap, subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName));
                                            if (wbCheckRecordOldIpMap != null && !wbCheckRecordOldIpMap.isEmpty()
                                                  && wbCheckRecordOldIpMap.get(clientIp) != null && wbCheckRecordOldIpMap.get(clientIp).booleanValue() == false) {
                                                if (subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName) != null) {
                                                    Map<String, List<Server>> aliasServers = subscribeHelper.getInterfaceVo(ifaceName).aliasServersMap;
                                                    String alias = null;
                                                    for (AliasProtocolVo aliasProtocolVo : subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName)) {
                                                        alias = aliasProtocolVo.getAlias();
                                                        if (!aliasList.contains(alias)) {  //防止重复通知，需要判断
                                                            aliasList.add(alias);
                                                            subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, SubscribeUrl.PROVIDER_UPDATE_ALL, alias, aliasServers.get(alias), insKey);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    logger.error("loadInterfaceConfig error, interface:" + ifaceName + ", error:" + e.getMessage(), e);
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    logger.error("notify config ifaceName:" + ifaceName + ",config:" + config + "," + wbCheckRecordOldIpMap + ",error:" + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 调用客户端callback，重新设置  客户端调用注册中心  的定时器
     * @param insKey
     * @return
     */
    @Override
    public boolean resetRemoteClientSchedule(String insKey) {
        if (subscribeHelper.getInstanceCache().get(insKey) != null && subscribeHelper.getInstanceCache().get(insKey).getCallback() != null) {
            SubscribeUrl url = new SubscribeUrl();
            url.setType(SubscribeUrl.INSTANCE_RESET_SCHEDULED);
            try {
                subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(url);
                //如果没有抛异常，说明已经调用成功(远端的notify异步处理)
                logger.info("-------------------######################  reset remote schedule :{}, {}", insKey, url);
                return true;
            } catch (ClientTimeoutException e) {
                try {
                    //重试下
                    subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(url);
                    //如果没有抛异常，说明已经调用成功(远端的notify异步处理)
                    return true;
                } catch (Exception e1) {
                    logger.error("retry reset " + insKey + " error:" + e.getMessage());
                }
            } catch (Exception e) {
                logger.error("reset " + insKey + " error:" + e.getMessage());
            }
        }
        return false;
    }

    /**
     * 删除实例和callback
     * 如果是强制删除，或者删除时间超过
     * @param insKey
     */
    @Override
    public void removeInstanceCache(String insKey) {
        subscribeHelper.removeInstanceCache(insKey);
    }

    @Override
    public Map<String, String> getInterfaceProperty(String ifaceName, String insKey, SubscribeCallback<SubscribeUrl> callback, String clientIp) throws Exception {
        if (subscribeHelper.getInterfaceVo(ifaceName) == null) {
            subscribeHelper.putInterfaceVo(ifaceName, new InterfaceCacheVo());
        }
        if (callback != null && insKey != null) {
        	subscribeHelper.putInstanceCallback(ifaceName, insKey, callback, 0);
            subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet().add(insKey);
        }
        //如果还没加载，就立刻加载。一般是在注册中心刚启动时，数据初始化还未完成，需要强制刷新下
        if (propertyLoadLastTime == RegistryConstants.ORIGINAL_TIME) {
            InterfaceInfo ifaceInfo = getByName(ifaceName);
            if (ifaceInfo != null) {
                List<Integer> list = new ArrayList<Integer>();
                list.add(ifaceInfo.getInterfaceId());
                //加载接口配置
                forceReloadInterfaceConfig(list);
            }
        }
        if (subscribeHelper.getInterfaceVo(ifaceName) != null && subscribeHelper.getInterfaceVo(ifaceName).propertyMap != null) {
            Map<String, String> config = new HashMap<String, String>(subscribeHelper.getInterfaceVo(ifaceName).propertyMap);
            //根据clientIp筛选mockvalue的值
            Map<String, Map<String, String>> mockMap = subscribeHelper.getInterfaceVo(ifaceName).cloneMockMap();
            if (clientIp != null && !mockMap.isEmpty() && mockMap.get(clientIp) != null && !mockMap.get(clientIp).isEmpty()) {
                config.putAll(mockMap.get(clientIp));
            }
            return config;
        }

        return null;
    }

    /**
     * 强制刷新所有接口下的provider缓存，并将最新缓存推送给客户端
     */
    @Override
    public void forceReloadAllProvider() throws Exception {
        List<InterfaceInfo> ifaceList = interfaceInfoManagerImpl.getAllListForProvider();
        loadInterfaceCacheLock(ifaceList);
    }

    /**
     * 强制刷新接口的provider缓存，并将最新缓存推送给客户端
     */
    @Override
    public boolean forceReloadProvider(List<Integer> list, boolean isAsyn) throws Exception {
//        if (list != null && !list.isEmpty()) {
//            if (isAsyn) {
//                ifaceAliasVersionQueue.addAll(list);
//                return true;
//            } else {
//                forceReloadProvider(list);
//                return true;
//            }
//        }
        return false;
    }

    /**
     * 刷新接口的provider缓存，并将最新缓存推送给客户端
     */
    @Override
    public boolean putIfaceAliasToQueue(List<IfaceAliasVersion> list) throws Exception {
    	if (list != null && !list.isEmpty()) {
			ifaceAliasVersionQueue.add(list);
			return true;
    	}
    	return false;
    }

    /**
     * @param list
     * @throws Exception
     */
    private void forceReloadByAliasVersion(List<IfaceAliasVersion> list) throws Exception {
    	List<InterfaceInfo> tmpList = new ArrayList<InterfaceInfo>();
    	List<InterfaceInfo> interfaceList = interfaceInfoManagerImpl.getInterfaceListFromVersionList(list);
    	String interfaceName = null;
    	for (InterfaceInfo iface : interfaceList) {
    		interfaceName = subscribeHelper.getInterfaceNameById(iface.getInterfaceId());
    		if (!StringUtils.isEmpty(interfaceName)) {
    			iface.setInterfaceName(interfaceName);
    			tmpList.add(iface);
    		}
    	}
    	loadInterfaceCache(tmpList);
    }

    /**
     * 根据interfaceId加载provider信息
     * @param list
     * @throws Exception
     */
    private void reloadByInterfaceId(List<Integer> list) throws Exception {
        loadInterfaceCache(interfaceInfoManagerImpl.getByIdListForProvider(list));
    }

    @Override
    public void forceReloadAllInterfaceConfig() throws Exception {
        List<InterfaceInfo> list = interfaceInfoManagerImpl.getChangeListByConfigUpdateTime(new Date(RegistryConstants.ORIGINAL_TIME));
        loadInterfaceConfigCacheLock(list);
    }

    @Override
    public boolean forceReloadInterfaceConfig(List<Integer> list, boolean isAsyn) throws Exception {
        if (list != null && !list.isEmpty()) {
            if (isAsyn) {
                ifaceConfigQueue.addAll(list);
                return true;
            } else {
                forceReloadInterfaceConfig(list);
                return true;
            }
        }
        return false;
    }

    private void forceReloadInterfaceConfig(List<Integer> list) throws Exception {
        //从数据取下，获取相关的信息
        List<InterfaceInfo> result = interfaceInfoManagerImpl.getByIdListForConfig(list);
        loadInterfaceConfigCache(result);
    }

    @Override
    public InterfaceInfo getByName(String ifaceName) throws Exception {
        InterfaceInfo info = null;
        boolean isInvalid = false;
        try {
            isInvalid = subscribeHelper.containInvalidInterface(ifaceName);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (!isInvalid) {  //如果不在已删除的接口列表中,就进行以下操作. 如果是已删除接口，直接返回null
            InterfaceCacheVo vo = subscribeHelper.getInterfaceVo(ifaceName);
            if (vo != null && vo.getInterfaceName() != null) {
                //从内存里取
                info = new InterfaceInfo();
                info.setInterfaceId(vo.getInterfaceId());
                info.setInterfaceName(vo.getInterfaceName());
                info.setCreateTime(new Date(vo.getCreateTime()));
                info.setConfigUpdateTime(new Date(vo.getConfigUpdateTime()));
            }
            if (info == null) {  //如果从内存里取不到，就到数据库里取
                info = interfaceInfoManagerImpl.getByName(ifaceName);
                subscribeHelper.putInterfaceVo(ifaceName, new InterfaceCacheVo(info));
            }
        }
        return info;
    }

    @Override
    public String getCallbackInfo(String interfaceName) {
        Map<String, Set<String>> map = new HashMap<String, Set<String>>();
        try {
        	InstanceVo insVo = null;
        	String value = null;
        	InterfaceCacheVo vo = subscribeHelper.getInterfaceVo(interfaceName);
            if (vo != null && vo.getInterfaceName() != null) {
                for (String insKey : vo.getInsKeySet()) {
                    insVo = subscribeHelper.getInstanceCache().get(insKey);
                    if (insVo != null && insVo.getCallback() != null) {
                        value = "insKey:" + insKey + "," + insVo.getCallback().toString();
                        if (map.get(vo.getInterfaceName()) == null) {
                            map.put(vo.getInterfaceName(), new HashSet<String>());
                        }
                        map.get(vo.getInterfaceName()).add(value);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return JSON.toJSONString(map);
    }

    @Override
    public void unRegistryConsumer(String ifaceName, String alias, int protocol, String insKey) {
        try {
            if (subscribeHelper.getInstanceCache().get(insKey) != null && subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap() != null && subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName) != null) {
                for (AliasProtocolVo vo : subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName)) {
                    if (vo.getAlias().equals(alias) && vo.getProtocol() == protocol) {
                        subscribeHelper.getInstanceCache().get(insKey).getIfaceAliasProtocolMap().get(ifaceName).remove(vo);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("error:{}, ifaceName:{}, alias:{}, protocol:{}, insKey:{}", e.getMessage(), ifaceName, alias, protocol, insKey);
        }
    }

    @Override
    public Map<String, String> getConfig(String insKey, SubscribeCallback<SubscribeUrl> callback) throws Exception {
        if (callback != null && insKey != null) {
        	subscribeHelper.putInstanceCallback(null, insKey, callback, 0);
        }
        return new HashMap<String, String>(globalConfigMapCache);
    }

    /**
     * 获取全局配置版本号
     * @param attrs
     * @return
     */
    @Override
    public long getGlobalConfigDataVersion(Map<String, String> attrs) {
        try {
            if (attrs != null && attrs.get(RegistryConstants.GLOBALSETTING_VERSION) != null) {
                return Long.parseLong(attrs.get(RegistryConstants.GLOBALSETTING_VERSION));
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return globalsettingVersion;
    }

    /**
     * 取出全局配置
     * @throws Exception
     */
    private void loadConfig() throws Exception {
        List<Integer> typeList = new ArrayList<Integer>();
        typeList.add(DataEnum.SysParamType.CLientConfig.getValue());
        typeList.add(DataEnum.SysParamType.Worker.getValue());
        //获取typelist中所有的配置信息
        List<SysParam> alllist = sysParamManagerImpl.getListByType(typeList);

        if (alllist != null && !alllist.isEmpty()) {
            Map<String, String> allClientMap = new HashMap<String, String>();
            Map<String, String> changeClientMap = new HashMap<String, String>();
            Map<String, String> changeRegistryMap = new HashMap<String, String>();

            for (SysParam param: alllist) {
                if (param == null || param.getKey() == null || param.getKey().isEmpty()) continue;
                if (param.getType() == DataEnum.SysParamType.CLientConfig.getValue()) {
                    //和globalConfigMapCache比较，将alllist中新增的数据放入changeMap中
                    if(!globalConfigMapCache.containsKey(param.getKey()) || !globalConfigMapCache.get(param.getKey()).equals(param.getValue())) {
                        changeClientMap.put(param.getKey(), param.getValue() == null ? "" : param.getValue());
                    }
                    //将alllist的所有数据放入allMap中
                    allClientMap.put(param.getKey(), param.getValue() == null ? "" : param.getValue());
                } else if (param.getType() == DataEnum.SysParamType.Worker.getValue()) {
                    //获取worker级别的配置
                    changeRegistryMap.put(param.getKey(), param.getValue() == null ? "" : param.getValue());
                }
            }
            logger.info("load all client config:{}", allClientMap);
            logger.info("load changed client config:{}", changeClientMap);
            logger.info("load changed registry config:{}", changeRegistryMap);

            //放入缓存globalConfigMapCache中
            globalConfigMapCache.clear();
            globalConfigMapCache.putAll(allClientMap);
            globalsettingVersion = getGlobalConfigDataVersion(globalConfigMapCache);

            //获取注册中心的配置信息
            subscribeHelper.getRegistryConfig(changeRegistryMap);

            // 将更新的数据推送给saf客户端
            if (!subscribeHelper.getInstanceCache().isEmpty() && !changeClientMap.isEmpty()) {
                JsfUrl jsfUrl = new JsfUrl();
                jsfUrl.setAlias(null);
                jsfUrl.setProtocol(0);
                SubscribeUrl url = new SubscribeUrl();
                url.setSourceUrl(jsfUrl);
                for (InstanceVo vo : subscribeHelper.getInstanceCache().values()) {
                    try {
                        SubscribeCallback<SubscribeUrl> callback = vo.getCallback();
                        if (callback != null) {
                            jsfUrl.setInsKey(vo.getInsKey());
                            // 只更新变化的部分（暂时未包括删除的属性）
                            subscribeHelper.notifyInstanceToUpdateConfig("", changeClientMap, url, vo.getInsKey(), SubscribeUrl.CONFIG_UPDATE);
                        }
                    } catch (Exception e) {
                        logger.error("notify instance to update global config, " + e.getMessage(), e);
                    }
                }
            }
        }
    }

    @Override
    public boolean updateProvider(String interfaceName, final Map<String, String> provider, int type) {
        logger.info("updateProvider by simple admin:ip[{}], alias[{}], type[" + type + "]", new Object[]{provider.get("ip"), provider.get("alias")});

        if (provider == null || provider.isEmpty() || subscribeHelper.getInterfaceVo(interfaceName) == null) {
            return true;
        }
        InterfaceCacheVo vo = subscribeHelper.getInterfaceVo(interfaceName);
        Server server = null;
        if (type == SubscribeUrl.PROVIDER_ADD) {
            server = subscribeHelper.convertMapToServer(provider, subscribeHelper.getInterfaceVo(provider.get("interfaceName")).getInterfaceId());
            if (vo.aliasServersMap.get(server.getAlias()) == null) {
                vo.aliasServersMap.put(server.getAlias(), new ArrayList<Server>());
            } else if (!vo.aliasServersMap.get(server.getAlias()).contains(server)) {
                vo.aliasServersMap.get(server.getAlias()).add(server);
            }
        } else if (type == SubscribeUrl.PROVIDER_DEL) {
            server = subscribeHelper.convertMapToServer(provider, subscribeHelper.getInterfaceVo(provider.get("interfaceName")).getInterfaceId());
            List<Server> have = vo.aliasServersMap.get(server.getAlias());
            if (have != null) {
                have.remove(server);
            }
        }
        // 通知client
        Map<String, List<Server>> temp = new HashMap<String, List<Server>>();
        List<Server> sers = new ArrayList<Server>();
        sers.add(server);
        temp.put(server.getAlias(), sers);
        calculateHelper.notifyClientsByAlias(interfaceName, temp, type);
        //更新缓存状态
        interfaceLoadLastTime = 1L;
        vo.serverLoadLastTime = 0L;
        return true;
    }

    /**
     * 修改注射中心中接口黑白名单
     * 只给简易管理端使用
     * @param interfaceName
     * @param bwList
     * @param type
     * @return
     */
    @Override
    public boolean updateInterfaceWBList(String interfaceName, Map<String, String> bwList, int type) {
        if (bwList.size() > 1) {
            throw new RuntimeException("updateInterfaceWBList error: only black or white list at one time!");
        }
        if (bwList.size() < 1) {
            return true;
        }
        // 1.更新缓存
        Map<String, List<String>> iconfig = wbCacheHelper.getWbCache(interfaceName);

        if (iconfig == null) {
            iconfig = new HashMap<String, List<String>>();
            wbCacheHelper.putWbCache(interfaceName, iconfig);
        }

        String bw = bwList.keySet().iterator().next();
        List<String> ips = iconfig.get(bw);

        if (ips == null) {
            ips = new ArrayList<String>();
            iconfig.put(bw, ips);
        }

        if (type == 2) {
            ips.remove(bwList.get(bw));
        } else if (type == 1) {
            ips.add(bwList.get(bw));
        }

        Map<String, Boolean> old = wbCacheHelper.getWbRecordCache(interfaceName);

        wbCacheHelper.removeWbRecordCache(interfaceName);
        String ipstring = RegistryUtil.getStringFromList(ips);
        Map<String, String> bwString = new HashMap<String, String>();
        bwString.put(bw, ipstring);

        // 2.通知client端
        Map<String, Map<String, String>> temp = new HashMap<String, Map<String, String>>();
        temp.put(interfaceName, bwString);

        Map<String, Map<String, Boolean>> temp1 = new HashMap<String, Map<String, Boolean>>();
        temp1.put(interfaceName, old);
        updateInterfaceConfigMapCache(temp, temp1, null);

        // 3. 连上DB后重新加载
        propertyLoadLastTime = 1;
        subscribeHelper.getInterfaceVo(interfaceName).setConfigUpdateTime(0);
        return false;
    }

    @Override
    public Map<String, String> getInterfaceConfig(String interfaceName) {
        if (subscribeHelper.getInterfaceVo(interfaceName) == null) {
            return new HashMap<String, String>();
        }
        return subscribeHelper.getInterfaceVo(interfaceName).propertyMap;
    }

    @Override
    public boolean checkCallback(String insKey, boolean checkNotify) {
        if (subscribeHelper.isCallbackNull(insKey) == false) {
            if (checkNotify) {
            	int i = 2;
				while (i-- > 0) {
	                SubscribeUrl url = new SubscribeUrl();
	                url.setType(SubscribeUrl.CHECK_RUOK);
	                try {
	                    String result = subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(url);
	                    logger.info("check callback result {}, insKey:{}", result, insKey);
	                    return true;
	                } catch (CallbackStubException e) {
	                    logger.warn("check callback warn, CallbackStubException: " + e.getMessage() + ", insKey:" + insKey);
	                    break;
	                } catch (ClientTimeoutException e) {
	                    logger.error("check callback error: ClientTimeoutException: " + e.getMessage() + ", insKey:" + insKey, e);
	                } catch (Throwable e) {
	                    logger.error("check callback error: " + e.getMessage() + ", insKey:" + insKey, e);
	                }
				}
            } else {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean insCtrl(String insKey, byte type) {
        if (insKey == null || insKey.isEmpty()) return false;
        String typeName = "";
        int notifyType = 0;
        if (type == 1) {  //重新注册
            notifyType = SubscribeUrl.INSTANCE_RECOVER;
            typeName = "recover";
        } else if (type == 2) {  //强制重连其它注册中心并强制重新订阅（包括provider和consumer等）
            notifyType = SubscribeUrl.INSTANCE_RECONNECT;
            typeName = "reconnect";
        } else if (type == 3) {  //重新设置时间
            notifyType = SubscribeUrl.INSTANCE_RESET_SCHEDULED;
            typeName = "resetschedule";
        }
        SubscribeUrl url = new SubscribeUrl();
        long start = 0;
        long end = 0;
        try {
            if (notifyType != 0) {
                //通知配置信息
                JsfUrl jsfUrl = new JsfUrl();
                jsfUrl.setInsKey(insKey);
                url.setSourceUrl(jsfUrl);
                url.setType(notifyType);
                if (subscribeHelper.isCallbackNull(insKey) == false) {
                    start = System.currentTimeMillis();
                    //推送给客户端后，客户端异步执行，返回值为空
                    subscribeHelper.getInstanceCache().get(insKey).getCallback().notify(url);
                    end = System.currentTimeMillis();
                    logger.info("{} inskey: {}, SubscribeUrl type: {}", typeName, insKey, notifyType);
                    CallbackRecoder.increaseCallbackCount();
                    //记录callback日志
                    callbackLogServiceImpl.saveCallbackLog("", insKey, null, CallbackLog.LOGTYPE_OTHER, url, (end - start));
                    return true;
                }
            }
        } catch (CallbackStubException e) {
            logger.warn("typeName " + typeName + " notify warn, inskey:" + insKey + ", error:" + e.getMessage());
            callbackLogServiceImpl.saveCallbackLog("", insKey, e, CallbackLog.LOGTYPE_STUB_EXCEPTION, url, 0);
            subscribeHelper.handleCallbackException(insKey);
        } catch (ClientTimeoutException e) {
            logger.error("typeName " + typeName + " notify error, inskey:" + insKey + ", error:" + e.getMessage(), e);
            callbackLogServiceImpl.saveCallbackLog("", insKey, e, CallbackLog.LOGTYPE_TIMEOUT_EXCEPTION, url, 0);
            subscribeHelper.handleCallbackException(insKey);
        } catch (Exception e) {
            CallbackRecoder.increaseCallbackFailTotalCount();
            logger.error("typeName " + typeName + " notify error, inskey:" + insKey + ", error:" + e.getMessage(), e);
            callbackLogServiceImpl.saveCallbackLog("", insKey, e, CallbackLog.LOGTYPE_EXCEPTION, url, 0);
        }
        logger.warn("{} inskey: {}, SubscribeUrl type: {}", typeName, insKey, notifyType);
        return false;
    }

}
