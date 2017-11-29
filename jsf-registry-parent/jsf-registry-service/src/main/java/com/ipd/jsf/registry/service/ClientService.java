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
package com.ipd.jsf.registry.service;

import java.util.List;

import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;

public interface ClientService {

    /**
     * 注册consumer
     * @param client
     * @throws Exception
     */
    public void registerClient(Client client, JsfIns ins) throws Exception;

    /**
     * 批量注册consumer
     * @param clientList
     * @param ins
     * @throws Exception
     */
    public void registerClient(List<Client> clientList, JsfIns ins) throws Exception;

    /**
     * 保存consumer信息,双写
     * @param clientList
     * @param ins
     * @throws Exception
     */
    public void saveClient(List<Client> clientList, JsfIns ins) throws Exception;

    /**
     * 取消注册consumer
     * @param client
     * @throws Exception
     */
    public void unRegisterClient(Client client, JsfIns ins) throws Exception;

    /**
     * 批量取消注册consumer
     * @param client
     * @throws Exception
     */
    public void unRegisterClient(List<Client> clientList, JsfIns ins) throws Exception;

    /**
     * 批量删除consumer信息,双写
     * @param clientList
     * @param ins
     * @throws Exception
     */
    public void removeClient(List<Client> clientList, JsfIns ins) throws Exception;
}
