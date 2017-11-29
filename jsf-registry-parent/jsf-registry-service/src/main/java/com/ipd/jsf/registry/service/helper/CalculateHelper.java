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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.JsfSerialization;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.service.ConfigService;
import com.ipd.jsf.registry.util.MapListServerDiff;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

/**
 * @author baoningtian
 *
 */
@Service
public class CalculateHelper {
	private Logger logger = LoggerFactory.getLogger(CalculateHelper.class);

	private final int UPDATEALL_COUNT = 5;

	private final int PROVIDERLIMIT = 2;

	@Autowired
	private SubscribeHelper subscribeHelper;

    @Autowired
    private ConfigService configServiceImpl;

    /**
     * 比较数据差异，将差异server数据推送给jsf客户端
     * @param ifaceName
     * @param newAliasServersMap
     * @param oldAliasServersMap
     * @param newIpRouterMap
     * @param oldIpRouterMap
     * @param newRoomStrategy
     * @param oldRoomStrategy
     * @param newProviderLimit
     * @param oldProviderLimit
     */
    public void compareAndNotifyClients(String ifaceName, Map<String, List<Server>> newAliasServersMap, Map<String, List<Server>> oldAliasServersMap,
										Map<String, List<String>> newIpRouterMap, Map<String, List<String>> oldIpRouterMap,
										byte newRoomStrategy, byte oldRoomStrategy, int newProviderLimit, int oldProviderLimit) {
        if (subscribeHelper.getInterfaceVo(ifaceName) == null) {
            logger.warn("ifaceName:{} is not exists, please check db", ifaceName);
            return;
        }

        // 增量更新，回调saf客户端，推送增量server节点。没有callback就不推送
        // addAliasServerMap 和 delAliasServerMap 有新增的组和减少的组变化, 或者相同组的节点有增减
        MapListServerDiff mld = new MapListServerDiff(newAliasServersMap, oldAliasServersMap);
        //删除节点
        Map<String, List<Server>> delAliasServerMap = mld.getOnlyOnRight();
        //添加节点
        Map<String, List<Server>> addAliasServerMap = mld.getOnlyOnLeft();

        //根据同机房策略和推送限制数量获取通知服务列表
        Map<String, List<Server>> tmpNewAliasServerMap = getUpdateAllProviderList(ifaceName, newAliasServersMap, oldAliasServersMap, newRoomStrategy, oldRoomStrategy, newProviderLimit, oldProviderLimit, delAliasServerMap, addAliasServerMap);
        if (tmpNewAliasServerMap != null) {
        	notifyClientsByAlias(ifaceName, tmpNewAliasServerMap, SubscribeUrl.PROVIDER_UPDATE_ALL);
        	return;
        }

        //add by wt 按ip路由规则通知客户端
        if (CommonUtils.isNotEmpty(newIpRouterMap) || CommonUtils.isNotEmpty(oldIpRouterMap)) {//ip路由配置变化，需要单独处理下。如果已经配置ip路由，或者曾经配置ip路由的，需要走一下逻辑，进行处理
            notifyClientsByIpRouter(ifaceName, newIpRouterMap, oldIpRouterMap, delAliasServerMap, addAliasServerMap);
            return;
        }

        //如果同机房策略没有变化，而且没有配置ip路由. 不可调整先删除后添加的顺序
        notifyClientsByAlias(ifaceName, delAliasServerMap, SubscribeUrl.PROVIDER_DEL);
        notifyClientsByAlias(ifaceName, addAliasServerMap, SubscribeUrl.PROVIDER_ADD);
    }

