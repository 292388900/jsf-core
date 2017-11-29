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

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;

import java.util.Date;
import java.util.List;

public interface ClientManager {

    /**
     * 新建client 
     * @param clientList
     * @return
     * @throws Exception
     */
    public int create(List<Client> clientList) throws Exception;

    /**
     * 更新client 
     * @param client
     * @return
     * @throws Exception
     */
    public int update(Client client) throws Exception;

    /**
     * 获取client的uniqKey 
     * @param uniqKeyList
     * @return
     * @throws Exception
     */
    public List<String> getUniqKeyList(List<String> uniqKeyList) throws Exception;

    /**
     * 更新节点为上线状态
     * @param list
     * @return
     * @throws Exception
     */
    public int updateStatusOnline(List<Client> list) throws Exception;

    /**
     * 更新节点为死亡状态
     * @param list
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(List<Client> list) throws Exception;

    /**
     * 获取实例下的节点
     * @param list
     * @param status
     * @return
     * @throws Exception
     */
    public List<Client> getClientByIns(List<JsfIns> list, InstanceStatus status) throws Exception;

    /**
     * 获取实例下的节点
     * @param list
     * @return
     * @throws Exception
     */
    public List<Client> getClientByIns(List<JsfIns> list) throws Exception;

    /**
     * 根据状态获取client
     * @param status
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByStatus(InstanceStatus status) throws Exception;

    /**
     * 批量删除节点
     * @param clientList
     * @param srcType
     * @return
     * @throws Exception
     */
    public int deleteByUniqkey(List<Client> clientList, int srcType) throws Exception;

    /**
     * 根据clientId删除client
     * @param clientList
     * @return
     * @throws Exception
     */
    public int deleteById(List<Client> clientList, int sourceType) throws Exception;

    /**
     * 按照接口名获取client列表
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public List<Client> getListByInterfaceName(String interfaceName) throws Exception;

    /**
     * 按照接口名获取client列表
     * @param interfaceName
     * @param alias
     * @return
     * @throws Exception
     */
    public List<Client> getListByInterfaceNameAndAlias(String interfaceName, String alias) throws Exception;

    /**
     * 获取来源是registry的无实例client列表，并获取小于date时间的数据
     * @return
     * @throws Exception
     */
    public List<Client> getNoInsClients(Date date) throws Exception;

    public boolean hafJsfVer(String interfaceName) throws Exception;

    /**
     * @param interfaceName
     * @return
     */
    public List<Client> getJsfClients(String interfaceName);

}