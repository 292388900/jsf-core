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
package com.ipd.jsf.registry.manager;

import java.util.Date;
import java.util.List;

import com.ipd.jsf.registry.domain.JsfIns;

public interface SafInsManager {
    /**
     * 新增实例。如果重复，用ON DUPLICATE KEY UPDATE修改心跳时间
     * @param ins
     * @return
     * @throws Exception
     */
    public int register(JsfIns ins) throws Exception;

    /**
     * 将内存中的心跳信息批量更新到数据库。用于定时任务
     * @param insKeyList
     * @param hbTime
     * @param regIp
     * @throws Exception
     */
    public int saveHb(List<String> insKeyList, Date hbTime, String regIp) throws Exception;

    /**
     * 根据inskey获取inskey，主要用来判断实例是否存在
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<String> getInsKeyListByInsKey(List<String> insKeyList) throws Exception;
}
