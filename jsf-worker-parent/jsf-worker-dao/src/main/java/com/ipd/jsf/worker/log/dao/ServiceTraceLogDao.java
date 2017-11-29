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
package com.ipd.jsf.worker.log.dao;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.ServiceTraceLog;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceTraceLogDao {
    /**
     * 创建ServiceTraceLog
     * @param list
     * @return
     * @throws Exception
     */
    public int create(@Param("list") List<ServiceTraceLog> list) throws Exception;

    /**
     * 按时间获取列表
     * @param interfaceName
     * @param pcType
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public List<ServiceTraceLog> getListByTime(@Param("interfaceName") String interfaceName, @Param("pcType") int pcType, @Param("startTime") Date startTime, @Param("endTime") Date endTime) throws Exception;

    /**
     * 删除记录
     * @param time
     * @return
     * @throws Exception
     */
    public int deleteByTime(Date time) throws Exception;


    /**
     * 统计时间段之内的上下线记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    Collection<ServiceTraceLog> statistics(@Param(value="startTime")String startTime, @Param(value="endTime")String endTime, @Param(value="offset")int offset, @Param(value="size")int size);

    /**
     * 上下线记录次数最大的
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    int getMaxCount(@Param(value="startTime")String startTime, @Param(value="endTime")String endTime);


    /**
     * 统计时间段之内的上下线记录数大于某一值的记录
     *
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     */
    Collection<ServiceTraceLog> getTopRecords(@Param(value="startTime")String startTime, @Param(value="endTime")String endTime, @Param(value="maxCount")int maxCount);


}