	/**
	 * 根据同机房策略和推送服务列表数量获取通知服务列表
	 * @param ifaceName
	 * @param newAliasServersMap
	 * @param oldAliasServersMap
	 * @param newRoomStrategy
	 * @param oldRoomStrategy
	 * @param newProviderLimit
	 * @param oldProviderLimit
	 * @param delAliasServerMap
	 * @param addAliasServerMap
	 * @return
	 */
    private Map<String, List<Server>> getUpdateAllProviderList(String ifaceName, Map<String, List<Server>> newAliasServersMap, Map<String, List<Server>> oldAliasServersMap, byte newRoomStrategy, byte oldRoomStrategy, 
			int newProviderLimit, int oldProviderLimit, Map<String, List<Server>> delAliasServerMap, Map<String, List<Server>> addAliasServerMap) {
		//如果是同机房策略，并且对删除的alias中现有的全量provider数小于UPDATEALL_COUNT个，就采用updateall事件
        boolean updateAll = false;
    	if (newRoomStrategy == DataEnum.RoomStrategyType.sameRoom.getValue()) {
    		Map<String, Map<Integer, Integer>> roomCountMap = new HashMap<String, Map<Integer, Integer>>();
    		if (checkSameRoomProviderCount(newAliasServersMap, addAliasServerMap, delAliasServerMap, roomCountMap) 
    				||checkSameRoomProviderCount(oldAliasServersMap, addAliasServerMap, delAliasServerMap, roomCountMap)) {
    			updateAll = true;
    		}
        }
    	Map<String, List<Server>> tmpNewAliasServerMap = null;
        if (newRoomStrategy != oldRoomStrategy || newProviderLimit != oldProviderLimit) {        //同机房策略发生变化或者推送服务列表数量变化，需要重新通知一遍所有相关的consumer
            tmpNewAliasServerMap = subscribeHelper.getInterfaceVo(ifaceName).aliasServersMap;
        } else if (subscribeHelper.getInterfaceVo(ifaceName).providerLimit > 1 || updateAll) {   //如果设置了推送服务列表数量, 或者同机房provider数小于UPDATEALL_COUNT个，推送所有节点
        	tmpNewAliasServerMap = newAliasServersMap;
        }
		return tmpNewAliasServerMap;
	}

