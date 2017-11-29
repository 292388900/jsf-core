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
package com.ipd.jsf.registry.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.registry.domain.InterfaceInfo;

/**
 * 获取接口信息
 */
@Repository
public interface InterfaceInfoDao  {

    public List<InterfaceInfo> getListForProvider() throws Exception;

    public List<InterfaceInfo> getChangeListByUpdateTime(@Param("time") Date time) throws Exception;

    public List<InterfaceInfo> getChangeListByConfigUpdateTime(@Param("time") Date time) throws Exception;

    public InterfaceInfo getByInterfaceName(@Param("interfaceName") String interfaceName) throws Exception;

    public List<InterfaceInfo> getByIdListForProvider(@Param("list") List<Integer> list) throws Exception;

    public List<InterfaceInfo> getByIdListForConfig(@Param("list") List<Integer> list) throws Exception;

    public List<InterfaceInfo> getInvalidList() throws Exception;

    public List<InterfaceInfo> getAllValidList() throws Exception;

    public List<InterfaceInfo> getListByIds(@Param("ids") List<Integer> list) throws Exception;
}
