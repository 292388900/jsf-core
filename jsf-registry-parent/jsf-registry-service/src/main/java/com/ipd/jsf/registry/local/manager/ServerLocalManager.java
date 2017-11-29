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
package com.ipd.jsf.registry.local.manager;

import java.util.List;

import com.ipd.jsf.registry.berkeley.domain.BdbServerList;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;

public interface ServerLocalManager {

    /**
     * 保存server信息
     * @param server
     * @return
     * @throws Exception
     */
    public void registerServer(Server server, JsfIns ins) throws Exception;
    
    /**
     * 保存server信息
     * @param server
     * @return
     * @throws Exception
     */
    public void registerServer(List<Server> serverList, JsfIns ins) throws Exception;

    /**
     * 更新状态，用于unregistry
     * @param server
     * @throws Exception
     */
    public void unRegisterServer(Server server, JsfIns ins) throws Exception;
    
    /**
     * 更新状态，用于unregistry
     * @param serverList
     * @throws Exception
     */
    public void unRegisterServer(List<Server> serverList, JsfIns ins) throws Exception;

    /**
     * 获取没有同步的server列表 
     * @param interfaceIdList
     * @return
     * @throws Exception
     */
    public List<BdbServerList> getServers() throws Exception;

    /**
     * 删除数据, 将已经同步的，而且更新时间大于1天的数据删除
     * @return
     * @throws Exception
     */
    public void delete(String key) throws Exception;

    /**
     * 获取总记录数
     * @return
     * @throws Exception
     */
    public int getTotalCount() throws Exception;

    /**
     * 同步刷新到磁盘
     */
    public void flush();
}
