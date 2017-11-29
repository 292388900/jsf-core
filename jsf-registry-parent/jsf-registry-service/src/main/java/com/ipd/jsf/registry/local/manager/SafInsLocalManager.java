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
package com.ipd.jsf.registry.local.manager;

import java.util.List;

import com.ipd.jsf.registry.berkeley.domain.BdbJsfIns;
import com.ipd.jsf.registry.domain.JsfIns;

public interface SafInsLocalManager {

    /**
     * 保存ins信息
     * @param ins
     * @return
     * @throws Exception
     */
    public void register(JsfIns ins) throws Exception;

    /**
     * 获取没有同步的ins列表 
     * @param interfaceIdList
     * @return
     * @throws Exception
     */
    public List<BdbJsfIns> getJsfInsList() throws Exception;

    /**
     * 删除已经同步的数据
     * @return
     * @throws Exception
     */
    public void delete(String key) throws Exception;

    /**
     * 获取总记录数
     * @return
     * @throws Exception
     */
    public int getTotalCount() throws Exception;

    /**
     * 同步刷新到磁盘
     */
    public void flush();
}
