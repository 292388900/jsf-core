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

import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;

public interface ScanClientManager {

    /**
     * 更新节点为死亡状态
     * @param clientList
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(List<Client> clientList) throws Exception;

    /**
     * 批量逻辑删除节点
     * @param clientList
     * @return
     * @throws Exception
     */
    public int tagClientToDel(List<Client> clientList) throws Exception;

    /**
     * 获取实例下的节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Client> getOnlineClientsByIns(String insKey) throws Exception;

    /**
     * 获取实例下的节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByIns(String insKey) throws Exception;

    /**
     * 获取实例下的节点
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByInsKeyList(List<String> insKeyList) throws Exception;

    /**
     * 批量删除节点
     * @param clientList
     * @return
     * @throws Exception
     */
    public int deleteByUniqkey(List<Client> clientList) throws Exception;

    /**
     * 批量删除节点
     * @param clientList
     * @return
     * @throws Exception
     */
    public int deleteById(List<Client> clientList) throws Exception;

    /**
     * 获取来源是registry的无实例client列表，并获取小于date时间的数据
     * @return
     * @throws Exception
     */
    public List<Client> getNoInsClients(long optTime) throws Exception;

    /**
     * 将心跳正常但是已经逻辑删除的consumer复活
     * @param insList
     * @return
     * @throws Exception
     */
    public int updateClientToRevival(List<JsfIns> insList) throws Exception;

}