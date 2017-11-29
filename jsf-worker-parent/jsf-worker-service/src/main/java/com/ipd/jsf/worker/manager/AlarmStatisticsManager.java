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
package com.ipd.jsf.worker.manager;

import com.ipd.jsf.worker.domain.JsfAlarmStatistics;

import java.util.List;

public interface AlarmStatisticsManager {

    /**
     * 单条插入
     * @param record
     * @return
     */
    public int insert(JsfAlarmStatistics record);


    /**
     * 批量插入
     * @param list
     * @throws Exception
     */
    public void batchInsert(List<JsfAlarmStatistics> list) throws Exception;


    /**
     * 按照类型进行分组
     * 根据某一天的时间查询
     * 参数:查询条件,指定的某一天
     * 返回:对象列表
     */
    //public List<JsfAlarmStatisticsItem> getOneDayStatistics(String date);


    /**
     * 按照类型，接口，ip进行分组
     * 根据某一天的时间查询
     * 参数:查询条件,指定的某一天
     * 返回:对象列表
     */
    public List<JsfAlarmStatistics> selectOneDayStatistics(String date);

    /**
     * 根据某一天的时间查询數據庫表中是否存在該條記錄
     * 参数:查询条件,指定的某一天
     * 返回:查詢到的記錄數
     */
    public int isExists(String date);


    //删除某一天的记录
    void deleteOneDay(String date) throws Exception;

}
