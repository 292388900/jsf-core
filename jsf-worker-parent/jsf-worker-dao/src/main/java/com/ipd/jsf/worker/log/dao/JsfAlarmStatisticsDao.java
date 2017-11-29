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

import com.ipd.jsf.worker.domain.JsfAlarmStatistics;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsfAlarmStatisticsDao {

    /**
     * 参数:JsfAlarmStatistics对象
     */
    int insert(JsfAlarmStatistics record);

    /**
     * 批量插入记录
     * @param list
     * @return
     * @throws Exception
     */
    public int batchInsert(@Param("list") List<JsfAlarmStatistics> list) throws Exception;

    /**
     * 根据某一天的时间查询
     * 参数:查询条件,指定的某一天,这一天的下一天
     * 返回:对象列表
     */
    //List<JsfAlarmStatisticsItem> selectOneDay(@Param(value="date")String date, @Param(value="theNextDay")String theNextDay);

    List<JsfAlarmStatistics> selectOneDayStatistics(@Param(value="date")String date, @Param(value="theNextDay")String theNextDay);

    //查询某一天的数据，通过数据量来判断统计数据是否存在
    int isExists(String date);

    //删除某一天的记录
    int deleteOneDay(String date) throws Exception;
}