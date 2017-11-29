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
package com.ipd.jsf.registry.recoder;

import java.util.concurrent.atomic.AtomicLong;

public class RequestRecoder {
    //provider 注册请求计数
    private static Recorder providerRegistryRecorder = new Recorder();
    //provider 取消注册请求计数总数
    private static AtomicLong providerUnRegistryTotalCount = new AtomicLong(0);
    //consumer 注册请求计数
    private static Recorder consumerRegistryRecorder = new Recorder();
    //consumer 取消注册请求计数总数
    private static AtomicLong consumerUnRegistryTotalCount = new AtomicLong(0);
    //订阅服务列表请求计数总数
    private static AtomicLong subscribeTotalCount = new AtomicLong(0);
    //取消订阅请求计数总数
    private static AtomicLong unSubscribeTotalCount = new AtomicLong(0);
    //获取服务列表请求计数总数
    private static AtomicLong lookupTotalCount = new AtomicLong(0);
    //订阅全局配置请求计数总数
    private static AtomicLong subscribeGlobalConfigTotalCount = new AtomicLong(0);
    //订阅接口配置请求计数总数
    private static AtomicLong subscribeInterfaceConfigTotalCount = new AtomicLong(0);
    //获取全局配置请求计数总数
    private static AtomicLong getGlobalConfigTotalCount = new AtomicLong(0);
    //获取接口配置请求计数总数
    private static AtomicLong getInterfaceConfigTotalCount = new AtomicLong(0);
    //获取心跳请求计总数
    private static AtomicLong heartbeatTotalCount = new AtomicLong(0);
    //失败计数总数
    private static AtomicLong requestFailTotalCount = new AtomicLong(0);

    /**
     * @return the providerRegistryCount
     */
    public static long getProviderRegistryCount() {
        return providerRegistryRecorder.getLastCount();
    }
    /**
     * @return the providerRegistryCount
     */
    public static long getProviderRegistryTotalCount() {
        return providerRegistryRecorder.getTotalCount();
    }
    /**
     * provider 注册请求计数加1
     */
    public static void recodeProviderRegistryCount() {
        providerRegistryRecorder.increment();
    }
    /**
     * provider 注册请求计数计算
     */
    public static void calProviderRegistryCount() {
        providerRegistryRecorder.calCount();
    }
    /**
     * @return the providerUnRegistryTotalCount
     */
    public static long getProviderUnRegistryTotalCount() {
        return providerUnRegistryTotalCount.get();
    }
    /**
     * provider 取消注册请求计数加1
     */
    public static void recodeProviderUnRegistryTotalCount() {
        RequestRecoder.providerUnRegistryTotalCount.incrementAndGet();
    }
    /**
     * @return the consumerRegistryCount
     */
    public static long getConsumerRegistryCount() {
        return consumerRegistryRecorder.getLastCount();
    }
    /**
     * @return the consumerRegistryCount
     */
    public static long getConsumerRegistryTotalCount() {
        return consumerRegistryRecorder.getTotalCount();
    }
    /**
     * consumer 注册请求计数加1
     */
    public static void recodeConsumerRegistryCount() {
        consumerRegistryRecorder.increment();
    }
    /**
     * consumer 注册请求计数计算
     */
    public static void calConsumerRegistryCount() {
        consumerRegistryRecorder.calCount();
    }
    /**
     * @return the consumerUnRegistryCount
     */
    public static long getConsumerUnRegistryTotalCount() {
        return consumerUnRegistryTotalCount.get();
    }
    /**
     * consumer 取消注册请求计数加1
     */
    public static void recodeConsumerUnRegistryTotalCount() {
        RequestRecoder.consumerUnRegistryTotalCount.incrementAndGet();
    }
    /**
     * @return the subscribeTotalCount
     */
    public static long getSubscribeTotalCount() {
        return subscribeTotalCount.get();
    }
    /**
     * 订阅服务列表请求计数加1
     */
    public static void recodeSubscribeTotalCount() {
        RequestRecoder.subscribeTotalCount.incrementAndGet();
    }
    /**
     * @return the unSubscribeTotalCount
     */
    public static long getUnSubscribeTotalCount() {
        return unSubscribeTotalCount.get();
    }
    /**
     * 取消订阅请求计数加1
     */
    public static void recodeUnSubscribeTotalCount() {
        RequestRecoder.unSubscribeTotalCount.incrementAndGet();
    }
    /**
     * @return the lookupTotalCount
     */
    public static long getLookupTotalCount() {
        return lookupTotalCount.get();
    }
    /**
     * 获取服务列表请求计数加1
     */
    public static void recodeLookupTotalCount() {
        RequestRecoder.lookupTotalCount.incrementAndGet();
    }
    /**
     * @return the subscribeGlobalConfigTotalCount
     */
    public static long getSubscribeGlobalConfigTotalCount() {
        return subscribeGlobalConfigTotalCount.get();
    }
    /**
     * 订阅全局配置请求计数加1
     */
    public static void recodeSubscribeGlobalConfigTotalCount() {
        RequestRecoder.subscribeGlobalConfigTotalCount.incrementAndGet();
    }
    /**
     * @return the subscribeInterfaceConfigTotalCount
     */
    public static long getSubscribeInterfaceConfigTotalCount() {
        return subscribeInterfaceConfigTotalCount.get();
    }
    /**
     * 订阅接口配置请求计数加1
     */
    public static void recodeSubscribeInterfaceConfigTotalCount() {
        RequestRecoder.subscribeInterfaceConfigTotalCount.incrementAndGet();
    }
    /**
     * @return the getGlobalConfigTotalCount
     */
    public static long getGetGlobalConfigTotalCount() {
        return getGlobalConfigTotalCount.get();
    }
    /**
     * 获取全局配置请求计数加1
     */
    public static void recodeGetGlobalConfigTotalCount() {
        RequestRecoder.getGlobalConfigTotalCount.incrementAndGet();
    }
    /**
     * @return the getInterfaceConfigTotalCount
     */
    public static long getGetInterfaceConfigTotalCount() {
        return getInterfaceConfigTotalCount.get();
    }
    /**
     * 订阅接口配置请求计数加1
     */
    public static void recodeGetInterfaceConfigCount() {
        RequestRecoder.getInterfaceConfigTotalCount.incrementAndGet();
    }
    /**
     * 返回心跳总数
     * @return
     */
    public static long getHeartbeatTotalCount() {
        return heartbeatTotalCount.get();
    }
    /**
     * 记录心跳总数
     */
    public static void recodeHeartbeatTotalCount() {
        heartbeatTotalCount.incrementAndGet();
    }
    /**
     * 返回失败数
     * @return
     */
    public static long getRequestFailTotalCount() {
        return requestFailTotalCount.get();
    }
    /**
     * 记录失败数
     */
    public static void recodeRequestFailTotalCount() {
        requestFailTotalCount.incrementAndGet();
    }
}
