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
package com.ipd.jsf.registry.schedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.monitor.service.MonitorRegistryService;
import com.ipd.jsf.monitor.service.vo.IpData;
import com.ipd.jsf.monitor.service.vo.RegistryData;
import com.ipd.jsf.registry.recoder.CallbackRecoder;
import com.ipd.jsf.registry.recoder.IpRequestHandler;
import com.ipd.jsf.registry.recoder.IpRequestRecorder;
import com.ipd.jsf.registry.recoder.RequestRecoder;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.fastjson.JSON;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.Constants.ProtocolType;

/**
 * 检查数据库是否连通
 */
public class RecorderSchedule {
	public static volatile boolean isDBOK = true;

    private static final Logger logger = LoggerFactory.getLogger(RecorderSchedule.class);

    private int interval = 120;  //120秒，需要设置小些

    private ConsumerConfig<MonitorRegistryService> consumerConfig = null;

    private MonitorRegistryService monitorRegistryService = null;

    private List<JsfUrl> providerList = null;

    private String providerUrl = null;

    private String alias = "1.0.0";

    @Autowired
    private SubscribeService subscribeService;
    
    public void start() {
        schedule();
        initMonitorSendor();
        logger.info("RecoderSchedule is running...");
    }

    /**
     * 定时清数据
     */
    private void schedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("recoder"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    CallbackRecoder.recordTime();
                    CallbackRecoder.calCallbackCount();
                    RequestRecoder.calProviderRegistryCount();
                    RequestRecoder.calConsumerRegistryCount();
                    IpRequestHandler.calAllCount();
                    sendMonitorData();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, getDelaySecond(), interval, TimeUnit.SECONDS);
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

    /**
     * 获取推迟的时间，按秒计算，以0秒开始
     * @return
     */
    private int getDelaySecond() {
        int currentMinute = Calendar.getInstance().get(Calendar.MINUTE);
        int deltaSecond = 0;
        if (currentMinute % 2 == 0) {
            deltaSecond = 60;
        }
        int currentSecond = Calendar.getInstance().get(Calendar.SECOND);
        int delaySecond = 60 - currentSecond + deltaSecond;
        if (delaySecond == interval) {
            delaySecond = 0;
        }
        return delaySecond;
    }

    private void sendMonitorData() {
        initMonitorSendor();
        try {
            int sendLimit = 20;
            RegistryData registryData = new RegistryData();
            registryData.setRegIp(RegistryUtil.getRegistryIP());
            registryData.setRegPort(RegistryUtil.getRegistryPort());
            registryData.setTime(System.currentTimeMillis());

            List<IpData> dataList = new ArrayList<IpData>();
            //获取IP采集的值
            for (Entry<String, IpRequestRecorder> entry : IpRequestHandler.ipRecorderMap.entrySet()) {
            	IpData jdata = new IpData();
                jdata.setIp(entry.getKey());
                jdata.setHbCount(entry.getValue().hbCount.getLastCount());
                jdata.setRegistryCount(entry.getValue().registryCount.getLastCount());
                jdata.setSubscribeCount(entry.getValue().subscribeCount.getLastCount());
                jdata.setTime(entry.getValue().lastUpdateTime);
                dataList.add(jdata);
                if (dataList.size() > sendLimit && monitorRegistryService != null) {
                    registryData.setDataList(dataList);
                    monitorRegistryService.collect(registryData);
                    logger.info("-->>> send data:{}", JSON.toJSONString(registryData));
                    dataList.clear();
                }
            }
            if (!dataList.isEmpty() && monitorRegistryService != null) {
                registryData.setDataList(dataList);
                monitorRegistryService.collect(registryData);
                logger.info("-->>> send data:{}", JSON.toJSONString(registryData));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

//    private void initMonitorSendor() {
//        if (consumerConfig == null) {
//            try {
//                consumerConfig = new ConsumerConfig<MonitorRegistryService>();
//                consumerConfig.setInterfaceId(MonitorRegistryService.class.getName());
//                consumerConfig.setProtocol("jsf");
//                consumerConfig.setAlias("1.0.0");
//                consumerConfig.setLazy(true);
//                if (monitorRegistryService == null) {
//                    monitorRegistryService = consumerConfig.refer();
//                }
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//            }
//        }
//    }

    private void initMonitorSendor() {
        if (isChangedProviderList()) {
            try {
                convertUrlFromProviderList();
                if (consumerConfig == null) {
                    consumerConfig = new ConsumerConfig<MonitorRegistryService>();
                } else {
                    logger.info("------------------------MonitorRegistryService  unrefer.......new providerlist: {}", providerUrl);
                    consumerConfig.unrefer();
                }
                consumerConfig.setInterfaceId(MonitorRegistryService.class.getName());
                consumerConfig.setProtocol("jsf");
                consumerConfig.setAlias(alias);
                //采用直连
                consumerConfig.setUrl(providerUrl);
                consumerConfig.setRegister(false);
                consumerConfig.setLazy(true);
                monitorRegistryService = consumerConfig.refer();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 检查providerList是否变化
     * @return
     */
    private boolean isChangedProviderList() {
        try {
            List<JsfUrl> tempList = subscribeService.subscribe(
                    MonitorRegistryService.class.getName(), alias,
                    ProtocolType.jsf.value(),
                    String.valueOf(Constants.JSF_VERSION),
                    Constants.DEFAULT_CODEC_TYPE.name(),
                    null,
                    RegistryUtil.getRegistryIP(), null, 0);
            if (tempList != null && !tempList.isEmpty()) {
                if (providerList == null || providerList.size() != tempList.size()) {
                    providerList = tempList;
                    return true;
                } else {
                    boolean findFlag = false;
                    for (JsfUrl temp : tempList) {
                        findFlag = false;
                        for (JsfUrl provider : providerList) {
                            if (temp.getIp().equals(provider.getIp()) && temp.getPort() == provider.getPort()) {
                                findFlag = true;
                                break;
                            }
                        }
                        //如果新的provider节点不在旧的列表里，说明provider发生变化了
                        if (findFlag == false) {
                            break;
                        }
                    }
                    if (findFlag == false) {
                        providerList = tempList;
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private void convertUrlFromProviderList() {
        StringBuilder url = new StringBuilder();
        if (providerList != null) {
            for (JsfUrl jsfUrl : providerList) {
                if (url.length() > 0) {
                    url.append(",");
                }
                url.append(jsfUrl.getIp()).append(":").append(jsfUrl.getPort());
            }
        }
        providerUrl = url.toString();
    }
    
    public static void main(String[] args) {
        final RecorderSchedule s = new RecorderSchedule();
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("recoder"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    s.getDelaySecond();
                } catch (Exception e) {
                    logger.error("RecoderSchedule error", e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}
