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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ipd.jsf.gd.error.CallbackStubException;
import com.ipd.jsf.gd.error.ClientTimeoutException;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.ConcurrentHashSet;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.berkeley.dao.BerkeleyDb;
import com.ipd.jsf.registry.callback.SubscribeCallback;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.AliasProtocolVo;
import com.ipd.jsf.registry.domain.CallbackLog;
import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.JsfSerialization;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.domain.ServerAlias;
import com.ipd.jsf.registry.manager.InterfaceManager;
import com.ipd.jsf.registry.recoder.CallbackRecoder;
import com.ipd.jsf.registry.service.CallbackLogService;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.registry.vo.InstanceVo;
import com.ipd.jsf.registry.vo.InterfaceCacheVo;
import com.ipd.jsf.service.vo.Instance;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

@Service
public class SubscribeHelper {
	private Logger logger = LoggerFactory.getLogger(SubscribeHelper.class);

    //失效接口加载计数器
    private int loadInvalidCount = 1;

	private float loadFactor = 0.75f;

    //权重放大系数，用于同机房优先放大权重策略
    private volatile int globalWeightFactor = 1;

	//缓存接口信息和provider列表, key=interfaceName
    private ConcurrentHashMap<String, InterfaceCacheVo> ifaceCache = new ConcurrentHashMap<String, InterfaceCacheVo>(128, loadFactor, 128);

    //当前注册中心的实例缓存，key=inskey
    private ConcurrentHashMap<String, InstanceVo> instanceCache = new ConcurrentHashMap<String, InstanceVo>(64, loadFactor, 64);

	//序列化map,key:jsf版本号，value:序列化配置信息
    private ConcurrentHashMap<String, JsfSerialization> serializationMap = new ConcurrentHashMap<String, JsfSerialization>(1, loadFactor, 1);

    //删除的接口, 在数据库中valid字段为0的接口
    private ConcurrentHashSet<String> invalidIfaceSet = new ConcurrentHashSet<String>();

    //注册中心配置缓存
    private ConcurrentHashMap<String, String> registryConfigMapCache = new ConcurrentHashMap<String, String>(1, loadFactor, 1);

    @Autowired
    private InterfaceManager interfaceInfoManagerImpl;

    @Autowired
    private BerkeleyDb berkeleyDb;

    @Autowired
    private CallbackLogService callbackLogServiceImpl;

    @Autowired
    private WbCacheHelper wbCacheHelper;
    
    @Autowired
    private CalculateHelper calculateHelper;

    /**
     * 根据配置下发历史记录，将server的相关配置进行替换
     * @param serversMap
     * @param serverConfigMap
     */
    public void replaceServerConfig(Map<Integer, Server> serversMap, Map<Integer, Map<String, String>> serverConfigMap) {
        if (serversMap != null && !serversMap.isEmpty() && serverConfigMap != null && !serverConfigMap.isEmpty()) {
            for (Server server : serversMap.values()) {
                try {
                    if (serverConfigMap.get(server.getId()) != null && serverConfigMap.get(server.getId()).get("weight") != null) {
                        server.setWeight(Integer.parseInt(serverConfigMap.get(server.getId()).get("weight")));
                    }
                } catch (Exception e) {
                    logger.warn("parse weight is error, serverId:{}, {}", server.getId(), e.getMessage());
                }
            }
        }
    }

