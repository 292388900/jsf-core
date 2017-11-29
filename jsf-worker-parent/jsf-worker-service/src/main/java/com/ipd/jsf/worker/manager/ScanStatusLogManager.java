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

import com.ipd.jsf.worker.domain.ScanStatusLog;

public interface ScanStatusLogManager {
    /**
     * 获取time以前的数据, 操作saf21库
     * @param time
     * @return
     * @throws Exception
     */
    public List<ScanStatusLog> getListBeforeTime(Date time) throws Exception;

    /**
     * 插入到备份表中, 操作saf_registry库
     * @param list
     * @return
     * @throws Exception
     */
    public int insertToHistory(List<ScanStatusLog> list) throws Exception;

    /**
     * 删除time以前的数据, 操作saf21库
     * @param time
     * @return
     * @throws Exception
     */
    public int batchDelete(Date time) throws Exception;
}
