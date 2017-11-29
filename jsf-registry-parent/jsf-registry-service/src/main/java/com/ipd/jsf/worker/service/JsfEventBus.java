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
package com.ipd.jsf.worker.service;

import java.util.List;

import com.ipd.jsf.worker.service.vo.CliEvent;
import com.ipd.jsf.worker.service.vo.RegistryInfo;
import com.ipd.jsf.gd.transport.Callback;

public interface JsfEventBus {

    /**
     * 注册中心注册到worker的方法
     * @param info
     * @param stub
     */
    void register(RegistryInfo info, Callback<List<CliEvent>, String> stub);

    /**
     * 注册中心取消注册到worker的方法
     * @param info
     */
    void unregister(RegistryInfo info);

    /**
     * 收集事件
     * @param event
     */
    void collect(CliEvent event);

    /**
     * 批量收集事件
     * @param event
     */
    void collectList(List<CliEvent> eventList);

}
