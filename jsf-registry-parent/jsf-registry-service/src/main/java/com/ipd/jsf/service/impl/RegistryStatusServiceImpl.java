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
package com.ipd.jsf.service.impl;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.druid.stat.DruidStatService;
import com.alibaba.fastjson.JSON;
import com.ipd.jsf.common.constant.RegistryMonitorConstants;
import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.recoder.CallbackRecoder;
import com.ipd.jsf.registry.recoder.ConnectionRecoder;
import com.ipd.jsf.registry.recoder.RequestRecoder;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryStatusService;

@Service
public class RegistryStatusServiceImpl implements RegistryStatusService {
    @Autowired
    private SubscribeService subscribeService;
    @Autowired
    private SubscribeHelper subscribeHelper;

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryStatusService#conf()
     */
    @Override
    public String conf() {
        
        String jdbcUrl = "jdbc.url";
        String jdbcMaxActive = "jdbc.maxActive";
        String jdbcInitialSize = "jdbc.initialSize";
        String safLogPath = "saf_logpath";
        String isOpenWholeBerkeleyDB = "bdb.open.whole.switch";
        String isOpenProviderBerkeleyDB = "bdb.open.provider.switch";
        String weightFactor = "room.weight.factor";
        Map<String, String> result = new HashMap<String, String>();
        result.put(jdbcUrl, String.valueOf(PropertyFactory.getProperty(jdbcUrl)));
        result.put(jdbcMaxActive, String.valueOf(PropertyFactory.getProperty(jdbcMaxActive)));
        result.put(jdbcInitialSize, String.valueOf(PropertyFactory.getProperty(jdbcInitialSize)));
        result.put(safLogPath, System.getProperty(safLogPath));
        result.put(isOpenWholeBerkeleyDB, String.valueOf(RegistryUtil.isOpenWholeBerkeleyDB));
        result.put(isOpenProviderBerkeleyDB, String.valueOf(RegistryUtil.isOpenProviderBerkeleyDB));
        result.put(weightFactor, String.valueOf(subscribeHelper.getWeightFactor()));
        result.put(RegistryConstants.SAF_INSTANCE, System.getProperty(RegistryConstants.SAF_INSTANCE));
        return JSON.toJSONString(result);
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryStatusService#envi()
     */
    @Override
    public String envi() {
        Map<String, String> result = new HashMap<String, String>();
        // 系统信息
        result.put(RegistryMonitorConstants.OSNAME, System.getProperty(RegistryMonitorConstants.OSNAME, "not specified"));
        result.put(RegistryMonitorConstants.JAVA_VERSION, System.getProperty(RegistryMonitorConstants.JAVA_VERSION, "not specified"));
        result.put(RegistryMonitorConstants.JDK_PATH, System.getProperty(RegistryMonitorConstants.JDK_PATH, "not specified"));
        result.put(RegistryMonitorConstants.APP_PATH, System.getProperty(RegistryMonitorConstants.APP_PATH, "not specified"));

        // jvm信息
        MemoryMXBean memorymbean = ManagementFactory.getMemoryMXBean();
        MemoryUsage usage = memorymbean.getHeapMemoryUsage();
        result.put(RegistryMonitorConstants.JVM_INIT, String.valueOf(usage.getInit()));
        result.put(RegistryMonitorConstants.JVM_MAX, String.valueOf(usage.getMax()));
        result.put(RegistryMonitorConstants.JVM_USED, String.valueOf(usage.getUsed()));
        
        //线程信息
        ThreadMXBean tm = (ThreadMXBean) ManagementFactory.getThreadMXBean();
        result.put(RegistryMonitorConstants.JVM_THREAD_COUNT, String.valueOf(tm.getThreadCount()));
        result.put(RegistryMonitorConstants.JVM_PEAKTHREAD_COUNT, String.valueOf(tm.getPeakThreadCount()));
        result.put(RegistryMonitorConstants.JVM_CURRENTTHREAD_CPUTIME, String.valueOf(tm.getCurrentThreadCpuTime()));
        result.put(RegistryMonitorConstants.JVM_CURRENTTHREAD_USERTIME, String.valueOf(tm.getCurrentThreadUserTime()));
        return JSON.toJSONString(result);
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryStatusService#stat()
     */
    @Override
    public String stat() {
        Map<String, String> result = new HashMap<String, String>();
        //统计的时间段戳
        result.put(RegistryMonitorConstants.STAT_RECORD_SECTION_TIME, String.valueOf(CallbackRecoder.getRecordTime()));
        //统计当前时间戳
        result.put(RegistryMonitorConstants.STAT_CURRENT_TIME, String.valueOf(System.currentTimeMillis()));
        //当前的连接数
        result.put(RegistryMonitorConstants.STAT_CONN_COUNT, String.valueOf(ConnectionRecoder.getConnectionTotalCount()));
        //当前的连接数
        result.put(RegistryMonitorConstants.STAT_CONN_TOTALCOUNT, String.valueOf(ConnectionRecoder.getConnectionTotalCount()));
        //当前的callback数量
        result.put(RegistryMonitorConstants.STAT_CALLBACK_COUNT, String.valueOf(CallbackRecoder.getCallbackCount()));
        //当前的callback失败数量
        result.put(RegistryMonitorConstants.STAT_CALLBACK_FAIL_TOTALCOUNT, String.valueOf(CallbackRecoder.getCallbackFailTotalCount()));
        //当前的心跳总数
        result.put(RegistryMonitorConstants.STAT_HEARTBEAT_TOTALCOUNT, String.valueOf(RequestRecoder.getHeartbeatTotalCount()));
        //provider注册请求数
        result.put(RegistryMonitorConstants.STAT_REGISTRY_PROVIDER_COUNT, String.valueOf(RequestRecoder.getProviderRegistryCount()));
        //provider注册请求总数
        result.put(RegistryMonitorConstants.STAT_REGISTRY_PROVIDER_TOTALCOUNT, String.valueOf(RequestRecoder.getProviderRegistryTotalCount()));
        //provider取消注册请求数
        result.put(RegistryMonitorConstants.STAT_UNREGISTRY_PROVIDER_TOTALCOUNT, String.valueOf(RequestRecoder.getProviderUnRegistryTotalCount()));
        //consumer注册请求数
        result.put(RegistryMonitorConstants.STAT_REGISTRY_CONSUMER_COUNT, String.valueOf(RequestRecoder.getConsumerRegistryCount()));
        //consumer注册请求总数
        result.put(RegistryMonitorConstants.STAT_REGISTRY_CONSUMER_TOTALCOUNT, String.valueOf(RequestRecoder.getConsumerRegistryTotalCount()));
        //consumer取消注册请求总数
        result.put(RegistryMonitorConstants.STAT_UNREGISTRY_CONSUMER_TOTALCOUNT, String.valueOf(RequestRecoder.getConsumerUnRegistryTotalCount()));
        //订阅服务列表请求总数
        result.put(RegistryMonitorConstants.STAT_DOSUBSCRIBE_TOTALCOUNT, String.valueOf(RequestRecoder.getSubscribeTotalCount()));
        //取消订阅服务列表请求总数
        result.put(RegistryMonitorConstants.STAT_UNSUBSCRIBE_TOTALCOUNT, String.valueOf(RequestRecoder.getUnSubscribeTotalCount()));
        //获取服务列表请求数总数
        result.put(RegistryMonitorConstants.STAT_LOOKUP_TOTALCOUNT, String.valueOf(RequestRecoder.getLookupTotalCount()));
        //订阅全局配置请求数总数
        result.put(RegistryMonitorConstants.STAT_SUBSCRIBE_GLOBAL_TOTALCOUNT, String.valueOf(RequestRecoder.getSubscribeGlobalConfigTotalCount()));
        //订阅接口配置请求数
        result.put(RegistryMonitorConstants.STAT_SUBSCRIBE_INTERFACE_TOTALCOUNT, String.valueOf(RequestRecoder.getSubscribeInterfaceConfigTotalCount()));
        //获取全局配置请求数
        result.put(RegistryMonitorConstants.STAT_GETCONFIG_GLOBAL_TOTALCOUNT, String.valueOf(RequestRecoder.getGetGlobalConfigTotalCount()));
        //获取接口配置请求数
        result.put(RegistryMonitorConstants.STAT_GETCONFIG_INTERFACE_TOTALCOUNT, String.valueOf(RequestRecoder.getGetInterfaceConfigTotalCount()));
        //获取失败请求数
        result.put(RegistryMonitorConstants.STAT_REQUEST_FAIL_TOTALCOUNT, String.valueOf(RequestRecoder.getRequestFailTotalCount()));
        return JSON.toJSONString(result);
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryStatusService#cons()
     */
    @Override
    public String cons() {
        Map<String, String> result = new HashMap<String, String>();
        result.put("all_conn", ConnectionRecoder.getAllConnContext().toString());
        return JSON.toJSONString(result);
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.service.RegistryStatusService#wchs()
     */
    @Override
    public String wchs() throws Exception {
        return "";
    }

    @Override
    public String dbex() throws Exception {
        DruidStatService statService = DruidStatService.getInstance();
        return statService.service("/sql.json");
    }

	@Override
	public String wtch(String interfaceName) throws Exception {
		if (interfaceName != null && !interfaceName.isEmpty()) {
			return subscribeService.getCallbackInfo(interfaceName);
		}
		return "";
	}

}
