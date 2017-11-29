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
package com.ipd.jsf.registry.lds;

public class LdsUtil {

//    /**
//     * 将jsfUrl转为server对象
//     * @param server
//     * @param iface
//     * @return
//     */
//    public static ProviderNodeInfo getProviderNodeInfoFromServer(Server server) {
//        ProviderNodeInfo provider = new ProviderNodeInfo();
//        provider.setIp(server.getIp());
//        provider.setPort(server.getPort());
//        provider.setPid(server.getPid());
//        provider.setInterfaceId(server.getInterfaceId());
//        provider.setInterfaceName(server.getInterfaceName());
//        provider.setAlias(server.getAlias());
//        provider.setProtocol(server.getProtocol());
//        provider.setTimeout(server.getTimeout());
//        provider.setWeight(server.getWeight());
//        provider.setRandom(server.isRandom());
//        provider.setSrcType(SourceType.registry.value());
//        provider.setInsKey(server.getInsKey());
//        provider.setSafVer(server.getSafVer());
//        provider.setAppPath(server.getAppPath());
//        provider.setContextPath(server.getContextPath());
//        provider.setStatus(server.getStatus());
//        provider.setStartTime(server.getStartTime());
//        provider.setUrlDesc(server.toString());
//        provider.setCreateTime(server.getCreateTime());
//        provider.setUpdateTime(server.getUpdateTime());
//        provider.setAttrUrl(server.getAttrUrl());
//        provider.setRegIp(RegistryUtil.getRegistryIPPort());
//        provider.setUniqKey(UniqkeyUtil.getServerUniqueKey(provider.getIp(), provider.getPort(), provider.getAlias(), provider.getProtocol(), provider.getInterfaceId()));
//        return provider;
//    }
//    
//    /**
//     * 将jsfUrl转为server对象
//     * @param server
//     * @param iface
//     * @return
//     */
//    public static List<ProviderNodeInfo> getProviderNodeInfosFromServers(List<Server> serverList) {
//    	List<ProviderNodeInfo> list = new ArrayList<ProviderNodeInfo>();
//    	for (Server server : serverList) {
//    		list.add(getProviderNodeInfoFromServer(server));
//    	}
//    	return list;
//    }
//
//    public static ConsumerNodeInfo getConsumerNodeInfoFromClient(Client client) {
//    	ConsumerNodeInfo info = new ConsumerNodeInfo();
//    	info.setAlias(client.getAlias());
//    	info.setAppPath(client.getAppPath());
//    	info.setcId(client.getcId());
//    	info.setCreateTime(client.getCreateTime());
//    	info.setcType(client.getcType());
//    	info.setInsKey(client.getInsKey());
//    	info.setInterfaceId(client.getInterfaceId());
//    	info.setInterfaceName(client.getInterfaceName());
//    	info.setIp(client.getIp());
//    	info.setPid(client.getPid());
//    	info.setProtocol(client.getProtocol());
//    	info.setRegIp(RegistryUtil.getRegistryIPPort());
//    	info.setSafVer(client.getSafVer());
//    	info.setStartTime(client.getStartTime());
//    	info.setStatus(client.getStatus());
//    	info.setUniqKey(client.getUniqKey());
//    	info.setUpdateTime(client.getUpdateTime());
//    	info.setUrlDesc(client.getUrlDesc());
//    	return info;
//    }
//    
//    public static List<ConsumerNodeInfo> getConsumerNodeInfosFromClients(List<Client> clientList) {
//    	List<ConsumerNodeInfo> list = new ArrayList<ConsumerNodeInfo>();
//    	for (Client client : clientList) {
//    		list.add(getConsumerNodeInfoFromClient(client));
//    	}
//    	return list;
//    }
//    
//    public static InstanceInfo getInstanceInfoFromJsfIns(JsfIns ins) {
//    	InstanceInfo insInfo = new InstanceInfo();
//    	insInfo.setAppId(ins.getAppId());
//    	insInfo.setAppInsId(ins.getAppInsId());
//    	insInfo.setAppName(ins.getAppName());
//    	insInfo.setInstanceKey(ins.getInsKey());
//    	insInfo.setIp(ins.getIp());
//    	insInfo.setJsfVersion(ins.getSafVer());
//    	insInfo.setPid(ins.getPid());
//    	insInfo.setRegIp(RegistryUtil.getRegistryIPPort());
//    	insInfo.setRoom(ins.getInsRoom());
//    	insInfo.setStartTime(ins.getStartTime());
//    	insInfo.setUpdateTime(ins.getCreateTime());
//    	insInfo.setLanguage(ins.getLanguage());
//    	insInfo.setLastHBTime(ins.getHb().getTime());
//    	return insInfo;
//    }
//
//    public static List<InsHBInfo> getInsHbInfo(List<String> insKeyList, long hbtime) {
//    	List<InsHBInfo> result = new ArrayList<InsHBInfo>();
//    	if (!CollectionUtils.isEmpty(insKeyList)) {
//    		for (String insKey : insKeyList) {
//	    		InsHBInfo hbInfo = new InsHBInfo();
//	    		hbInfo.setInsKey(insKey);
//	    		hbInfo.setHb(hbtime);
//	    		result.add(hbInfo);
//    		}
//    	}
//    	return result;
//    }
}
