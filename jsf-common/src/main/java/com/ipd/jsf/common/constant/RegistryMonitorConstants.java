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

package com.ipd.jsf.common.constant;

public class RegistryMonitorConstants {
    //系统名称
    public static String OSNAME = "os.name";
    //jdk版本
    public static String JAVA_VERSION = "java.version";
    //jdk路径
    public static String JDK_PATH = "java.home";
    //实例路径
    public static String APP_PATH = "user.home";
    //heap初始值
    public static String JVM_INIT = "heap.init";
    //heap最大值
    public static String JVM_MAX = "heap.max";
    //heap使用值
    public static String JVM_USED = "heap.uesd";
    //jvm线程数
    public static String JVM_THREAD_COUNT = "jvm.thread.count";
    //jvm使用时最大线程数
    public static String JVM_PEAKTHREAD_COUNT = "jvm.peakthread.count";
    //jvm当前线程cpu时间
    public static String JVM_CURRENTTHREAD_CPUTIME = "jvm.currentthread.cputime";
    //jvm当前线程cpu使用时间
    public static String JVM_CURRENTTHREAD_USERTIME = "jvm.currentthread.usertime";

    //连接数
    public static String STAT_CONN_COUNT = "connection.count";
    //连接数
    public static String STAT_CONN_TOTALCOUNT = "connection.totalcount";
    //心跳数
    public static String STAT_HEARTBEAT_TOTALCOUNT = "hb.totalcount";
    //回调数
    public static String STAT_CALLBACK_COUNT = "callback.count";
    //回调失败总数
    public static String STAT_CALLBACK_FAIL_TOTALCOUNT = "callback.fail.totalcount";
    //provider注册请求数
    public static String STAT_REGISTRY_PROVIDER_COUNT = "registry.provider.count";
    //provider注册请求总数
    public static String STAT_REGISTRY_PROVIDER_TOTALCOUNT = "registry.provider.totalcount";
    //provider取消注册请求总数
    public static String STAT_UNREGISTRY_PROVIDER_TOTALCOUNT = "unregistry.provider.totalcount";
    //consumer注册请求数
    public static String STAT_REGISTRY_CONSUMER_COUNT = "registry.consumer.count";
    //consumer注册请求总数
    public static String STAT_REGISTRY_CONSUMER_TOTALCOUNT = "registry.consumer.totalcount";
    //consumer取消注册请求数
    public static String STAT_UNREGISTRY_CONSUMER_TOTALCOUNT = "unregistry.consumer.totalcount";
    //订阅服务列表请求总数
    public static String STAT_DOSUBSCRIBE_TOTALCOUNT = "dosubscribe.totalcount";
    //取消订阅服务列表请求总数
    public static String STAT_UNSUBSCRIBE_TOTALCOUNT = "unsubscribe.totalcount";
    //获取服务列表请求数总数
    public static String STAT_LOOKUP_TOTALCOUNT = "lookup.totalcount";
    //订阅全局配置请求数总数
    public static String STAT_SUBSCRIBE_GLOBAL_TOTALCOUNT = "subscribe.global.totalcount";
    //订阅接口配置请求数总数
    public static String STAT_SUBSCRIBE_INTERFACE_TOTALCOUNT = "subscribe.interface.totalcount";
    //获取全局配置请求数
    public static String STAT_GETCONFIG_GLOBAL_TOTALCOUNT = "getconfig.global.totalcount";
    //获取接口配置请求数
    public static String STAT_GETCONFIG_INTERFACE_TOTALCOUNT = "getconfig.interface.totalcount";
    //获取失败请求数
    public static String STAT_REQUEST_FAIL_TOTALCOUNT = "request.fail.totalcount";

    //统计时间段戳
    public static String STAT_RECORD_SECTION_TIME = "record.section.time";
    //统计当前时间戳
    public static String STAT_CURRENT_TIME = "record.current.time";
}
