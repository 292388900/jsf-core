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

import com.ipd.jsf.worker.domain.ScanStatusLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ScanStatusLogDao {
    /**
     * 创建日志
     * @param list
     * @return
     */
    public int create(@Param("list") List<ScanStatusLog> list) throws Exception;

    /**
     * 删除日志
     * @param time
     * @return
     * @throws Exception
     */
    public int deleteBeforeTime(@Param("time") Date time) throws Exception;
    
    /**
     * 获取某个时间点以前的数据
     * @param time
     * @return
     */
    public List<ScanStatusLog> getList(@Param("time") Date time) throws Exception;
}
