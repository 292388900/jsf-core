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

import com.ipd.jsf.registry.callback.SubscribeCallback;
import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

public interface SubscribeService {
    /**
     * 订阅provider
     * @param ifaceName
     * @param alias
     * @param protocolType
     * @param jsfVersion
     * @param serialization
     * @param insKey
     * @param clientIp
     * @param callback
     * @param appId
     * @return
     * @throws Exception
     */
    public List<JsfUrl> subscribe(String ifaceName, String alias, int protocolType, String jsfVersion, String serialization, String insKey, String clientIp, SubscribeCallback<SubscribeUrl> callback, int appId) throws Exception;

    /**
     * 取消订阅，将callback移除
     * @param ifaceName
     * @param insKey
     * @throws Exception
     */
    public void unSubscribe(String ifaceName, String insKey) throws Exception;

    /**
     * 刷新缓存
     * @throws Exception
     */
    public void refreshCache(int loadInterval) throws Exception;

    /**
     * 远程重启客户端的心跳、订阅的schedule(如果客户端的时间被改小，schedule执行会推迟。linux环境下会存在这种问题 )
     * @param insKey
     * @return
     */
    public boolean resetRemoteClientSchedule(String insKey);

    /**
     * 删除实例和callback
     * @param insKey
     * @param isForce
     */
    public void removeInstanceCache(String insKey);

    /**
     * 获取接口配置信息
     * @param interfaceName
     * @param insKey
     * @param callback
     * @param clientIp
     * @return
     * @throws Exception
     */
    public Map<String, String> getInterfaceProperty(String interfaceName, String insKey, SubscribeCallback<SubscribeUrl> callback, String clientIp) throws Exception;

    /**
     * 根据接口信息，强制刷新接口下的服务节点
     * @param list
     * @throws Exception
     */
    public boolean forceReloadProvider(List<Integer> list, boolean isAsyn) throws Exception;

    /**
     * 根据接口+alias的version加载服务节点
     * @param list
     * @return
     * @throws Exception
     */
    public boolean putIfaceAliasToQueue(List<IfaceAliasVersion> list) throws Exception;

    /**
     * 强制刷新所有接口下的服务节点
     * @param list
     * @throws Exception
     */
    public void forceReloadAllProvider() throws Exception;

    /**
     * 根据接口信息，强制刷新接口下的配置
     * @param list
     * @throws Exception
     */
    public boolean forceReloadInterfaceConfig(List<Integer> list, boolean isAsyn) throws Exception;

    /**
     * 强制刷新所有接口下的配置
     * @param list
     * @throws Exception
     */
    public void forceReloadAllInterfaceConfig() throws Exception;

    /**
     * 获取接口信息
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public InterfaceInfo getByName(String interfaceName) throws Exception;

    /**
     * 获取callback信息
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public String getCallbackInfo(String interfaceName);

    /**
     * 取消注册consumer时，需要将instanceCache中对应consumer信息删除
     * @param ifaceName
     * @param alias
     * @param protocol
     * @param insKey
     */
    public void unRegistryConsumer(String ifaceName, String alias, int protocol, String insKey);

    /**
     * 获取全局配置信息
     * @param insKey
     * @param callback
     * @return
     * @throws Exception
     */
    public Map<String, String> getConfig(String insKey, SubscribeCallback<SubscribeUrl> callback) throws Exception;

    /**
     * 获取全局配置版本号
     * @param attrs
     * @return
     */
    public long getGlobalConfigDataVersion(Map<String, String> attrs);

    /**
     * 给特定的interface添加一个provider
     * 仅简易管理端用
     * 简易管理端中添加provider及上下线使用
     * 凡是通过该接口做update操作，说明数据库此时已经不可用；
     * 设置interfaceLoadLastTime 属性为1 ， 表示如果数据库可用，需要重新加载所有数据,以DB为准，擦除简易管理端的设置
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
     * 获取注册中心内存中的接口配置
     * @param interfaceName
     * @return
     */
    public Map<String, String> getInterfaceConfig(String interfaceName);

    /**
     * 检查callback
     * true：callback存在
     * false：callback不存在
     * @param insKey
     * @param checkNotify
     * @return
     */
    public boolean checkCallback(String insKey, boolean checkNotify);

    /**
     * 实例控制
     * 包括：recover(重新注册), reconnect(强制重连其它注册中心并强制重新订阅（包括provider和consumer等）)
     * @param insKey
     * @param type
     * @return
     */
    public boolean insCtrl(String insKey, byte type);
}
