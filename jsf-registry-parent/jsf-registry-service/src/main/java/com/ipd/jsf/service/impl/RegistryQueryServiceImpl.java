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

import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.service.RegistryQueryService;
import com.ipd.jsf.service.vo.*;
import com.ipd.jsf.vo.JsfUrl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RegistryQueryServiceImpl implements RegistryQueryService {

    private final String INTERFACE = "interface";
    private final String IP = "ip";
    private final String ALIAS = "alias";
    private final String PROTOCOL = "protocol";
    private final String SRC = "src"; //0-管理端
    private final String SUBIP = "subIp";
    private final String ALIAS_VERSION = "interface.alias.version";

    @Autowired
    private SubscribeHelper subscribeHelper;

    @Autowired
    private SubscribeService subscribeServiceImpl;

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryQueryService#getProviders(java.lang.String)
     */
    @Override
    public InterfaceInfoVo getProviders(Paging paging) throws Exception {
        return getProviderList(paging);
    }

    @Override
    public InterfaceInfoVo getProviders4Simple(Paging paging) throws Exception {
        return getProviderList(paging);
    }

    /**
     * 获取服务列表
     * @param paging
     * @return
     * @throws Exception
     */
    private InterfaceInfoVo getProviderList(Paging paging) throws Exception {
        String interfaceName = (String) getParam(paging, INTERFACE, true);
        String ip = (String) getParam(paging, IP, false);
        String alias = (String) getParam(paging, ALIAS, false);
        int protocol = getParam(paging, PROTOCOL, false) == null ? -1 : Integer.parseInt(String.valueOf(getParam(paging, PROTOCOL, false)));
        String src = (String) getParam(paging, SRC, false);
        String subIp = (String) getParam(paging, SUBIP, false);

        InterfaceInfoVo vo = new InterfaceInfoVo();

        Map<String, List<Server>> aliasServersMap;
        Map<String, List<Integer>> aliasServerIdsMap = new HashMap<String, List<Integer>>();
        List<Server> serverSourceList = new ArrayList<Server>();
        Long aliasVersion = 0L;
        //1. 获取alias server
        if (src == null || src.isEmpty()) {
            aliasServersMap = subscribeHelper.getAliasServers(interfaceName);
            aliasServerIdsMap = getAliasServers(aliasServersMap);
            serverSourceList = getServers(alias, aliasServersMap);
        } else if ("0".equals(src) && alias != null && !alias.isEmpty()) {
            if (subIp != null && !subIp.isEmpty()) {
                List<JsfUrl> providerList = subscribeServiceImpl.subscribe(interfaceName, alias, protocol, null, null, null, subIp, null, 0);
                serverSourceList = getServersFromJsfUrls(providerList);
            } else {
                serverSourceList = subscribeHelper.getAliasServers(interfaceName, alias);
            }
            aliasServersMap = new HashMap<String, List<Server>>();
            aliasServersMap.put(alias, serverSourceList);
            List<Integer> serverIds = getAliasServers(serverSourceList);
            aliasServerIdsMap.put(alias, serverIds);
        } else {
            //TODO
        }

        if (serverSourceList == null) {
            serverSourceList = new ArrayList<Server>();
        }

        if (alias != null && !alias.isEmpty()) {
            aliasVersion = subscribeHelper.getAliasVersion(interfaceName, alias);
        }
        vo.setAliasServers(aliasServerIdsMap);

        //2. 获取provider列表
        List<Server> serverResultList = new ArrayList<Server>();
        for (Server server : serverSourceList) {
            //(1)过滤ip
            if (ip != null && !ip.isEmpty()) {
                if (!ip.equals(server.getIp())) { //ip不匹配，就比较下一个server
                    continue;
                }
            }
            //(2)过滤protocol
            if (protocol >= 0) {
                if (protocol != server.getProtocol()) {  //protocol不匹配，就比较下一个server
                    continue;
                }
            }
            serverResultList.add(server);
        }

        //(3)分页
        vo.setProviderTotalRecord(serverResultList.size());
        paging.setTotalRecord(serverResultList.size());
        int startIndex = (paging.getPageIndex() - 1) * paging.getPageSize();
        int endIndex = paging.getPageIndex() * paging.getPageSize();
        if (startIndex <= serverResultList.size() && !serverResultList.isEmpty()) {
            if (endIndex > serverResultList.size()) {
                endIndex = serverResultList.size();
            }
            vo.setProviders(getProvidersFromServers(interfaceName, serverResultList.subList(startIndex, endIndex)));
        }

        //3.获取接口配置
        Map<String, String> configTemp = subscribeServiceImpl.getInterfaceProperty(interfaceName, null, null, null);
        Map<String, String> configs = new HashMap<String, String>();
        if (configTemp != null) {
            configs.putAll(configTemp);
        }
        configs.put(ALIAS_VERSION, aliasVersion + "");
        vo.setConfigs(configs);
        return vo;
    }

    /**
     * 获取alias 与server对应关系,用于简易管理端用
     * @param aliasServersMap
     * @return
     */
    private Map<String, List<Integer>> getAliasServers(Map<String, List<Server>> aliasServersMap) {
        Map<String, List<Integer>> result = new HashMap<String, List<Integer>>();
        if (aliasServersMap != null) {
            for (Map.Entry<String, List<Server>> entry : aliasServersMap.entrySet()) {
                for (Server server : entry.getValue()) {
                    if (result.get(entry.getKey()) == null) {
                        result.put(entry.getKey(), new ArrayList<Integer>());
                    }
                    result.get(entry.getKey()).add(server.getId());
                }
            }
        }
        return result;
    }

    /**
     * 获取alias 与server对应关系,用于简易管理端用
     * @param servers
     * @return
     */
    private List<Integer> getAliasServers(List<Server> servers) {
        List<Integer> result = new ArrayList<Integer>();
        if (servers != null) {
            for (Server server : servers) {
                result.add(server.getId());
            }
        }
        return result;
    }

    private List<Server> getServers(String alias, Map<String, List<Server>> aliasServersMap) {
        List<Server> result = new ArrayList<Server>();
        if (alias != null && !alias.isEmpty() && aliasServersMap.get(alias) != null) {
            result.addAll(aliasServersMap.get(alias));
        } else {
            for (List<Server> list : aliasServersMap.values()) {
                result.addAll(list);
            }
        }
        return result;
    }

    /**
     * @param paging
     * @param key
     * @return
     */
    private Object getParam(Paging paging, String key, boolean check) {
        Object value = null;
        value = paging.getParams().get(key);
        if (check && value == null) {
            throw new RpcException(key + " is null");
        }
        return value;
    }

    private List<Provider> getProvidersFromServers(String interfaceName, List<Server> servers) {
        List<Provider> result = new ArrayList<Provider>();
        for (Server server : servers) {
            Provider p = new Provider();
            p.setId(server.getId());
            p.setIp(server.getIp());
            p.setPort(server.getPort());
            p.setPid(server.getPid());
            p.setAlias(server.getAlias());
            p.setWeight(server.getWeight());
            p.setProtocol(server.getProtocol());
            p.setInterfaceName(interfaceName);
            p.setSafVer(server.getSafVer());
            result.add(p);
        }
        return result;
    }

    private List<Server> getServersFromJsfUrls(List<JsfUrl> jsfUrls) {
        List<Server> result = new ArrayList<Server>();
        for (JsfUrl jsfUrl : jsfUrls) {
            Server p = new Server();
            p.setIp(jsfUrl.getIp());
            p.setPort(jsfUrl.getPort());
            p.setPid(jsfUrl.getPid());
            p.setAlias(jsfUrl.getAlias());
            p.setProtocol(jsfUrl.getProtocol());
            p.setInterfaceName(jsfUrl.getIface());
            result.add(p);
        }
        return result;
    }

    @Override
    public InstanceResponse getInstanceList(Paging paging) throws Exception {
        String inskey = getParam(paging, "inskey", false) == null ? null : (String) getParam(paging, "inskey", false);
        List<Instance> temp = subscribeHelper.getInskeyList(inskey, false);

        InstanceResponse result = new InstanceResponse();
        result.setTotalRecord(temp.size());
        int startIndex = (paging.getPageIndex() - 1) * paging.getPageSize();
        int endIndex = paging.getPageIndex() * paging.getPageSize();
        if (startIndex < temp.size()) {
            if (endIndex > temp.size()) {
                endIndex = temp.size();
            }
            if (endIndex - startIndex > 0) {
                List<Instance> subList = temp.subList(startIndex, endIndex);
                List<Instance> list = new ArrayList<Instance>();
                for (Instance i : subList) {
                    list.add(i);
                }
                result.setInstanceList(list);
            }
        }
        return result;
    }

    @Override
    public Instance getInstance(String insKey) throws Exception {
        if (insKey == null || insKey.isEmpty()) return null;
        Instance result = null;
        String ip = UniqkeyUtil.getIpFromInsKey(insKey);
        int pid = UniqkeyUtil.getPidFromInsKey(insKey);
        List<Instance> temp = subscribeHelper.getInskeyList(insKey, true);
        if (!temp.isEmpty()) {
            result = temp.get(0);
            if (result.getIfaceAliasProtocolMap() != null && !result.getIfaceAliasProtocolMap().isEmpty()) {
                for (String interfaceName : result.getIfaceAliasProtocolMap().keySet()) {
                    //1. 获取接口provider列表
                    List<Server> servers = subscribeHelper.getServers(interfaceName);
                    List<Server> tempServers = null;
                    for (Server server : servers) {
                        //找到匹配inskey的server
                        if (server.getIp().equals(ip) && server.getPid() == pid) {
                            if (tempServers == null) {
                                tempServers = new ArrayList<Server>();
                            }
                            tempServers.add(server);
                        }
                    }
                    //将匹配的server放入result中
                    if (tempServers != null && !tempServers.isEmpty()) {
                        result.getProviders().addAll(getProvidersFromServers(interfaceName, tempServers));
                    }

                    //2. 获取接口配置信息
                    Map<String, String> config = subscribeServiceImpl.getInterfaceProperty(interfaceName, null, null, null);
                    if (config != null && !config.isEmpty()) {
                        result.getConfig().put(interfaceName, config);
                    }
                }

            }
        }
        return result;
    }
}
