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

import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.vo.HbResult;
import com.ipd.jsf.vo.Heartbeat;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.LookupParam;
import com.ipd.jsf.vo.LookupResult;

/**
 * 注册中心对外发布服务接口
 */
public interface RegistryHttpService {

    /**
     * provider/consumer注册<br>
     * 处理契约：<br>
     * 1. JsfUrl的ip,port,pid,iface,alias,protocol(参考ProtocolType枚举),timeout,stTime为必填项，这些是provider/consumer端的配置信息<br>
     * 2. JsfUrl中的attrs应该设置apppath=客户端的应用路径, safVersion=210, jsfVersion=1000, serialization=msgpack(参考CodecType枚举)<br>
     * 3. 如果为provider时，当JsfUrl中的attrs设置了check=false 且是第一次上线(即 re-reg不为true时)，则将状态设置为下线，用做灰度上线<br>
     * 4. 如果为provider时，JsfUrl中的attrs设置weight=provider权重值<br>
     * 5. 如果为consumer时，JsfUrl中的attrs应该设置consumer=1<br>
     * 6. 返回结果中，会在JsfUrl中给出insKey值<br>
     * @param jsfUrl
     * @return
     * @throws Exception
     */
    public JsfUrl doRegister(JsfUrl jsfUrl) throws RpcException;

    /**
     * provider/consumer取消注册<br>
     * 处理契约：<br>
     * 1. JsfUrl的ip,port,pid,iface,alias,protocol(参考ProtocolType枚举),insKey为必填项，这些是provider/consumer端的配置信息<br>
     * 2. 如果为consumer时，JsfUrl中的attrs应该设置consumer=1<br>
     * 3. 返回为true成功，false失败<br>
     * @param jsfUrl
     * @return
     * @throws Exception
     */
    public boolean doUnRegister(JsfUrl jsfUrl) throws RpcException;
    /**
     * 实例心跳<br>
     * 处理契约：<br>
     * 1. heartbeat的insKey为必填项<br>
     * 2. 返回结果HbResult中，config包含recover，说明需要客户端重新注册服务；config包含callback，说明需要重新注册callback<br>
     * @param heartbeat
     * @return
     * @throws Exception
     */
    public HbResult doHeartbeat(Heartbeat heartbeat) throws RpcException;

    /**
     * 读取provider信息<br>
     * 处理契约：<br>
     * 1. jsonString是json对象，转换为LookupParam对象，iface,insKey,alias,ip,protocol,dataVersion为必填项<br>
     * 2. 返回SubscribeUrl中sourceUrl中，会给出dataVersion的值，以便客户端进行订阅版本比对. 如果dataVersion没有变化，就不返回provider列表了<br>
     * @param param
     * @return
     * @throws RpcException
     */
    public LookupResult lookup(LookupParam param) throws RpcException;

    /**
     * 读取provider信息<br>
     * 处理契约：<br>
     * 1. jsonString是json对象，转换为LookupParam对象，iface,insKey,alias,ip,protocol,dataVersion为必填项<br>
     * 2. 返回SubscribeUrl中sourceUrl中，会给出dataVersion的值，以便客户端进行订阅版本比对. 如果dataVersion没有变化，就不返回provider列表了<br>
     * @param param
     * @return
     * @throws RpcException
     */
    public List<LookupResult> lookuplist(List<LookupParam> list) throws RpcException;
}
