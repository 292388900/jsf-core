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

package com.ipd.jsf.service;

import com.ipd.jsf.service.vo.Instance;
import com.ipd.jsf.service.vo.InstanceResponse;
import com.ipd.jsf.service.vo.InterfaceInfoVo;
import com.ipd.jsf.service.vo.Paging;

/**
 * 查看注册中心缓存信息
 */
public interface RegistryQueryService {
    /**
     * 获取接口的provider列表, 可分页，支持interfaceName, ip, alias, protocol条件查询
     * @return
     */
    public InterfaceInfoVo getProviders(Paging page) throws Exception;

    /**
     * 获取接口的provider列表, 可分页，支持interfaceName, ip, alias, protocol条件查询
     * 仅给简易管理端使用，数据只来源于内存
     * @return
     */
    public InterfaceInfoVo getProviders4Simple(Paging page) throws Exception;

    /**
     * 获取实例列表，可分页，支持insKey 模糊查询
     * @return
     */
    public InstanceResponse getInstanceList(Paging page) throws Exception;
    
    /**
     * 获取实例信息
     * @return
     * @throws Exception
     */
    public Instance getInstance(String insKey) throws Exception;
}
