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

import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;

public interface ServerService {

    /**
     * 注册provider
     * @param server
     * @throws Exception
     */
    public void registerServer(Server server, JsfIns ins) throws Exception;
    
    /**
     * 注册provider
     * @param server
     * @throws Exception
     */
    public void registerServer(List<Server> serverList, JsfIns ins) throws Exception;

    /**
     * 保存server信息,双写
     * @param serverList
     * @param ins
     * @throws Exception
     */
    public void saveServer(List<Server> serverList, JsfIns ins) throws Exception;

    /**
     * 取消注册provider
     * @param server
     * @throws Exception
     */
    public void unRegisterServer(Server server, JsfIns ins) throws Exception;

    /**
     * 取消注册provider
     * @param server
     * @throws Exception
     */
    public void unRegisterServer(List<Server> serverList, JsfIns ins) throws Exception;
    
    /**
     * 批量删除server信息,双写
     * @param serverList
     * @param ins
     * @throws Exception
     */
    public void removeServer(List<Server> serverList, JsfIns ins) throws Exception;
}