    /**
     * 1.从aliasServers中删除强制下线或者不存在的serverId
     * 2.将从server表中查询到serverId 合并到aliasServer中
     * 3.将替换aliasServerReplaceMap中的serverId, 合并到aliasServers中。先删除掉serverId的原有组，然后再添加到替换组中
     * @param aliasServerIdsMap
     * @param aliasServerReplaceMap
     * @param serversMap
     */
    public static  void mergeAliasServer(Map<String, List<Integer>> aliasServerIdsMap, Map<String, List<Integer>> aliasServerReplaceMap, Map<Integer, Server> serversMap, Set<String> loadAliasSet) {
        if (serversMap != null && !serversMap.isEmpty()) {
//            Set<Integer> serverIdSet = new HashSet<Integer>();
//            for (Server server : serversMap.values()) {
//                serverIdSet.add(server.getId());
//            }
            // 1.从aliasServer中去除强制下线或者不存在的serverId。
            // 也就是遍历aliasServer，如果serverId不在serverIdSet集合中，就从aliasServer删除掉serverId
            for (List<Integer> serverIds : aliasServerIdsMap.values()) {
                if (serverIds != null) {
                    Iterator<Integer> iterator = serverIds.iterator();
                    while (iterator.hasNext()) {
                        Integer id = iterator.next();
                        if (!serversMap.containsKey(id)) {
                            iterator.remove();
                        }
                    }
                }
            }

            // 2.将从server表中查询到serverId 合并到aliasServer中
            List<Integer> tempServerIds;
            for (Server server : serversMap.values()) {
                tempServerIds = aliasServerIdsMap.get(server.getAlias());
                if (tempServerIds == null) {
                    tempServerIds = new ArrayList<Integer>();
                    aliasServerIdsMap.put(server.getAlias(), tempServerIds);
                }
                if (!tempServerIds.contains(server.getId())) {
                    tempServerIds.add(server.getId());
                }
            }

            // 3.将替换aliasServerReplaceMap中的serverId, 合并到aliasServers中。步骤是：(1)在serverId的原有组中，删除掉serverId，(2)然后将serverId添加到替换组中
            if (aliasServerReplaceMap != null && !aliasServerReplaceMap.isEmpty()) {
                String srcAlias = null;
                for (String roadmapAlias : aliasServerReplaceMap.keySet()) {
                    for (Integer serverId : aliasServerReplaceMap.get(roadmapAlias)) {
                        //(1)先从aliasServers中删掉provider原有srcAlias的serverId
                        if (serversMap.get(serverId) != null) {
                            //找到provider的原有srcAlias, 从aliasServers里删掉对应srcAlias的serverId
                            srcAlias = serversMap.get(serverId).getAlias();
                            aliasServerIdsMap.get(srcAlias).remove(serverId);
                        }
                        //(2)将新的替换alias添加进去
                        if (aliasServerIdsMap.get(roadmapAlias) == null) {
                            aliasServerIdsMap.put(roadmapAlias, new ArrayList<Integer>());
                        }
                        aliasServerIdsMap.get(roadmapAlias).add(serverId);
                    }
                }
            }
        }
        
        //4. 如果加载的loadAliasSet的alias不在aliasServerIdsMap中, 就合并到 aliasServerIdsMap中
        if (!CollectionUtils.isEmpty(loadAliasSet)) {
        	for (String alias : loadAliasSet) {
        		if (aliasServerIdsMap.get(alias) == null) {
        			aliasServerIdsMap.put(alias, new ArrayList<Integer>());
        		}
        	}
        }
    }

    /**
     * 从iface列表中获取ifaceAlias列表
     * @param list
     * @return
     */
    public List<IfaceAliasVersion> getIfaceAliasList(List<InterfaceInfo> list) {
    	List<IfaceAliasVersion> ifaceAliasList = new ArrayList<IfaceAliasVersion>();
    	if (!CollectionUtils.isEmpty(list)) {
    		for (InterfaceInfo iface : list) {
    			if (!CollectionUtils.isEmpty(iface.getVersionList())) {
    				ifaceAliasList.addAll(iface.getVersionList());
    			}
    		}
    	}
    	return ifaceAliasList;
    }

    /**
     * 转为map，key：interfaceId，value：alias
     * @param list
     * @return
     */
    public Map<Integer, Set<String>> getIfaceAliasMap(List<IfaceAliasVersion> list) {
    	Map<Integer, Set<String>> result = new HashMap<Integer, Set<String>>();
    	if (!CollectionUtils.isEmpty(list)) {
    		for (IfaceAliasVersion iface : list) {
    			if (result.get(iface.getInterfaceId()) == null) {
    				result.put(iface.getInterfaceId(), new HashSet<String>());
    			}
    			result.get(iface.getInterfaceId()).add(iface.getAlias());
    		}
    	}
    	return result;
    }
    
    /**
     * 将动态分组的serverAliasList合并到ifaceAliasVersionList中, 也就是将接口中动态分组alias合并进来
     * @param ifaceAliasVersionList
     * @param serverAliasList
     * @return
     */
    public List<IfaceAliasVersion> mergeIfaceAliasList(List<IfaceAliasVersion> ifaceAliasVersionList, List<ServerAlias> serverAliasList) {
    	Set<IfaceAliasVersion> tmpList = new HashSet<>();
        boolean findSrc;
        boolean findTarget;
    	for (ServerAlias serverAlias : serverAliasList) {
    		findSrc = false;
    		findTarget = false;
    		for (IfaceAliasVersion tmpVersion : ifaceAliasVersionList) {
    			if (serverAlias.getInterfaceId() == tmpVersion.getInterfaceId()) {
    				if (serverAlias.getSrcAlias().equals(tmpVersion.getAlias())) {
    					findSrc = true;
    				}
    				if (serverAlias.getTargetAlias().equals(tmpVersion.getAlias())) {
    					findTarget = true;
    				}
    			}
    		}
    		if (!findSrc) {
    			tmpList.add(makeIfaceAliasVersion(serverAlias.getInterfaceId(), serverAlias.getSrcAlias()));
    		}
    		if (!findTarget) {
    			tmpList.add(makeIfaceAliasVersion(serverAlias.getInterfaceId(), serverAlias.getTargetAlias()));
    		}
    	}
    	ifaceAliasVersionList.addAll(tmpList);
    	return ifaceAliasVersionList;
    }

    private IfaceAliasVersion makeIfaceAliasVersion(int interfaceId, String alias) {
        IfaceAliasVersion version = new IfaceAliasVersion();
        version.setAlias(alias);
        version.setInterfaceId(interfaceId);
        return version;
    }

