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
package com.ipd.jsf.worker.manager;

import java.util.List;
import java.util.Map;

import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.Server;

public interface ServerManager {

    /**
     * 新建server 
     * @param serverList
     * @return
     * @throws Exception
     */
    public int create(List<Server> serverList) throws Exception;

    /**
     * 更新server 
     * @param server
     * @return
     * @throws Exception
     */
    public int update(Server server) throws Exception;

    /**
     * 获取server的uniqKey 
     * @param uniqKey
     * @return
     * @throws Exception
     */
    public List<String> getUniqKeyList(List<String> uniqKeyList) throws Exception;

    /**
     * 更新节点为上线状态
     * @param server
     * @return
     * @throws Exception
     */
    public int updateStatusOnline(Server server) throws Exception;

    /**
     * 更新节点为死亡状态
     * @param server
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(Server server) throws Exception;

    /**
     * 更改节点为强制下线并死亡状态 
     * @param interfaceId
     * @param serverId
     * @param lastHbTime
     * @return
     * @throws Exception
     */
    public int updateStatusOfflineNotwork(Server server) throws Exception;

    /**
     * 批量删除节点
     * @param serverList
     * @param sourceType
     * @return
     * @throws Exception
     */
    public int deleteServer4ZkSyn(List<Server> serverList, int sourceType) throws Exception;

    /**
     * 批量删除节点
     * @param serverList
     * @return
     * @throws Exception
     */
    public int batchDelete(List<Server> serverList) throws Exception;

    /**
     * 获取状态为存活的节点
     * @param insList
     * @return
     * @throws Exception
     */
    public List<Server> getOnlineServersByIns(List<JsfIns> insList) throws Exception;

    /**
     * 获取状态为死亡的节点
     * @param insList
     * @return
     * @throws Exception
     */
    public List<Server> getOfflineServersByIns(List<JsfIns> insList) throws Exception;

    /**
     * 获取状态为死亡的节点
     * @return
     * @throws Exception
     */
    public List<Server> getOfflineServers() throws Exception;

    /**
     * 按照接口获取server列表
     * @param interfaceInfo
     * @return
     * @throws Exception
     */
    public List<Server> getListByInterface(InterfaceInfo interfaceInfo) throws Exception;

    /**
     * 按照接口Id获取server列表
     * @param interfaceId
     * @return
     * @throws Exception
     */
    public List<Server> getListByInterfaceId(int interfaceId) throws Exception;

    public List<Server> getListByInterfaceName(String interfaceName) throws Exception;

    public List<Server> getListByInterfaceNameAndAlias(String interfaceName, String alias) throws Exception;

    /**
     * 获取无实例server列表
     * @return
     * @throws Exception
     */
    public List<Server> getNoInsServers() throws Exception;

    /**
     * 根据实例获取server列表
     * @param insList
     * @return
     * @throws Exception
     */
    public List<Server> getServersByIns(List<JsfIns> insList) throws Exception;

    public boolean hafJsfVer(String interfaceName) throws Exception;

    /**
     * @param interfaceName
     * @return
     */
    public List<Server> getJsfServers(String interfaceName);

    /**
     * 根据IP,接口名,别名,[自动部署ID]获取Server列表(serverId,server_uniquekey,interfaceId)
     * @param params
     * @return
     */
    public List<Server> getServersForDynamicGrouping(Map<String, Object> params);

}