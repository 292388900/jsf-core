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

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 注册中心心跳并测试db连接dao
 */
@Repository
public interface RegHbDao {

    public int insert(@Param("regIp") String regIp, @Param("hbTime") long hbTime) throws Exception;

    public int insertHealth(@Param("regAddr") String regIp, @Param("hbTime") long hbTime) throws Exception;

    public void delHealthHistory(@Param("regAddr") String regIp, @Param("period") long period) throws Exception;

    public int update(@Param("regIp") String regIp, @Param("hbTime") long hbTime) throws Exception;

}