    /**
     * 将serverId转为server
     * @param serverIdMap
     * @param serverMap
     * @return
     */
    public Map<String, List<Server>> convertServerIdToServer(Map<String, List<Integer>> serverIdMap, Map<Integer, Server> serverMap) {
        //key : alias, value : server list
        Map<String, List<Server>> result = new HashMap<String, List<Server>>();
        if (!CollectionUtils.isEmpty(serverIdMap)) {
            List<Integer> serverIds = null;
            String alias = null;
            Server server = null;
            for (Map.Entry<String, List<Integer>> serverIdMapEntry : serverIdMap.entrySet()) {
                alias = serverIdMapEntry.getKey();
                serverIds = serverIdMapEntry.getValue();
                if (result.get(alias) == null) {
                    result.put(alias, new ArrayList<Server>());
                }
                if (!CollectionUtils.isEmpty(serverMap)) {
                    for (Integer serverId : serverIds) {
                        server = serverMap.get(serverId);
                        if (server != null) {
                            result.get(serverIdMapEntry.getKey()).add(server);
                        }
                    }
                }
            }
        }
        return result;
    }

    /**
     * 读取同机房优先策略
     * @param ifacePropertyMap
     * @return
     */
    public byte getRoomStrategy(Map<String, String> ifacePropertyMap) {
        Byte result = 0;
        if (ifacePropertyMap != null && ifacePropertyMap.get(RegistryConstants.ROOM_STRATEGY) != null) {
        	try {
        		result = Byte.valueOf(ifacePropertyMap.get(RegistryConstants.ROOM_STRATEGY));
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
        }
        return result.byteValue();
    }

    /**
     * 读取provider限制数量
     * @param ifacePropertyMap
     * @return
     */
    public int getProviderLimit(Map<String, String> ifacePropertyMap) {
        int result = -1;  //初始值与InterfaceVo一致
        if (ifacePropertyMap != null && ifacePropertyMap.get(RegistryConstants.PROVIDER_LIMIT) != null) {
        	try {
        		result = Integer.valueOf(ifacePropertyMap.get(RegistryConstants.PROVIDER_LIMIT)).intValue();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
        }
        return result;
    }

	/**
	 * @param type
	 * @return
	 */
    public String getNofityLog(int type) {
		switch (type) {
		    case SubscribeUrl.PROVIDER_ADD:
		        return "add";
		    case SubscribeUrl.PROVIDER_DEL:
		    	return "del";
		    case SubscribeUrl.PROVIDER_UPDATE_ALL:
		    	return "update_all";
		    case SubscribeUrl.PROVIDER_CLEAR:
		    	return "clear";
		    default:
		    	return "";
		}
	}

    /**
     * 检查clientIp是否符合Ip路由
     * @param ifaceName
     * @param clientIp
     * @return  true : 符合，false：不符合
     */
    public boolean checkClientIpRouter(String ifaceName, String clientIp, Map<String, List<String>> ipRouterMap) {
        if (CommonUtils.isNotEmpty(ipRouterMap)) {
            for (Map.Entry<String, List<String>> entry : ipRouterMap.entrySet()) {
                String clientIpExp = entry.getKey();
                if (clientIp == null || clientIp.isEmpty()) {
                    logger.warn("{} client ip is null ", ifaceName);
                    return false;
                }
                if ("*".equals(clientIpExp) || RegistryUtil.match(clientIpExp, clientIp)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Server convertMapToServer(Map<String, String> map, int interfaceId) {
        // 需要生成负的ID
        Server ret = new Server();
        ret.setId(RegistryUtil.getRanNegativeInt());
        ret.setIp(map.get("ip"));
        ret.setPort(Integer.parseInt(map.get("port")));
        ret.setAlias(map.get("alias"));
        ret.setAppPath(map.get("appPath"));
        ret.setInterfaceName(map.get("interfaceName"));
        ret.setProtocol(Integer.parseInt(map.get("protocol")));
        ret.setSafVer(Integer.parseInt(map.get("safVersion")));
        ret.setInterfaceId(interfaceId);
        ret.setUniqKey(UniqkeyUtil.getServerUniqueKey(ret.getIp(), ret.getPort(), ret.getAlias(), ret.getProtocol(), ret.getInterfaceId()));
        return ret;
    }

    /**
     * 加载删除的接口
     * @throws Exception 
     */
    public void loadInvalidInterface() throws Exception {
        int mod = 3;
        if (loadInvalidCount++ % mod == 0) {
            loadInvalidCount = 1;
            Set<String> temp = interfaceInfoManagerImpl.getInvalidList();
            invalidIfaceSet.clear();
            if (!temp.isEmpty()) {
                invalidIfaceSet.addAll(temp);
            }
            logger.info("load invalid interface:{}, count:{}", invalidIfaceSet, loadInvalidCount);
        }
    }

    public boolean containInvalidInterface(String interfaceName) {
    	return invalidIfaceSet.contains(interfaceName);
    }

    public int checkServer(String interfaceName, Server server) {
        try {
            List<Server> servers = ifaceCache.get(interfaceName).aliasServersMap.get(server.getAlias());
            if (servers != null && !servers.isEmpty()) {
                for (Server cacheServer : servers) {
                    //判断port,protocol, alias和ip，如果相等，就认为存在
                    if (cacheServer.getPort() == server.getPort() && cacheServer.getProtocol() == server.getProtocol() && cacheServer.getIp().equals(server.getIp())) {
                        if (cacheServer.getWeight() == server.getWeight()) { //如果权重没变，不需要更新接口版本号
                            return RegistryConstants.SERVER_EXIST;
                        } else { //否则，更新接口版本号
                            return RegistryConstants.SERVER_UPDATEVERSION;
                        }
                    }
                }
            }
        } catch(Exception e) {   //如果有异常就返回false
            logger.warn("check server is error:{}, {}", e.getMessage(), server.toString());
        }
        //返回0，表示没有找到server
        return RegistryConstants.SERVER_NOT_EXIST;
    }

    public long getInterfaceConfigDataVersion(String interfaceName) {
        return ifaceCache.get(interfaceName) == null ? 0 : ifaceCache.get(interfaceName).getConfigUpdateTime() == 0 ? 0 : ifaceCache.get(interfaceName).getConfigUpdateTime();
    }

    public long getInterfaceDataVersion(String interfaceName, String alias) {
    	if (ifaceCache.get(interfaceName) == null || ifaceCache.get(interfaceName).versionMap == null
    			 || ifaceCache.get(interfaceName).versionMap.get(alias) == null) {
    		return 0;
    	}
    	return ifaceCache.get(interfaceName).versionMap.get(alias).longValue();
    }

    public int getSubscribeType(int type) {
    	switch (type) {
    	case 1:  // 获取consumer配置
            return SubscribeUrl.CONSUMER_CONFIG;
    	case 2:  // 获取consumer的服务列表
            return SubscribeUrl.CONSUMER_PROVIDERLIST;
    	case 3:  // 获取provider配置
            return SubscribeUrl.PROVIDER_CONFIG;
    	case 11:  // 获取实例级别的数据
            return SubscribeUrl.INSTANCE_CONFIG;
    	case 12:  //获取provider线程并发信息
            return SubscribeUrl.PROVIDER_CONCURRENT;
    	case 13: //获取consumer线程并发信息
            return SubscribeUrl.CONSUMER_CONCURRENT;
    	case 14: //查询实例的线程池+future并发信息
            return SubscribeUrl.INSTANCE_CONCURRENT;
        default:
            return -1;
        }
    }

    /**
     * 获取注册中心的配置
     * @param changeRegistryMap
     */
	public void getRegistryConfig(Map<String, String> changeRegistryMap) {
		//放入缓存registryConfigMapCache中
		try {
		    if (changeRegistryMap != null && !changeRegistryMap.isEmpty()) {
		    	try {
		    		//(1)读取provider的berkeley开关
		    		String providerNewValue = changeRegistryMap.get(RegistryConstants.ISOPEN_BERKELEYDB_PROVIDER);
		    		if (providerNewValue != null) {
		    			RegistryUtil.isOpenProviderBerkeleyDB = Boolean.valueOf(providerNewValue).booleanValue();
		    		}
		    		//(2)读取整个berkeley开关
			        String wholeNewValue = changeRegistryMap.get(RegistryConstants.ISOPEN_BERKELEYDB);
			        if (wholeNewValue != null) {
			        	RegistryUtil.isOpenWholeBerkeleyDB = Boolean.valueOf(wholeNewValue).booleanValue();
			            if (RegistryUtil.isOpenWholeBerkeleyDB == false) {
			                //如果整体是关闭，provider也关闭
			            	RegistryUtil.isOpenProviderBerkeleyDB = RegistryUtil.isOpenWholeBerkeleyDB;
			                if (berkeleyDb.isValue()) {
			                    berkeleyDb.flush();
			                    berkeleyDb.close();
			                }
			            }
			        }
			        logger.info("provider isOpenBerkeleyDB : {}, whole isOpenBerkeleyDB : {}", providerNewValue, wholeNewValue);
		        } catch (Exception e) {
            		logger.error(e.getMessage(), e);
            	}
		        //(3)读取序列化配置信息
		    	try {
		    		String serialization = changeRegistryMap.get(RegistryConstants.JSF_SERIALIZATION);
		    		Map<String, JsfSerialization> tempMap = JSON.parseObject(serialization, new TypeReference<Map<String, JsfSerialization>>(){});
		    		serializationMap.clear();
		    		if (tempMap != null) {
		    			serializationMap.putAll(tempMap);
		    		}
		    		logger.info("serialization : {}", serialization);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
		        //(4)获取机房权重系数
		        try {
			        String value = changeRegistryMap.get(RegistryConstants.ROOM_WEIGHT_FACTOR);
			        if (value != null) {
			        	globalWeightFactor = Integer.parseInt(value);
			            logger.info("load weight factor : {}", globalWeightFactor);
			        }
		        } catch (Exception e) {
		        	logger.error("get weight factor is error, " + e.getMessage(), e);
		        }
		        //(5)获取版本限制，用于在测试环境的JSF客户端版本检查
		        try {
		        	if (changeRegistryMap.get(RegistryConstants.JSF_CONFIG_VERSION_INT) != null
		        			&& changeRegistryMap.get(RegistryConstants.JSF_CONFIG_VERSION_STRING) != null) {
		        		RegistryUtil.jsfVersionInt = Integer.parseInt(changeRegistryMap.get(RegistryConstants.JSF_CONFIG_VERSION_INT));
		        		RegistryUtil.jsfVersionString = changeRegistryMap.get(RegistryConstants.JSF_CONFIG_VERSION_STRING);
		        	}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
		        //(6)获取保存callback正常日志的开关
		        try {
		        	if (changeRegistryMap.get(RegistryConstants.CALLBACKLOG_SWITCH) != null) {
		        		callbackLogServiceImpl.setOpenCallbackNormalLog(Boolean.valueOf(changeRegistryMap.get(RegistryConstants.CALLBACKLOG_SWITCH)));
		        		logger.info("load isOpenCallbackNormalLog : {}", callbackLogServiceImpl.isOpenCallbackNormalLog());
		        	}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
		    }
		    //将注册中心需要的参数放入进来
		    registryConfigMapCache.clear();
		    registryConfigMapCache.putAll(changeRegistryMap);
		} catch (Exception e) {
		    logger.info(e.getMessage(), e);
		}
	}

    public int getWeightFactor() {
        String value = registryConfigMapCache.get(RegistryConstants.ROOM_WEIGHT_FACTOR);
        if (value != null) {
            try {
                globalWeightFactor = Integer.parseInt(value);
            } catch (Exception e) {
                logger.error("get weight factor is error, " + e.getMessage(), e);
            }
        }
        return globalWeightFactor;
    }

    /**
     * 获取生成clientId开关
     * true-打开生成clientId功能； false-关闭，也就是使用mysql的id自增
     * 默认是打开
     * @return
     */
    public boolean genClientIdSwitch() {
        boolean value = true;  //默认是打开
        try {
            String tmp = registryConfigMapCache.get(RegistryConstants.GEN_CLIENTID_SWITCH_KEY);
            if (tmp != null) {
                value = Boolean.parseBoolean(tmp);
            }
        } catch (Exception e) {
            logger.error("get " + RegistryConstants.GEN_CLIENTID_SWITCH_KEY + " is error, " + e.getMessage(), e);
        }
        return value;
    }

    /**
     * 将差异server数据推送给实例
     * @param ifaceName
     * @param type
     * @param alias
     * @param serverList
     * @param insKey
     */
    public void notifyInstanceToUpdateProviders(String ifaceName, int type, String alias, List<Server> serverList, String insKey) {
        if (isCallbackNull(insKey) || instanceCache.get(insKey) == null 
        		|| instanceCache.get(insKey).getIfaceAliasProtocolMap().get(ifaceName) == null 
        		|| instanceCache.get(insKey).getIfaceAliasProtocolMap().get(ifaceName).isEmpty()) {
            return;
        }
        try {
            long dataVersion = getInterfaceDataVersion(ifaceName, alias);
            String logStr = getNofityLog(type);
            SubscribeUrl url = new SubscribeUrl();
            JsfUrl jsfUrl = new JsfUrl();
            jsfUrl.setIface(ifaceName);
            jsfUrl.setAlias(alias);
            jsfUrl.setDataVersion(dataVersion);
            jsfUrl.setInsKey(insKey);
            long start = 0;
            long end = 0;
            int i = 2;
            String clientIp = instanceCache.get(insKey).getCallback().getClientIp();
            List<JsfUrl> providerList = null;
            //遍历实例下的alias:protocol
            for (AliasProtocolVo aliasProtocolVo : instanceCache.get(insKey).getIfaceAliasProtocolMap().get(ifaceName)) {
            	url.setType(type);
            	logStr = getNofityLog(url.getType());
            	if (alias.equals(aliasProtocolVo.getAlias())) {   //比较alias，找到相同的组回调通知
                    providerList = calculateHelper.filterServers(ifaceName, serverList, clientIp, aliasProtocolVo.getProtocol(), aliasProtocolVo.getJsfVersion(), aliasProtocolVo.getSerialization(), type);
                    // 如果provider列表不为空，或者类型为清空provider列表时
                    if (!providerList.isEmpty() || type == SubscribeUrl.PROVIDER_UPDATE_ALL || type == SubscribeUrl.PROVIDER_CLEAR) {
                        jsfUrl.setProtocol(aliasProtocolVo.getProtocol());
                        url.setProviderList(providerList);
                        url.setSourceUrl(jsfUrl);
                        i = 2;
                        while (i-- > 0) {
                            try {
                                //类型为新增provider，或者更新所有provider，或者清空provider列表时，才检查黑白名单. 删除状态，不用检查
                                if (type == SubscribeUrl.PROVIDER_ADD || type == SubscribeUrl.PROVIDER_UPDATE_ALL || type == SubscribeUrl.PROVIDER_CLEAR ) {
                                    //检查黑白名单
                                    if (!wbCacheHelper.checkCanVisit(ifaceName, clientIp)) {
                                        url.setType(SubscribeUrl.PROVIDER_CLEAR);
                                        url.setProviderList(null);
                                        logStr = "此ip在黑白名单中，已经被限制了 " + getNofityLog(url.getType());
                                    }
                                }
                                if (isCallbackNull(insKey) == false) {
                                    start = System.currentTimeMillis();
                                    instanceCache.get(insKey).getCallback().notify(url);
                                    end = System.currentTimeMillis();
                                    logger.info(logStr + " provider notify by alias:{}, insKey:{}, result: {}", alias, insKey, url.toString());
                                    CallbackRecoder.increaseCallbackCount();
                                    //保存callback记录
                                    callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, null, CallbackLog.LOGTYPE_PROVIDERLIST, url, (end - start));
                                }
                                return;
                            } catch (CallbackStubException e) {
                                logger.warn(logStr + " provider notify warn, interface:" + ifaceName + ", inskey:" + insKey + ", " + url.toString(), e);
                                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_STUB_EXCEPTION, url, 0);
                                handleCallbackException(insKey);
                                return;
                            } catch (ClientTimeoutException e) {
                                logger.error(logStr + " provider notify error, interface:" + ifaceName + ", inskey:" + insKey + ", " + url.toString(), e);
                                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_TIMEOUT_EXCEPTION, url, 0);
                                if (i == 0) {
                                    handleCallbackException(insKey);
                                    return;
                                }
                            } catch (Exception e) {
                                CallbackRecoder.increaseCallbackFailTotalCount();
                                logger.error(logStr + " provider notify error, interface:" + ifaceName + ", inskey:" + insKey + ", subscribeurl:" + url.toString(), e);
                                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_EXCEPTION, url, 0);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 先把callback置空，有可能是网络原因，待网络恢复正常时，callback会重置。如果实例重启，过deleteInstanceInterval后，将从instanceCache中删除该实例
     * @param insKey
     */
    public void handleCallbackException(String insKey) {
        try {
            if (instanceCache.get(insKey) != null) {
                instanceCache.get(insKey).setCallback(null);
            }
        } catch (Exception e) {
            //不做处理，防止instanceCache.get(insKey)抛空指针异常
        }
        CallbackRecoder.increaseCallbackFailTotalCount();
    }

    /**
     * 检查callback是否为null，如果为null，就删除
     * @param insKey
     * @return
     */
    public boolean isCallbackNull(String insKey) {
        if (instanceCache.get(insKey) == null) {
            return true;
        }
        if (instanceCache.get(insKey).getCallback() == null) {
            removeInstanceCache(insKey);
            return true;
        }
        return false;
    }

    public void removeInstanceCache(String insKey) {
        if (instanceCache.get(insKey) != null) {
            try {
                InstanceVo vo = instanceCache.remove(insKey);
                logger.info("-------------========= remove insKey:{}", insKey);
                if (vo != null && vo.getIfaceAliasProtocolMap() != null && !vo.getIfaceAliasProtocolMap().isEmpty()) {
                    for (String ifaceName : vo.getIfaceAliasProtocolMap().keySet()) {
                        if (ifaceCache.get(ifaceName) != null && ifaceCache.get(ifaceName).getInsKeySet() != null) {
                        	ifaceCache.get(ifaceName).getInsKeySet().remove(insKey);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @param ifaceName
     * @param config
     * @param url
     * @param insKey
     * @return
     */
    public void notifyInstanceToUpdateConfig(String ifaceName, Map<String, String> config, SubscribeUrl url, String insKey, int type) {
        long start = 0;
        long end = 0;
        int i = 2;
        while (i-- > 0) {
            try {
                //通知配置信息
                url.setType(type);
                url.setConfig(config);
                if (isCallbackNull(insKey) == false) {
                    start = System.currentTimeMillis();
                    instanceCache.get(insKey).getCallback().notify(url);
                    end = System.currentTimeMillis();
                    logger.info("'{}' interface config notify by inskey: {}, type:{}, SubscribeUrl: {}", ifaceName, insKey, type, url.toString());
                    CallbackRecoder.increaseCallbackCount();
                    //记录callback日志
                    callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, null, CallbackLog.LOGTYPE_CONFIG, url, (end - start));
                }
                break;
            } catch (CallbackStubException e) {
                logger.warn("interface config notify warn, interface:" + ifaceName + ", inskey:" + insKey + ", error:" + e.getMessage());
                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_STUB_EXCEPTION, url, 0);
                handleCallbackException(insKey);
                break;
            } catch (ClientTimeoutException e) {
                logger.error("interface config notify error, interface:" + ifaceName + ", inskey:" + insKey + ", error:" + e.getMessage(), e);
                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_TIMEOUT_EXCEPTION, url, 0);
                if (i == 0) {
                	handleCallbackException(insKey);
                    break;
                }
            } catch (Exception e) {
                CallbackRecoder.increaseCallbackFailTotalCount();
                logger.error("interface config notify error, interface:" + ifaceName + ", inskey:" + insKey + ", error:" + e.getMessage(), e);
                callbackLogServiceImpl.saveCallbackLog(ifaceName, insKey, e, CallbackLog.LOGTYPE_EXCEPTION, url, 0);
            }
        }
    }

    public ConcurrentHashMap<String, InstanceVo> getInstanceCache() {
    	return instanceCache;
    }

    /**
     * 获取接口下所有server列表,用于简易管理端用
     * @param interfaceName
     * @return
     */
    public List<Server> getServers(String interfaceName) {
        List<Server> result = new ArrayList<Server>();
        if (ifaceCache.get(interfaceName) != null) {
        	for (List<Server> servers : ifaceCache.get(interfaceName).aliasServersMap.values()) {
        		result.addAll(servers);
        	}
        }
        return result;
    }

    /**
     * 获取alias 与server对应关系,用于简易管理端用
     * @param interfaceName
     * @return
     */
    public Map<String, List<Server>> getAliasServers(String interfaceName) {
        if (ifaceCache.get(interfaceName) != null) {
            return Collections.unmodifiableMap(ifaceCache.get(interfaceName).aliasServersMap);
        }
        return null;
    }

    /**
     * 获取alias 与server对应关系,用于简易管理端用
     * @param interfaceName
     * @return
     */
    public List<Server> getAliasServers(String interfaceName, String alias) {
        if (ifaceCache.get(interfaceName) != null && ifaceCache.get(interfaceName).aliasServersMap != null) {
            List<Server> aliasServers = ifaceCache.get(interfaceName).aliasServersMap.get(alias);
            if (aliasServers == null) {
                aliasServers = new ArrayList<Server>(0);
            }
            // notice : NullExpression
            return Collections.unmodifiableList(aliasServers);
        }
        return null;
    }

    /**
     * 获取alias 与server对应关系,用于简易管理端用
     * @param interfaceName
     * @return
     */
    public Long getAliasVersion(String interfaceName, String alias) {
        if (ifaceCache.get(interfaceName) != null && ifaceCache.get(interfaceName).versionMap != null) {
            return ifaceCache.get(interfaceName).versionMap.get(alias);
        }
        return null;
    }

    /**
     * 获取实例信息, 如果insKeyList 有值，就取相应的实例信息；如果没有值，将所有实例信息返回
     * 用于简易管理端用
     * @param insKey
     * @return
     */
    public List<Instance> getInskeyList(String insKey, boolean isExactSearch) {
        List<Instance> result = new ArrayList<Instance>();
        if (insKey != null && !insKey.isEmpty()) {
            InstanceVo vo;
            if (isExactSearch) {  //如果是精确匹配
                vo = instanceCache.get(insKey);
                if (vo != null) {
                    putInstanceList(result, vo);
                }
            } else {
                for (String key : instanceCache.keySet()) {  //如果是模糊匹配
                    if (key.contains(insKey)) {
                        vo = instanceCache.get(key);
                        putInstanceList(result, vo);
                    }
                }
            }
        } else {
            for (InstanceVo vo : instanceCache.values()) {
                putInstanceList(result, vo);
            }
        }
        return result;
    }

	/**
	 * @param result
	 * @param vo
	 */
	private void putInstanceList(List<Instance> result, InstanceVo vo) {
		Instance ins = new Instance();
		ins.setInsKey(vo.getInsKey());
		ins.getIfaceAliasProtocolMap().putAll(vo.getIfaceAliasProtocolStringMap());
		result.add(ins);
	}

    /**
     * @param ifaceName
     * @param callback
     */
    public void putInstanceCallback(String ifaceName, String insKey, SubscribeCallback<SubscribeUrl> callback, int appId) {
        if (instanceCache.get(insKey) == null) {
            InstanceVo vo = new InstanceVo();
            vo.setInsKey(insKey);
            vo.setAppId(appId);
            instanceCache.put(insKey, vo);
        } else if (instanceCache.get(insKey).getAppId() == 0 && appId != 0) {
        	instanceCache.get(insKey).setAppId(appId);
        }
        //保存callback
        instanceCache.get(insKey).setCallback(callback);
        //和接口相关的callback，需要记录
        if (ifaceName != null && instanceCache.get(insKey).getIfaceAliasProtocolMap().get(ifaceName) == null) {
        	instanceCache.get(insKey).getIfaceAliasProtocolMap().put(ifaceName, new HashSet<AliasProtocolVo>());
        }
    }

    public InterfaceCacheVo getInterfaceVo(String interfaceName) {
    	return ifaceCache.get(interfaceName);
    }

    public void putInterfaceVo(String interfaceName, InterfaceCacheVo vo) {
    	ifaceCache.put(interfaceName, vo);
    }

    public boolean isIfaceCacheEmpty() {
    	return ifaceCache.isEmpty();
    }

    /**
     * 根据id获取接口名
     * @param interfaceId
     * @return
     */
    public String getInterfaceNameById(int interfaceId) {
    	for (Map.Entry<String, InterfaceCacheVo> entry : ifaceCache.entrySet()) {
			if (interfaceId == entry.getValue().getInterfaceId()) {
				return entry.getKey();
			}
		}
    	return null;
    }

    public JsfSerialization getSerialization(String serverJsfVersion) {
    	return serializationMap.get(serverJsfVersion);
    }

	/**
	 * @return the globalWeightFactor
	 */
	public int getGlobalWeightFactor() {
		return globalWeightFactor;
	}

    public static void main(String[] args) {
        Map<String, List<Integer>> newAliasServerIds = new HashMap<>();
        Map<String, List<Integer>> aliasServerIdReplaceMap = new HashMap<>();
        Map<Integer, Server> newServersMap = new HashMap<>();
        Set<String> loadAliasSet = new HashSet<>();

        newAliasServerIds.put("track-LF", new ArrayList<>());
        newAliasServerIds.get("track-LF").add(10349581);
        newAliasServerIds.get("track-LF").add(10351488);
        newAliasServerIds.get("track-LF").add(10416648);
        newAliasServerIds.get("track-LF").add(10416647);
        newAliasServerIds.get("track-LF").add(10416646);
        newAliasServerIds.get("track-LF").add(10416645);

//        newAliasServerIds.get("track-LF").add(10444536);
//        newAliasServerIds.get("track-LF").add(10486566);
//        newAliasServerIds.get("track-LF").add(10486567);
//        newAliasServerIds.get("track-LF").add(10486568);
//        newAliasServerIds.get("track-LF").add(10486569);
//        newAliasServerIds.get("track-LF").add(8000857);

        aliasServerIdReplaceMap.put("track-YFB", new ArrayList<>());
        aliasServerIdReplaceMap.get("track-YFB").add(10444536);
        aliasServerIdReplaceMap.get("track-YFB").add(10486566);
        aliasServerIdReplaceMap.get("track-YFB").add(10486567);
        aliasServerIdReplaceMap.get("track-YFB").add(10486568);
        aliasServerIdReplaceMap.get("track-YFB").add(10486569);
        aliasServerIdReplaceMap.get("track-YFB").add(8000857);

        Server server = new Server();
        server.setId(8000857);
        server.setAlias("track-LF");
        newServersMap.put(8000857, server);

        server = new Server();
        server.setId(10349581);
        server.setAlias("track-MJQ");
        newServersMap.put(10349581, server);

        server = new Server();
        server.setId(10351488);
        server.setAlias("track-MJQ");
        newServersMap.put(10351488, server);

        server = new Server();
        server.setId(10416645);
        server.setAlias("track-MJQ");
        newServersMap.put(10416645, server);

        server = new Server();
        server.setId(10416646);
        server.setAlias("track-MJQ");
        newServersMap.put(10416646, server);

        server = new Server();
        server.setId(10416647);
        server.setAlias("track-MJQ");
        newServersMap.put(10416647, server);

        server = new Server();
        server.setId(10416648);
        server.setAlias("track-MJQ");
        newServersMap.put(10416648, server);

        server = new Server();
        server.setId(10444536);
        server.setAlias("track-LF");
        newServersMap.put(10444536, server);

        server = new Server();
        server.setId(10486566);
        server.setAlias("track-LF");
        newServersMap.put(10486566, server);

        server = new Server();
        server.setId(10486567);
        server.setAlias("track-LF");
        newServersMap.put(10486567, server);

        server = new Server();
        server.setId(10486568);
        server.setAlias("track-LF");
        newServersMap.put(10486568, server);

        server = new Server();
        server.setId(10486569);
        server.setAlias("track-LF");
        newServersMap.put(10486569, server);

        server = new Server();
        server.setId(11710496);
        server.setAlias("track-YFB");
        newServersMap.put(11710496, server);

        mergeAliasServer(newAliasServerIds, aliasServerIdReplaceMap, newServersMap, loadAliasSet);
        System.out.println("newAliasServerIds:" + newAliasServerIds);
        System.out.println("aliasServerIdReplaceMap:"+aliasServerIdReplaceMap);
        System.out.println("newServersMap:"+newServersMap);
        System.out.println("loadAliasSet:"+loadAliasSet);
    }
}
