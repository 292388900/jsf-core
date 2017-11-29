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
package com.ipd.jsf.worker.dao;

import java.util.List;
import java.util.Map;

import com.ipd.jsf.worker.domain.JsfIns;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.Server;

@Repository
public interface ServerDao {

    /**
     * 创建server
     *
     * @param serverList
     * @return
     * @throws Exception
     */
    public int create(@Param("serverList") List<Server> serverList) throws Exception;

    /**
     * 更新server
     *
     * @param server
     * @return
     * @throws Exception
     */
    public int update(Server server) throws Exception;

    /**
     * 获取server
     *
     * @param uniqKeyList
     * @return
     * @throws Exception
     */
    public List<String> getUniqKeyList(@Param("list") List<String> uniqKeyList) throws Exception;

    /**
     * 更新节点为上线状态
     *
     * @param serverId
     * @return
     * @throws Exception
     */
    public int updateStatusOnline(@Param("serverId") int serverId) throws Exception;

    /**
     * 更新节点为死亡状态
     *
     * @param serverId
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(@Param("serverId") int serverId) throws Exception;

    /**
     * 更新节点为强制下线并死亡
     *
     * @param serverId
     * @return
     * @throws Exception
     */
    public int updateStatusOfflineNotwork(@Param("serverId") int serverId) throws Exception;

    /**
     * 根据serverId列表，删除server
     *
     * @param IdList
     * @param sourceType
     * @return
     * @throws Exception
     */
    public int deleteById(@Param("idList") List<Integer> IdList, @Param("sourceType") int sourceType) throws Exception;

    /**
     * 根据实例key查找节点
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<Server> getServersByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取状态为存活的节点
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<Server> getOnlineServersByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取状态为死亡的节点
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<Server> getOfflineServersByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取状态为死亡的节点
     *
     * @return
     * @throws Exception
     */
    public List<Server> getOfflineServers() throws Exception;

    /**
     * 按照接口名获取server列表
     *
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public List<Server> getListByInterfaceName(@Param("interfaceName") String interfaceName) throws Exception;

    /**
     * 按照接口名获取server列表
     *
     * @param interfaceName
     * @param alias
     * @return
     * @throws Exception
     */
    public List<Server> getListByInterfaceNameAndAlias(@Param("interfaceName") String interfaceName, @Param("alias") String alias) throws Exception;

    /**
     * 按照接口ID获取server列表
     *
     * @param interfaceId
     * @return
     * @throws Exception
     */
    public List<Server> getListByInterfaceId(@Param("interfaceId") int interfaceId) throws Exception;

    /**
     * 获取无实例server列表
     *
     * @return
     * @throws Exception
     */
    public List<Server> getNoInsServers() throws Exception;

    /**
     * 获取jsf版本数量
     *
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public int getJsfVerCount(String interfaceName) throws Exception;

    /**
     * @param interfaceName
     * @return
     */
    public List<Server> getJsfServers(String interfaceName);

    /**
     * 根据appId和appInsId查找server
     *
     * @param appId
     * @param appInsId
     * @return
     * @throws Exception
     */
    public List<Server> getServersByApp(@Param("appId") int appId, @Param("appInsId") String appInsId, @Param("pid") Integer pid) throws Exception;

    /**
     * 更新实例的provider为强制下线状态
     *
     * @param serverIds
     * @return
     */
    public int updateServerToOffline(@Param("serverIds") List<Integer> serverIds);

    /**
     * 更新实例的provider为上线状态
     *
     * @param serverIds
     * @return
     */
    public int updateServerToOnline(@Param("serverIds") List<Integer> serverIds);

    /**
     * 根据IP,接口名,别名,[自动部署ID]获取Server列表(serverId,server_uniquekey,interfaceId)
     *
     * @param params
     * @return
     */
    public List<Server> getServersForDynamicGrouping(Map<String, Object> params);


    //根据interfaceId，serverAlias查看provider的数目
    int getCountByIfaceAndAlias(@Param("interfaceId") int interfaceId, @Param("serverAlias") String serverAlias);
    
    /**
	 * 查询该接口下面有效的服务端信息
	 * 
	 * @param interfaceName
	 * @return
	 */
    public List<Map<String,Object>> listByInterfaceName(String interfaceName);

}