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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.JsfAlarmHistory;

public interface AlarmManager {
    /**
     * 批量插入
     * @param list
     * @throws Exception
     */
    public void batchInsert(List<JsfAlarmHistory> list) throws Exception;

    /**
     * 单条插入
     * @param record
     * @return
     */
    public int insert(JsfAlarmHistory record);

    /**
     * 获取报警历史记录
     * @param createTime
     * @param alarmType
     * @param extendKey1
     * @return
     */
    public List<JsfAlarmHistory> getAlarmHistoryList(Date createTime, int alarmType);
}
