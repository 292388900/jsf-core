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

package com.ipd.jsf.service;

import java.util.List;
import java.util.Map;

import com.ipd.jsf.service.vo.PCNodeInfo;
import com.ipd.jsf.vo.SubscribeUrl;

public interface RegistryCtrlService {
    /**
     * 强制刷新缓存（包括服务列表和配置）
     * 如果指定了接口，则按照接口刷；
     * 如果没有指定接口，则刷新全部缓存。
     * param
     * @param interfaceName
     * @return
     */
    public boolean refreshCache(String interfaceName);

    /**
     * 定向推送服务列表
     * 如果是定向某个接口，则将最新的服务列表推送给这个接口的这些订阅者；
     * 如果更细的定向某个接口的订阅者，则将最新的服务列表推送给这些订阅者。
     * @param interfaceName
     * @param insKey
     * @return
     */
    public boolean pinpointCallbackProvider(String interfaceName, List<String> insKeyList);
    
    /**
     * 定向推送配置
     * @param interfaceName
     * @param insKey
     * @return
     */
    public boolean pinpointCallbackConfig(String interfaceName, List<String> insKeyList);
    
    /**
     * 定向推送实例属性配置
     * @param interfaceName
     * @param insKey
     * @param attribute
     * @param isProvider
     * @return
     */
    public boolean pinpointCallbackInterfaceConfig(String interfaceName, List<String> insKey, Map<String, String> attribute, boolean isProvider);
    
    /**
     * 定向推送实例属性配置, 适用于切换alias
     * @param interfaceName
     * @param pcNodeInfoList
     * @param attribute
     * @param isProvider
     * @return
     */
    public boolean pinpointCallbackInterfaceConfigExt(String interfaceName, List<PCNodeInfo> pcNodeInfoList, Map<String, String> attribute, boolean isProvider);

    /**
     * 获取consumer的相关信息
     * 1. 获取consumer配置
     * 2. 获取consumer的服务列表
     * @param interfaceName
     * @param alias
     * @param protocol
     * @param insKey
     * @param type  1-获取consumer配置, 2-获取consumer的服务列表
     * @return
     */
    public String consumerConfig(String interfaceName, String alias, int protocol, String insKey, byte type);

    /**
     * 给特定的interface添加一个provider
     * 仅简易管理端用
     * 简易管理端中添加provider及上下线使用
     * @param interfaceName
     * @param provider
     * @param type  0=delete  1=add
     * @return
     */
    public boolean updateProvider(String interfaceName, Map<String, String> provider, int type);

    /**
     * 修改注射中心中接口黑白名单
     * 只给简易管理端使用
     * 只能对白或黑名单进行单次的操作：如增加白名单
     * @param interfaceName
     * @param bwList key:white value=1.1.1.1
     * @param type  1=add  2 = del
     * @return
     */
    public boolean updateInterfaceWBList(String interfaceName, Map<String, String> bwList, int type);

    /**
     * 实例控制
     * 包括： 1-recover(重新注册)
     *       2-reconnect(强制重连其它注册中心并强制重新订阅（包括provider和consumer等）)
     *       3-reset remote client schedule(重新设置远程客户端的订阅心跳定时器)
     * @param insKey
     * @param type  1-recover, 2-reconnect, 3-resetschedule
     * @return
     */
    public boolean insCtrl(String insKey, byte type);

    /**
     * 指定InstanceKey自定义Callback
     *
     * @param insKey
     *         InstanceKey
     * @param subscribeUrl
     *         自定义对象
     * @return callback返回值
     */
    public String customCallback(String insKey, SubscribeUrl subscribeUrl) throws Exception;

    public String resetDruidDS() throws Exception;
}