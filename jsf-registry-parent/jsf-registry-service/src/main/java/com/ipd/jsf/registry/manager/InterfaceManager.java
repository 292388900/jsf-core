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
package com.ipd.jsf.registry.manager;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.InterfaceInfo;

public interface InterfaceManager {
    /**
     * 根据接口名获取接口ID
     * @param ifaceName
     * @return
     * @throws Exception
     */
    public InterfaceInfo getByName(String ifaceName) throws Exception;

    /**
     * 根据接口id获取接口信息,此方法只用于刷新provider，不加载cfg_update_time
     * @param ifaceIdList
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getByIdListForProvider(List<Integer> ifaceIdList) throws Exception;
    
    /**
     * 根据接口id获取接口信息, 此方法只用于刷新config，不加载update_time
     * @param ifaceIdList
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getByIdListForConfig(List<Integer> ifaceIdList) throws Exception;
    
    /**
     * 取接口数据，此方法只用于刷新provider，不加载cfg_update_time
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getListForProvider() throws Exception;

    /**
     * 根据provider更新时间，获取provider变化的接口
     * @param lastTime
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getChangeListByUpdateTime(Date lastTime) throws Exception;

    /**
     * 根据接口配置更新时间，获取配置变化的接口
     * @param lastTime
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getChangeListByConfigUpdateTime(Date lastTime) throws Exception;

    /**
     * 获取删除后的记录
     * @return
     * @throws Exception
     */
    public Set<String> getInvalidList() throws Exception;

    /**
     * 获取所有接口信息, 包括接口+alias的版本信息
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getAllListForProvider() throws Exception;

    /**
     * 根据接口+alias更新时间，获取provider变化的接口信息
     * @param lastTime
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getChangeListByUpdateTimeForProvider(long lastTime) throws Exception;

    /**
     * 根据接口id获取接口信息,此方法只用于刷新provider，不加载cfg_update_time
     * @param ifaceIdAliasList
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getChangeListByIfaceIdAliasForProvider(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception;

    /**
     * 根据接口+alias转换为接口信息，不加载数据库，只是做下转换
     * @param ifaceIdAliasList
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getInterfaceListFromVersionList(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception;
    
}
