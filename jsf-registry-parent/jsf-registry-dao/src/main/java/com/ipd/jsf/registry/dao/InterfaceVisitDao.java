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

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.registry.domain.InterfaceVisit;

/**
 * 接口与app关系对象
 */
@Repository
public interface InterfaceVisitDao {
    /**
     * 查找变化的接口与app关系记录
     * @return
     * @throws Exception
     */
    public List<InterfaceVisit> getList(@Param("start") int start, @Param("limit") int limit) throws Exception;

    public int getListCount() throws Exception;
}
