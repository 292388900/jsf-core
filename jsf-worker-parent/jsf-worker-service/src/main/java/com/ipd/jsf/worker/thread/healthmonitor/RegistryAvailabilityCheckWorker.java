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
package com.ipd.jsf.worker.thread.healthmonitor;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.JSFContext;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.registry.service.JsfRegistryCheckService;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.service.RegistryAddressCache;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.management.ManagementFactory;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


/**
 * Title: <br>
 *
 * Description:
 *     定时检查各个注册中心的可用性，每个机房都部署相关实例，在香港跟广州比较特殊【香港只检查香港的，广州检查广州的】<br>
 *     <B></>不部署印尼机房，不配置印尼机房注册中心地址</B>
 */
public class RegistryAvailabilityCheckWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(RegistryAvailabilityCheckWorker.class);

    private static List<String> regAddrs = new ArrayList<String>();//缓存注册中心地址列表
    private static long dateBeforeTime;
    private final String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
    private static int defaultPort = 40000;
    private final static int connectTimes = 3;//检查注册中心可用性共尝试的次数
    private static ConcurrentMap<String, Date> alarmList = new ConcurrentHashMap<String, Date>();//报警列表记录

    private Map<String, Exception> exceptionList;

    @Autowired
    private RegistryAddressCache registryAddressCache;

    @Override
    public void init() {
    }

    //只有在有异常的时候，再初始化，懒加载
    public Map<String, Exception> getExceptionList() {
        if (exceptionList == null) {
            exceptionList = new HashMap<String, Exception>();
        }
        return exceptionList;
    }

    /**
     * 改从UCC取数据
     */
    public void getRegistryAddress() {
        //缓存,获取线上的注册中心
        List<String> regAddrList = null;
        String config = "";
        try {
            String addressFrom = "registry_address.key.default";
            config = registryAddressCache.getRegistryAddress().get(PropertyFactory.getProperty(addressFrom));
            logger.info("Get the [{}] registry address from ucc success:[{}].", PropertyFactory.getProperty(addressFrom), config);
        } catch (Exception e) {
            //异常，不报警
            logger.error(e.getMessage() + ",from ip  {}", WorkerUtil.getWorkerIP(), e);
        }
        if (!StringUtils.isEmpty(config)) {
            regAddrList = Arrays.asList(config.split(";"));
        }
        //如果获取的列表为空，不更新缓存
        if (regAddrList != null && regAddrList.size() > 0) {
            regAddrs.clear();
            for (String regAddr : regAddrList) {
                regAddrs.add(regAddr);
            }
            logger.info("get the valid registry address,size:{}.", (regAddrList == null ? null : regAddrList.size()));
            dateBeforeTime = System.currentTimeMillis();
        }
    }

    public boolean checkRegAddr() {
        if (regAddrs == null || regAddrs.size() == 0) {
            Exception e = new NoRegistryAddressException(WorkerUtil.getWorkerIP());
            logger.error("umpKey:" + (String) PropertyFactory.getProperty("ump.registry.check.key") + ";" + e.getMessage(), e);
            return false;
        }
        return true;
    }

    /**
     * 接口【com.ipd.jsf.worker.service.JsfRegistryCheckService】服务实例定时各个注册中心进行注册、反注册、lookup、反订阅等
     * @return
     */
    @Override
    public boolean run() {
        //定时获取注册中心地址列表；或者当前地址列表为空，重试从缓存取数据
        if (regAddrs == null || regAddrs.size() <= 0 || isTimeout()) {
            getRegistryAddress();
        }
        if (!checkRegAddr()) {
            return true;
        }
        logger.info("run the worker of checking the availability of registry center starting......");
        try {
            if (regAddrs != null && regAddrs.size() > 0) {
                if (exceptionList != null && exceptionList.size() > 0)
                    exceptionList.clear();
                for (String regAddr : regAddrs) {
                    ConsumerConfig<RegistryService> consumerConfig = getConsumerConfig();
                    try {
                        consumerConfig.setUrl(regAddr);
                        //直连registry中心,尝试connectTimes次后，报警
                        RegistryService registryService = getRegistryService(consumerConfig);
                        if (registryService == null) {
                            continue;
                        }
                        //注册com.ipd.jsf.worker.service.JsfRegistryCheckService[Provider]服务,检查注册中心可用性，返回insKey
                        String insKeyP = doRegistry(registryService, regAddr, true);
                        if (StringUtils.isEmpty(insKeyP)) {
                            continue;
                        }
                        //注册consumer服务,检查注册中心可用性
                        String insKeyC = doRegistry(registryService, regAddr, false);
                        if (StringUtils.isEmpty(insKeyC)) {
                            continue;
                        }

                        //Consumer订阅 todo

                        JsfUrl jsfUrlC = buildJsfUrl(false);
                        jsfUrlC.setInsKey(insKeyC);

                        //lookup服务列表
                        if (!lookUp(registryService, jsfUrlC, regAddr)) {
                            continue;
                        }

                        if (!doUnSubscribe(registryService, jsfUrlC, regAddr)) {
                            continue;
                        }

                        //反注册Consumer
                        boolean unRegistryFlagC = doUnRegister(registryService, jsfUrlC, regAddr);
                        if (!unRegistryFlagC) {
                            continue;
                        }

                        //反注册Provider
                        JsfUrl jsfUrlP = buildJsfUrl(true);
                        jsfUrlP.setInsKey(insKeyP);
                        boolean unRegistryFlagP = doUnRegister(registryService, jsfUrlP, regAddr);
                        if (!unRegistryFlagP) {
                            continue;
                        }

                        //如果注册中心服务可用，则从alarmList中移除。
                        if (alarmList.containsKey(regAddr)) {
                            alarmList.remove(regAddr);
                        }
                    } catch (Exception re) {
                        logger.error("jsf注册中心检测出现异常：", re.getMessage(), re);
                    } finally {
                        consumerConfig.unrefer();
                    }
                }
                staticAlarm();
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    /**
     * 对报警数据进行统计，报出；
     */
    private void staticAlarm() {
        int i = 0;
        StringBuffer sbf = new StringBuffer("[Jsf]registry check: unavailable registry:[");
        Map<String, Integer> eType = new HashMap<String, Integer>();
        if (exceptionList == null || exceptionList.isEmpty())
            return;
        for (Map.Entry<String, Exception> entry : exceptionList.entrySet()) {
            if (i != 0) {
                sbf.append(",");
            }
            String address = entry.getKey();
            //String excepationType=entry.getValue().getClass().getName();
            String excepationType = ExceptionHandler.getExceptionCode(entry.getValue());
            if (eType.containsKey(excepationType)) {
                int count = eType.get(excepationType).intValue() + 1;
                eType.put(excepationType, count);
            } else {
                eType.put(excepationType, 1);
            }
            sbf.append(address);
            i++;
        }
        if (i > 0) {
            int j = 0;
            sbf.append("],Exception:");
            for (Map.Entry<String, Integer> type : eType.entrySet()) {
                String typeS = type.getKey();
                if (j != 0) {
                    sbf.append(";");
                }
                sbf.append(typeS).append("[").append(type.getValue()).append("]");
                j++;
            }
            sbf.append(",from ip:").append(WorkerUtil.getWorkerIP()).append(",报警时间:").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis()));
            logger.error(sbf.toString());
        }
    }


    private void loggerAlarm(String url, Exception e) {
        logger.error("registry address check failure, registry: " + url + ", alarm time:" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())) + e.getMessage(), e);
        logger.error("because of had alarmed, now don't alarm......");
    }

    private void doSleep(Integer sleepTime) {
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

    /**
     * 从注册中心拉去服务列表
     * @param registryService
     * @param jsfUrl
     * @param regAddr
     * @return
     */
    private boolean lookUp(RegistryService registryService, JsfUrl jsfUrl, String regAddr) {
        boolean flag = false;
        List<JsfUrl> providerList = null;
        for (int i = 0; i < connectTimes; i++) {
            try {
                SubscribeUrl subscribeUrl = registryService.lookup(jsfUrl);
                providerList = subscribeUrl.getProviderList();
                if (providerList != null && providerList.size() > 0) {
                    flag = true;
                }
            } catch (RpcException rpE) {//JSF客户端,JSF注册中心，统一抛RpcException异常
                if (i == connectTimes - 1) {
                    dealAlarm(regAddr, rpE);
                }
            }
            if (flag) {
                logger.info("look up consumer subscribe list success:[");
                for (JsfUrl jsfurl : providerList) {
                    logger.info(jsfurl.toString());
                }
                logger.info("]");
                break;
            }
            doSleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
        }
        return flag;
    }

    /**
     * doRegistry connectTimes
     * @param registryService
     * @param regAddr
     * @param isProvider
     * @return
     */
    private String doRegistry(RegistryService registryService, String regAddr, boolean isProvider) {
        String insKey = "";
        for (int i = 0; i < connectTimes; i++) {
            try {
                JsfUrl result = registryService.doRegister(buildJsfUrl(isProvider));
                insKey = result.getInsKey();
            } catch (Exception e) {
                if (i == connectTimes - 1) {
                    dealAlarm(regAddr, e);
                }
            }
            if (StringUtils.isNotEmpty(insKey)) {
                logger.info("doRegistry client success,return insKey:{}, registry center address:{} ", insKey, regAddr);
                break;
            }
            doSleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
        }
        return insKey;
    }

    private void dealAlarm(String regAddr, Exception e) {
        if (!alarmFlag(regAddr)) {
            loggerAlarm(regAddr, e);
        } else {
            alarmList.put(regAddr, new Date());
            getExceptionList().put(regAddr, e);
            logger.error("umpKey:" + (String) PropertyFactory.getProperty("ump.registry.check.key") + "," + e.getMessage(), e);
        }
    }

    /**
     * consumer反订阅
     * @param registryService
     * @param jsfUrl
     * @param regAddr
     * @return
     */
    private boolean doUnSubscribe(RegistryService registryService, JsfUrl jsfUrl, String regAddr) {
        boolean flag = false;
        for (int i = 0; i < connectTimes; i++) {
            try {
                flag = registryService.doUnSubscribe(jsfUrl);
            } catch (RpcException rpcE) {//JSF客户端,JSF注册中心，统一抛RpcException异常
                if (i == connectTimes - 1) {
                    dealAlarm(regAddr, rpcE);
                }
            }
            if (flag) {
                logger.info("consumer doUnsubscribe {}.", flag);
                break;
            }
            doSleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
        }
        return flag;
    }

    /**
     * 反注册
     * @param registryService
     * @param jsfUrl
     * @param regAddr
     * @return
     */
    private boolean doUnRegister(RegistryService registryService, JsfUrl jsfUrl, String regAddr) {
        boolean flag = false;
        for (int i = 0; i < connectTimes; i++) {
            try {
                flag = registryService.doUnSubscribe(jsfUrl);
            } catch (RpcException rpcE) {//JSF客户端,JSF注册中心，统一抛RpcException异常
                if (i == connectTimes - 1) {
                    dealAlarm(regAddr, rpcE);
                }
            }
            if (flag) {
                logger.info("duUnRegistry client {} success.", jsfUrl.getIface());
                break;
            }
            doSleep(Integer.valueOf((String) PropertyFactory.getProperty("interrupted.time")));
        }
        return flag;
    }

    /**
     * build用于测试的Provider、Consumer的jsfUrl,Consumer根据protocol、interface、alias去registryService注册
     * @param isfProvider
     * @return
     */
    private JsfUrl buildJsfUrl(boolean isfProvider) {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp(NetUtils.getLocalHost());
        //Todo Port的判断及递增
        safUrl.setAlias((String) PropertyFactory.getProperty("interface.alias"));
        safUrl.setPid(Integer.parseInt(PID));
        safUrl.setStTime(JSFContext.START_TIME);
        safUrl.setIface(JsfRegistryCheckService.class.getCanonicalName());
        safUrl.setTimeout(7000);
        safUrl.setProtocol(Constants.ProtocolType.jsf.value());
        Map<String, String> attrs = new HashMap<String, String>();
        if (isfProvider) {
            safUrl.setPort(defaultPort);
            safUrl.setRandom(false);
            attrs.put("weight", "100");
        } else {
            attrs.put("consumer", "1");
        }
        //attrs.put("safVersion", String.valueOf(Constants.JSF_VERSION));
        attrs.put("app", (String) PropertyFactory.getProperty("app.name"));
        attrs.put("appId", (String) PropertyFactory.getProperty("app.id"));
        attrs.put("apppath", (String) PropertyFactory.getProperty("app.path"));
        attrs.put("LANGUAGE", "java");
        attrs.put("jsfVersion", String.valueOf(Constants.JSF_VERSION));
        safUrl.setAttrs(attrs);
        return safUrl;
    }

    private ConsumerConfig<RegistryService> getConsumerConfig() {
        ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
        consumerConfig.setInterfaceId(RegistryService.class.getCanonicalName());
        consumerConfig.setAlias((String) PropertyFactory.getProperty("jsf.server.alias"));
        consumerConfig.setProtocol((String) PropertyFactory.getProperty("jsf.server.protocol"));
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        consumerConfig.setCheck(true);//true:提前捕获异常;false:连接provider时不捕捉异常
        consumerConfig.setSubscribe(false);
        consumerConfig.setRetries(connectTimes);
        return consumerConfig;
    }

    /**
     * 根据注册中心地址信息获取注册中心服务
     * @return
     */
    private RegistryService getRegistryService(ConsumerConfig<RegistryService> consumerConfig) {
        RegistryService registryService = null;
        try {
            registryService = consumerConfig.refer();
            logger.info("return registry center service {}.", consumerConfig.getUrl());
        } catch (InitErrorException initE) {
            //报过警，and 未过报警时间间隔，then 只记录日志、不报警
            dealAlarm(consumerConfig.getUrl(), initE);
        } finally {
            return registryService;
        }
    }

    /**
     *每次只需执行任务时，检查数据缓存是否过期
     */
    private boolean isTimeout() {
        long nowDate = System.currentTimeMillis();
        String IntervalTime = (String) PropertyFactory.getProperty("data.update.intervalTime");
        if (WorkerUtil.getDateUtil(nowDate, dateBeforeTime) >= Integer.parseInt(IntervalTime)) {
            return true;
        }
        return false;
    }

    /**
     *注册中心服务是否需要可用性报警检测：如果已在alarmIntervalTime内报过警，则不检测
     * @param alarmTime
     * @return
     */
    private boolean isNotAlarm(Date alarmTime) {
        long nowTime = System.currentTimeMillis();
        String alarmIntervalTime = (String) PropertyFactory.getProperty("alarm.intervalTime");
        if (WorkerUtil.getDateUtil(nowTime, alarmTime.getTime()) < Integer.parseInt(alarmIntervalTime)) {
            return true;
        }
        return false;
    }

    private boolean alarmFlag(String registryAddress) {
        if (alarmList.containsKey(registryAddress) && isNotAlarm(alarmList.get(registryAddress))) {
            return false;
        }
        return true;
    }

    /**
    @Override
    public String getWorkerType() {
        return "registryCheckWorker";
    }
    */

    public String getIntervalTime() {
        return (String) PropertyFactory.getProperty("registry.check.intervalTime");
    }

}