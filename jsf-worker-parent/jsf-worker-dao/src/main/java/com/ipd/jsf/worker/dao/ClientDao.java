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

import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Repository
public interface ClientDao {

    /**
     * 创建client
     *
     * @param clientList
     * @return
     * @throws Exception
     */
    public int create(@Param("clientList") List<Client> clientList) throws Exception;

    /**
     * 更新client
     *
     * @param client
     * @return
     * @throws Exception
     */
    public int update(Client client) throws Exception;

    /**
     * 获取client
     *
     * @param uniqKeyList
     * @return
     * @throws Exception
     */
    public List<String> getUniqKeyList(@Param("list") List<String> uniqKeyList) throws Exception;

    /**
     * 更新节点为上线状态
     *
     * @param list
     * @return
     * @throws Exception
     */
    public int updateStatusOnline(@Param("list") List<Client> list) throws Exception;

    /**
     * 更新节点为死亡状态
     *
     * @param list
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(@Param("list") List<Client> list) throws Exception;

    /**
     * 更新节点为上线状态
     *
     * @param list
     * @return
     * @throws Exception
     */
    @Deprecated
    public int updateStatusOnlineByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 更新节点为死亡状态
     *
     * @param list
     * @return
     * @throws Exception
     */
    @Deprecated
    public int updateStatusOfflineByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 根据uniqkey删除节点
     *
     * @param uniqkeyList
     * @param srcType
     * @return
     * @throws Exception
     */
    public int deleteByUniqkey(@Param("uniqkeyList") List<String> uniqkeyList, @Param("srcType") int srcType) throws Exception;

    /**
     * 根据clientId删除
     *
     * @param idList
     * @param sourceType
     * @return
     * @throws Exception
     */
    public int deleteById(@Param("idList") List<Integer> idList, @Param("sourceType") int sourceType) throws Exception;

    /**
     * 获取状态为存活的节点
     * @param list
     * @return
     * @throws Exception
     */
    public List<Client> getOnlineClientsByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取状态为死亡的节点
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<Client> getOfflineClientsByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取节点
     *
     * @param list
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 按照接口名获取client列表
     *
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public List<Client> getListByInterfaceName(@Param("interfaceName") String interfaceName) throws Exception;

    /**
     * 按照接口名获取client列表
     *
     * @param interfaceName
     * @param alias
     * @return
     * @throws Exception
     */
    public List<Client> getListByInterfaceNameAndAlias(@Param("interfaceName") String interfaceName, @Param("alias") String alias) throws Exception;

    /**
     * 获取来源是registry的无实例client列表，并获取小于date时间的数据
     *
     * @return
     * @throws Exception
     */
    public List<Client> getNoInsClients(@Param("date") Date date) throws Exception;

    /**
     * 根据状态获取client
     *
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByStatus(@Param("status") int status) throws Exception;

    public int getJsfVerCount(String interfaceName) throws Exception;

    /**
     * @param interfaceName
     * @return
     */
    public List<Client> getJsfClients(String interfaceName);

    //根据interfaceId，serverAlias查看consumer的数目
    int getCountByIfaceAndAlias(@Param("interfaceId") int interfaceId, @Param("clientAlias") String clientAlias);
    
    /**
	 * 查询该接口下面有效的客户端信息
	 * 
	 * @param interfaceName
	 * @return
	 */
	public List<Map<String,Object>> listByInterfaceName(String interfaceName);

}