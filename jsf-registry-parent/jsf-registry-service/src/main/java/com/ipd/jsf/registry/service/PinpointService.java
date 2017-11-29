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

import java.util.List;
import java.util.Map;

import com.ipd.jsf.service.vo.PCNodeInfo;
import com.ipd.jsf.vo.SubscribeUrl;

public interface PinpointService {

    /**
     * 定向推送服务列表
     * 如果是定向某个接口，则将最新的服务列表推送给这个接口的这些订阅者；
     * 如果更细的定向某个接口的订阅者，则将最新的服务列表推送给这些订阅者。
     * @param interfaceName
     * @param insKeyList
     * @return
     */
    public boolean pinpointCallbackProvider(String interfaceName, List<String> insKeyList);

    /**
     * 定向推送接口配置
     * @param interfaceName
     * @param insKeyList
     * @return
     */
    public boolean pinpointCallbackConfig(String interfaceName, List<String> insKeyList);

    /**
     * 定向推送接口参数, 下发参数
     * @param interfaceName
     * @param insKey
     * @param attribute
     * @return
     */
    public boolean pinpointCallbackInterfaceConfig(String interfaceName, List<String> insKey, Map<String, String> attribute, boolean isProvider);
    
    /**
     * 定向推送接口参数, 下发参数, 用于切换alias
     * @param interfaceName
     * @param pcNodeInfoList
     * @param attribute
     * @param isProvider
     * @return
     */
    public boolean pinpointCallbackInterfaceConfigExt(String interfaceName, List<PCNodeInfo> pcNodeInfoList, Map<String, String> attribute, boolean isProvider);

    /**
     * 获取consumer配置信息
     * type=1, 配置信息
     * type=2，provider列表
     * type=3, 获取实例级别的数据
     * @param interfaceName
     * @param insKey
     * @param type
     * @return
     */
    public String consumerConfig(String interfaceName, String alias, int protocol, String insKey, byte type);

    /**
     * 指定InstanceKey自定义Callback
     *
     * @param insKey
     *         InstanceKey
     * @param subscribeUrl
     *         自定义对象
     * @return callback返回值
     * @throws Exception 
     */
    public String customCallback(String insKey, SubscribeUrl subscribeUrl) throws Exception;
}
