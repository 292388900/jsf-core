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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.worker.service.vo.CliEvent;
import com.ipd.jsf.worker.service.vo.RegistryInfo;
import com.ipd.jsf.worker.service.vo.CliEvent.NotifyType;
import com.ipd.jsf.gd.transport.Callback;

public interface EventSynService {

    /**
     * 注册中心注册到worker的方法
     * @param info
     * @param stub
     */
    public void register(RegistryInfo info, Callback<List<CliEvent>, String> stub) throws Exception;

    /**
     * 注册中心取消注册到worker的方法
     * @param info
     */
    public void unregister(RegistryInfo info) throws Exception;

    /**
     * 采集实例连接断开的事件
     * @param ins
     */
    public void eventCollectInsDisconnect(JsfIns ins) throws Exception;

    /**
     * 采集节点(provider)注册事件
     * @param serverList
     * @param updateVersion
     * @param isNotifyList
     * @throws Exception
     */
    public void eventCollectServerRegister(List<Server> serverList, Date updateVersion, List<NotifyType> isNotifyList) throws Exception;

    /**
     * 采集节点(consumer)注册事件
     * @param clientList
     * @throws Exception
     */
    public void eventCollectClientRegister(List<Client> clientList) throws Exception;

    /**
     * 采集节点(provider)取消注册事件
     * @param serverList
     * @param updateVersion
     * @param isNotifyList
     * @throws Exception
     */
    public void eventCollectServerUnRegister(List<Server> serverList, Date updateVersion, List<NotifyType> isNotifyList) throws Exception;
    
    /**
     * 采集节点(consumer)取消注册事件
     * @param clientList
     * @throws Exception
     */
    public void eventCollectClientUnRegister(List<Client> clientList) throws Exception;
}
