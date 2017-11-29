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

import java.util.List;

import com.ipd.jsf.worker.domain.RegHealthInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * 注册中心心跳并测试db连接dao
 */
@Repository
public interface RegHbDao {

    public List<String> getUncheckList(@Param("time") long hbTime) throws Exception;

    public List<RegHealthInfo> getUncheckRegList(@Param("startTime") long startHbTime, @Param("endTime") long endHbTime) throws Exception;

    public RegHealthInfo getLatestUncheckRegList(@Param("startTime") long startHbTime, @Param("endTime") long endHbTime, @Param("regAddr") String regAddr) throws Exception;

    public void delHealthHistory(@Param("regAddr") String regAddr, @Param("period") long period) throws Exception;

}