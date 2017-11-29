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

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.JsfAlarmHistory;

@Repository
public interface JsfAlarmHistoryDao {

    /**
     * 插入，空属性也会插入
     * 参数:pojo对象
     * 返回:删除个数
     * @ibatorgenerated 2014-08-14 17:59:27
     */
    int insert(JsfAlarmHistory record);

    /**
     * 批量插入记录
     * @param list
     * @return
     * @throws Exception
     */
    public int batchInsert(@Param("list") List<JsfAlarmHistory> list) throws Exception;
    
    /**
     * 根据最后更新时间查询
     * 参数:查询条件,上次查询时间
     * 返回:对象列表
     * @ibatorgenerated 2014-08-14 17:59:27
     */
    JsfAlarmHistory selectById(Integer id);
    
    
    JsfAlarmHistory getNewLastTime();
    
    /**
     * 根据最后更新时间查询
     * 参数:查询条件,上次查询时间
     * 返回:对象列表
     * @ibatorgenerated 2014-08-14 17:59:27
     */
    List<JsfAlarmHistory> selectByTime(@Param("createTime")Date createTime);
    
    /**
     * 根据时间段和报警key、接口名、方法名、报警类型统计个数
     * 参数:查询条件,时间段、是否报警
     * 返回:对象列表
     * @ibatorgenerated 2014-08-14 17:59:27
     */
    List<JsfAlarmHistory> countByTime(@Param("createTime")Date lastTime, @Param("isAlarmed")int alarmed);

    /**
     * 根据主键修改，空值条件会修改成null
     * 参数:1.要修改成的值
     * 返回:成功修改个数
     * @ibatorgenerated 2014-08-14 17:59:27
     */
    int updateByPrimaryKey(@Param("id")Integer id, @Param("alarmTime")Date alarmTime, @Param("remarks")String remarks,
    		@Param("isAlarmed") int isAlarmed);

    /**
     * 获取无实例provider报警
     * @param createTime
     * @param alarmType
     * @param extendKey1
     * @return
     */
    public List<JsfAlarmHistory> getAlarmHistoryList(@Param("createTime")Date createTime, @Param("alarmType")int alarmType);
    
    public int getListByTime(@Param("startTime")Date startTime, @Param("endTime")Date endTime, @Param("alarmType")int alarmType);
}