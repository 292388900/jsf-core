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

import com.ipd.jsf.worker.domain.ServiceTraceLog;

public interface ServiceTraceLogManager {
    /**
     * 批量创建
     * @param list
     * @throws Exception
     */
    public void create(List<ServiceTraceLog> list) throws Exception;

    /**
     * 按时间获取列表
     * @param interfaceName
     * @param pcType
     * @param startTime
     * @param endTime
     * @return
     * @throws Exception
     */
    public List<ServiceTraceLog> getListByTime(String interfaceName, int pcType, Date startTime, Date endTime) throws Exception;

    /**
     * 删除记录
     * @param time
     * @return
     * @throws Exception
     */
    public int deleteByTime(Date time) throws Exception;
}
