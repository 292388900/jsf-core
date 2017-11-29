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
package com.ipd.jsf.registry.service;

import com.ipd.jsf.registry.domain.JsfIns;

/**
 * 心跳操作
 */
public interface HeartbeatService {

    /**
     * 保存实例信息mysql中
     * @param ins
     * @throws Exception
     */
    public void register(JsfIns ins) throws Exception;

    /**
     * 保存心跳信息到内存中
     * @param insKey
     * @return
     * @throws Exception
     */
    public boolean putHbCache(String insKey) throws Exception;

    /**
     * 保存心跳信息
     * @param regIp
     * @throws Exception
     */
    public void saveHb(String regIp) throws Exception;

    /**
     * 检查实例是否存在心跳
     * @param insKey
     * @return
     */
    public boolean isExist(String insKey);
}