	/**
	 * 如果变化节点所在的机房provider数量小于UPDATEALL_COUNT个，就返回true
	 * @param aliasServersMap
	 * @param addAliasServerMap
	 * @param delAliasServerMap
	 * @param roomCountMap
     * @return
     */
    private boolean checkSameRoomProviderCount(Map<String, List<Server>> aliasServersMap, Map<String, List<Server>> addAliasServerMap, Map<String, List<Server>> delAliasServerMap, Map<String, Map<Integer, Integer>> roomCountMap) {
    	try {
        	if (!CollectionUtils.isEmpty(aliasServersMap)) {
        		roomCountMap.clear();
        		//计算所有机房的provider数量
        		for (String alias : aliasServersMap.keySet()) {
    	    		List<Server> list = aliasServersMap.get(alias);
        			if (!CollectionUtils.isEmpty(list)) {
        				if (roomCountMap.get(alias) == null) {
        					roomCountMap.put(alias, new HashMap<Integer, Integer>());
        				}
        				for (Server server : list) {
        					if (roomCountMap.get(alias).get(server.getRoom()) == null) {
        						roomCountMap.get(alias).put(server.getRoom(), 0);
        						continue;
        					}
        					roomCountMap.get(alias).put(server.getRoom(), roomCountMap.get(alias).get(server.getRoom()).intValue() + 1);
        				}
        			}
        		}
        		if (!CollectionUtils.isEmpty(addAliasServerMap) && checkChangeAliasServerCount(addAliasServerMap, roomCountMap)) {
        			return true;
        		}
        		if (!CollectionUtils.isEmpty(delAliasServerMap) && checkChangeAliasServerCount(delAliasServerMap, roomCountMap)) {
        			return true;
        		}
        	}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    	return false;
    }

	/**
	 * true：同机房provider数量小于UPDATEALL_COUNT
	 * @param changeAliasServerMap
	 * @param roomCountMap
	 */
	private boolean checkChangeAliasServerCount(Map<String, List<Server>> changeAliasServerMap, Map<String, Map<Integer, Integer>> roomCountMap) {
		for (String alias : changeAliasServerMap.keySet()) {
			List<Server> list = changeAliasServerMap.get(alias);
			if (!CollectionUtils.isEmpty(list)) {
				for (Server server : list) {
					if (roomCountMap.get(alias) == null || roomCountMap.get(alias).get(server.getRoom()) == null || roomCountMap.get(alias).get(server.getRoom()).intValue() < UPDATEALL_COUNT) {
						return true;
					}
				}
			}
		}
		return false;
	}
    
	/**
	 * @param ifaceName
	 * @param newIpRouterMap
	 * @param oldIpRouterMap
	 * @param delAliasServerMap
	 * @param addAliasServerMap
	 */
    private void notifyClientsByIpRouter(String ifaceName, Map<String, List<String>> newIpRouterMap, Map<String, List<String>> oldIpRouterMap, Map<String, List<Server>> delAliasServerMap, Map<String, List<Server>> addAliasServerMap) {
		// modify by baoningtian  如果有ip路由，需要重新通知
		String alias = null;
		List<Server> serverList = null;
		Map<String, List<Server>> newAliasServerMap = subscribeHelper.getInterfaceVo(ifaceName).aliasServersMap;
		for (String insKey : subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet()) {
		    if (subscribeHelper.isCallbackNull(insKey)) {
		        continue;
		    }
		    //检查是否符合IP路由。如果符合新的或者旧的ip路由规则，就触发update_all事件。对旧的判断，是因为当ip路由规则发生变化后(比如删除ip路由规则)，需要重新通知下
		    if (subscribeHelper.checkClientIpRouter(ifaceName, subscribeHelper.getInstanceCache().get(insKey).getCallback().getClientIp(), newIpRouterMap)
		            || subscribeHelper.checkClientIpRouter(ifaceName, subscribeHelper.getInstanceCache().get(insKey).getCallback().getClientIp(), oldIpRouterMap)) {
		        for (Map.Entry<String, List<Server>> aliasServerEntry : newAliasServerMap.entrySet()) {
		            alias = aliasServerEntry.getKey();
		            serverList = aliasServerEntry.getValue();
		            subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, SubscribeUrl.PROVIDER_UPDATE_ALL, alias, serverList, insKey);
		        }
		    } else {//对于接口上有路由，但是clientIp不符合ip路由的就做del和add操作
		        //删除provider
		        for (Map.Entry<String, List<Server>> aliasServerEntry : delAliasServerMap.entrySet()) {
		            alias = aliasServerEntry.getKey();
		            serverList = aliasServerEntry.getValue();
		            subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, SubscribeUrl.PROVIDER_DEL, alias, serverList, insKey);
		        }
		        //添加provider
		        for (Map.Entry<String, List<Server>> addAliasServerEntry : addAliasServerMap.entrySet()) {
		        	alias = addAliasServerEntry.getKey();
		        	serverList = addAliasServerEntry.getValue();
		        	subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, SubscribeUrl.PROVIDER_ADD, alias, serverList, insKey);
		        }
		    }
		}
	}

    /**
     * 差异server数据推送给该接口下的所有实例
     * 1. 循环遍历变化的alias和provider，用alias和SubscribeCallbackList的key(alias:protocol)的alias比对，如果匹配，循环通知客户端，通知前验证黑白名单，再通知
     * @param ifaceName
     * @param changeAliasServerMap
     * @param type
     */
    public void notifyClientsByAlias(String ifaceName, Map<String, List<Server>> changeAliasServerMap, int type) {
        if (changeAliasServerMap != null && !changeAliasServerMap.isEmpty() && subscribeHelper.getInterfaceVo(ifaceName) != null && subscribeHelper.getInterfaceVo(ifaceName).getInterfaceName() != null) {
            //同一接口中，将各组的provider变化，通知给客户端
            String alias = null;
            List<Server> serverList = null;
            for (Map.Entry<String, List<Server>> aliasServerEntry : changeAliasServerMap.entrySet()) {
                alias = aliasServerEntry.getKey();
                serverList = aliasServerEntry.getValue();
                //遍历每个实例的callback
                for (String insKey : subscribeHelper.getInterfaceVo(ifaceName).getInsKeySet()) {
                    subscribeHelper.notifyInstanceToUpdateProviders(ifaceName, type, alias, serverList, insKey);
                }
            }
        }
    }

    /**
     * 根据路由规则，过滤provider
     * 返回true: 表示符合路由规则，或者没有路由规则; 
     * 返回false: 表示不符合路由规则
     * @param ifaceName
     * @param clientIp
     * @param serverIp
     * @return
     */
	private boolean filterServerByIpRouter(String ifaceName, String clientIp, String serverIp) {
        if (subscribeHelper.getInterfaceVo(ifaceName) == null) {
            logger.warn("ifaceName:{} is not exists, please check db", ifaceName);
            return true;
        }
        Map<String,List<String>> ipRouterMap = subscribeHelper.getInterfaceVo(ifaceName).cloneIpRouterMap();
        boolean clientIpHasIpRouterConfig = false;
        if (ipRouterMap != null && ipRouterMap.size() > 0) {
            for (Map.Entry<String, List<String>> entry : ipRouterMap.entrySet()) {
                String clientIpExp = entry.getKey();
                List<String> serverIpExpValue = entry.getValue();
                if (clientIp == null || clientIp.isEmpty()) {
                    logger.warn("{} client ip is null ", ifaceName);
                    return false;
                }
                //检查consumer ip是否符合路由
                if ("*".equals(clientIpExp) || RegistryUtil.match(clientIpExp, clientIp)) {
                    clientIpHasIpRouterConfig = true;
                    //一个客户端ip表达式可能配置多个ip路由
                    for (String serverIpExp : serverIpExpValue) {
                        //检查provider ip是否符合路由
                        if ("*".equals(serverIpExp) || RegistryUtil.match(serverIpExp, serverIp)) {
                            return true;
                        } else {
                            logger.info("exclude server {} for client ip {} by ip router for interface :{}", serverIp, clientIp, ifaceName);
                        }
                    }
                }
            }
        }
        // 该clientIp没有配置ip路由规则，则返回true,serverIp不做过滤
        if (!clientIpHasIpRouterConfig) {
            return true;
        }
        return false;
    }

    /**
     * 转换server为jsfurl
     * 过滤逻辑：
     * 1.根据protocol过滤provider
     * 2.根据同机房策略过滤provider(当同机房与ip路由同时都配置了，优先判断同机房)
     * 3.根据ip路由过滤provider
     * @param interfaceName
     * @param servers
     * @param clientIp
     * @param clientProtocolType
     * @param clientJsfVersion
     * @param clientSerialization
     * @return
     */
    public List<JsfUrl> filterServers(String interfaceName, List<Server> servers, String clientIp, int clientProtocolType, String clientJsfVersion, String clientSerialization, int callbackType) {
        List<JsfUrl> providers = new ArrayList<JsfUrl>();
        if (servers != null) {
            byte roomStrategy = subscribeHelper.getInterfaceVo(interfaceName) == null ? DataEnum.RoomStrategyType.noStrategy.getValue() : subscribeHelper.getInterfaceVo(interfaceName).roomStrategy;
            List<Server> tempServerList = new ArrayList<Server>();   //当采取订阅同机房provider策略时，过滤后providers列表为空时，就用其他机房的provider（也就是tempServerList）
            //1.过滤1：根据protocol过滤provider
            List<Server> filterServerList = filterServersByProtocol(servers, clientProtocolType, interfaceName, clientIp);
            
            if (roomStrategy == DataEnum.RoomStrategyType.noStrategy.getValue()) {
            	//2.过滤2：没有设置同机房策略
            	providers = filterServersByNoSameRoom(filterServerList, clientProtocolType, interfaceName, clientIp, clientJsfVersion, clientSerialization);
            } else {
            	//3.过滤3：设置同机房策略
            	providers = filterServersBySameRoom(filterServerList, clientProtocolType, interfaceName, clientIp, roomStrategy, tempServerList, clientJsfVersion, clientSerialization);
            }

            //增量推送不做以下处理。 以下判断是针对采用同机房节点订阅策略后，consumer所在机房provider数小于2时，就将其他机房的provider推给该consumer
            if ((callbackType != SubscribeUrl.PROVIDER_ADD && callbackType != SubscribeUrl.PROVIDER_DEL) && providers.size() < PROVIDERLIMIT && roomStrategy != DataEnum.RoomStrategyType.noStrategy.getValue() && !tempServerList.isEmpty()) {
                providers = getJsfUrlList(tempServerList, clientJsfVersion, clientSerialization);
                logger.warn("接口{}的consumer ip:{}同机房provider数小于2个，开始推送所有机房服务列表:{}", interfaceName, clientIp, providers.toString());
            }
        }
        //限制服务列表数量，必须以update_all事件推送才行
        return providerCountLimit(interfaceName, providers);
    }

    /**
     * 过滤协议不一致的server
     * @param serverList
     * @param protocol
     * @param interfaceName
     * @param clientIp
     * @return
     */
    private List<Server> filterServersByProtocol(List<Server> serverList, int protocol, String interfaceName, String clientIp) {
    	List<Server> result = new ArrayList<Server>();
    	for (Server server: serverList) {
    		if (server.getProtocol() == protocol) {
    			result.add(server);
    		} else {
    			logger.info("exclude server {}:{},pid:{} for client ip:{} by not same protocol for interface :{}", server.getIp(), server.getPort(), server.getPid(), clientIp, interfaceName);
    		}
    	}
    	return result;
    }

    /**
     * 过滤没有设置同机房策略
     * @param serverList
     * @param protocol
     * @param interfaceName
     * @param clientIp
     * @return
     */
    private List<JsfUrl> filterServersByNoSameRoom(List<Server> serverList, int protocol, String interfaceName, String clientIp, String clientJsfVersion, String clientSerialization) {
    	List<JsfUrl> result = new ArrayList<JsfUrl>();
    	for (Server server: serverList) {
    		//过滤：如果不配置同机房推送策略，则根据ip路由过滤provider（同机房策略优先级比ip路由优先级高）
            if (filterServerByIpRouter(interfaceName, clientIp, server.getIp()) == false) {
            	logger.info("exclude server {}:{},pid:{} for client ip:{} by iprouter for interface :{}", server.getIp(), server.getPort(), server.getPid(), clientIp, interfaceName);
                continue;
            }
            result.add(getJsfUrlFromServer(server, server.getWeight(), clientJsfVersion, clientSerialization));
    	}
    	return result;
    }

	/**
	 * 过滤同机房策略. 如果设置同机房优先策略，则ip路由失效
	 * @param serverList
	 * @param protocol
	 * @param interfaceName
	 * @param clientIp
	 * @param roomStrategy
	 * @param tempServerList
	 * @param clientJsfVersion
	 * @param clientSerialization
     * @return
     */
    private List<JsfUrl> filterServersBySameRoom(List<Server> serverList, int protocol, String interfaceName, String clientIp, byte roomStrategy, List<Server> tempServerList, String clientJsfVersion, String clientSerialization) {
    	int clientIpRoom = configServiceImpl.getRoomByIp(clientIp);
    	List<JsfUrl> result = new ArrayList<JsfUrl>();
    	int weight = 0;
    	for (Server server: serverList) {
    		weight = server.getWeight();
    		if (roomStrategy == DataEnum.RoomStrategyType.weightFactor.getValue()) {  //同机房provider权重放大
            	if (clientIpRoom != 0 && clientIpRoom == server.getRoom()) {   //判断consumer与provider是否同机房
                    //与clientIp在同机房的provider做权重放大处理
                    weight = weight * subscribeHelper.getGlobalWeightFactor();
                    logger.info("clientIp:{}, weight changed to {}, cause of same room, server:{}", clientIp, weight, server.toString());
                }
            } else if (roomStrategy == DataEnum.RoomStrategyType.sameRoom.getValue() ) {   //订阅同机房provider
                tempServerList.add(server);
                //判断consumer的ip与provider是否是同机房。如果是权重放大策略，对同机房的provider进行权重放大; 如果是同机房节点筛选策略，只将同机房的provider推送给consumer
                if (clientIpRoom != 0 && clientIpRoom != server.getRoom()) {  //不是同机房，就过滤掉此节点
                    logger.info("exclude server {}:{},pid:{} for client ip:{} by sameRoomStrategy for interface :{}", server.getIp(), server.getPort(), server.getPid(), clientIp, interfaceName);
                    continue;
                }
            }
    		result.add(getJsfUrlFromServer(server, weight, clientJsfVersion, clientSerialization));
    	}
    	return result;
    }

    private List<JsfUrl> getJsfUrlList(List<Server> serverList, String clientJsfVersion, String clientSerialization) {
    	List<JsfUrl> result = new ArrayList<JsfUrl>();
    	for (Server server: serverList) {
    		result.add(getJsfUrlFromServer(server, server.getWeight(), clientJsfVersion, clientSerialization));
    	}
    	return result;
    }

    /**
     * 将server转为jsfurl
     * @param server
     * @param weight
     * @param clientJsfVersion
     * @param clientSerialization
     * @return
     */
    private JsfUrl getJsfUrlFromServer(Server server, int weight, String clientJsfVersion, String clientSerialization) {
        Map<String, String> attrs = RegistryUtil.getAttrMap(server.getAttrUrl());
        attrs.put(RegistryConstants.WEIGHT, String.valueOf(weight));
        attrs.put(RegistryConstants.SAFVERSION, String.valueOf(server.getSafVer()));
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp(server.getIp());
        jsfUrl.setPort(server.getPort());
        jsfUrl.setPid(server.getPid());
        jsfUrl.setAlias(server.getAlias());
        jsfUrl.setProtocol(server.getProtocol());
        String serverJsfVersion = RegistryUtil.getIntValueFromMapString(server.getAttrUrl(), RegistryConstants.JSFVERSION);
        if (serverJsfVersion != null && !serverJsfVersion.isEmpty()) {
            attrs.put(RegistryConstants.JSFVERSION, serverJsfVersion);
        }
        if (serverJsfVersion != null && clientJsfVersion != null && !serverJsfVersion.equals(clientJsfVersion)) {
            try {
                JsfSerialization serial = subscribeHelper.getSerialization(serverJsfVersion);
                //如果consumer的序列化，不在server的jsf版本对应的序列化列表中，就给出consumer的jsf版本的默认序列化
                if (serial != null && !serial.getList().contains(clientSerialization)) {
                    attrs.put(RegistryConstants.SERIALIZATION, serial.getFirstSerialization());
                }
            } catch (Exception e) {
                logger.error("parse serialization is error:{}, clientJsfVersion:{}, clientSerialization:{}, server:{}", e.getMessage(), clientJsfVersion, clientSerialization, server.toString());
            }
        }
        jsfUrl.setAttrs(attrs);
        return jsfUrl;
    }

    /**
     * 限制返回provider的数量
     * @param interfaceName
     * @param providerList
     * @return
     */
    private List<JsfUrl> providerCountLimit(String interfaceName, List<JsfUrl> providerList) {
    	List<JsfUrl> result = null;
    	if (providerList.isEmpty() || subscribeHelper.getInterfaceVo(interfaceName).providerLimit <= 1 || subscribeHelper.getInterfaceVo(interfaceName).providerLimit >= providerList.size()) {
    		result = providerList;
    	} else if (subscribeHelper.getInterfaceVo(interfaceName).providerLimit < providerList.size()) {
    		try {
    			result = new ArrayList<JsfUrl>();
				int start = (int) (System.currentTimeMillis() % providerList.size());
    			for (int i = 0; i < subscribeHelper.getInterfaceVo(interfaceName).providerLimit; i++) {
    				result.add(providerList.get(start++ % providerList.size()));
    			}
			} catch (Exception e) {
				logger.error("限制provider返回数量失效, " + e.getMessage(), e);
				result = providerList;  //如果失败，就使用原来的服务列表
			}
    	}
    	return result;
    }


}
