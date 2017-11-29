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
package com.ipd.jsf.sqllite.test.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.sqllite.test.domain.Server;


/**
 * 操作server表
 */
@Repository
public interface ServerDao {
    public void create() throws Exception;
    
    public int insert(Server obj) throws Exception;

    public int update(Server obj) throws Exception;

    public int updateStatus(@Param("uniqKey") String uniqKey, @Param("updateTime") Date updateTime) throws Exception;

    public List<Server> getServersByInterfaceIdList(@Param("list") List<Integer> interfaceIdList) throws Exception;

    public List<Server> getAliasServerByInterfaceIdList(@Param("list") List<Integer> interfaceIdList) throws Exception;
}
