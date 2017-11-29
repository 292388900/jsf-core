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

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 更新接口数据版本
 */
@Repository
public interface InterfaceDataVersionDao  {
    public int create(@Param("interfaceId") int interfaceId, @Param("updateTime") Date updateTime) throws Exception;
    public int update(@Param("list") List<Integer> interfaceIdList, @Param("updateTime") Date updateTime) throws Exception;
    public List<Integer> getListByTime(@Param("updateTime") Date updateTime) throws Exception;

    //更新接口的配置的时间戳，没有就插入
    public int createOrUpdateCfgTime(@Param("interfaceId") int interfaceId) throws Exception;
}
