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

import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.JsfAlarmSetting;

@Repository
public interface JsfAlarmSettingDao {

    /**
     * 插入，空属性也会插入
     * 参数:pojo对象
     * 返回:删除个数
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    int insert(JsfAlarmSetting record);

    /**
     * 根据更新时间查询
     * 参数:查询条件,更新时间
     * 返回:对象
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    List<JsfAlarmSetting> selectByTime(Date updateTime);
    
    /**
     * 根据主键查询
     * 参数:查询条件,主键值
     * 返回:对象
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    JsfAlarmSetting selectByPk(Integer id);

    /**
     * 根据主键修改，空值条件会修改成null
     * 参数:1.要修改成的值
     * 返回:成功修改个数
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    int updateByPrimaryKey(JsfAlarmSetting record);
}