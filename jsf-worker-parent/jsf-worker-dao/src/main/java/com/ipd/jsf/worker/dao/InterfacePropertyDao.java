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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.InterfaceProperty;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InterfacePropertyDao {
    /**
     * 获取所有接口的worker配置
     * @param paramType
     * @return
     * @throws Exception
     */
    public List<InterfaceProperty> getList(@Param("paramType") int paramType) throws Exception;


    //获取某个接口的服务端限流配置
    public InterfaceProperty getCallLimitProperty(@Param("interfaceName") String interfaceName,@Param("paramKey") String paramKey) throws Exception;

    //更新某个接口的服务端限流配置
    //public void updateCallLimitProperty(@Param("interfaceName") String interfaceName,@Param("value") String value,@Param("date") Date date) throws Exception;
    public void updateCallLimitProperty(InterfaceProperty interfaceProperty) throws Exception;

    //增加某个接口的服务端限流配置
    //这个id没什么用，会自动生成。但是不加的话，mybatis会抛异常出来
    public void addCallLimitProperty(@Param("id") int id,@Param("interfaceId") int interfaceId,@Param("interfaceName") String interfaceName,@Param("paramValue") String paramValue,@Param("updateTime") Date updateTime,@Param("paramKey") String paramKey) throws Exception;

    //删除某个接口的服务端限流配置
    public void deleteCallLimitProperty(String interfaceName,@Param("paramKey") String paramKey) throws Exception;

}
