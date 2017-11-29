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

public interface InterfaceVisitService {
    /**
     * 检查接口的visitor是否授权
     * @param interfaceName
     * @param visitorName
     * @return
     */
    public boolean check(String interfaceName, String visitorName);

    /**
     * 刷新缓存, 加载接口授权访问者缓存(用于多语言接入)
     * @throws Exception
     */
    public void refreshCache() throws Exception;
}
