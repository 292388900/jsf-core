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
package com.ipd.testsaf;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ipd.fastjson.JSON;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.Heartbeat;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;

public class RegistryServiceTest {
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceTest.class);
    AtomicInteger count = new AtomicInteger(0);
    public static void main(String[] args) {
        RegistryServiceTest test = new RegistryServiceTest();
//        test.testBatchProviderRegistry();
//        test.testProviderUnRegistryList();
        test.testConsumerRegistryList();
    }

    private ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("/spring-registry-consumer.xml");
    private RegistryService registryService = (RegistryService)appContext.getBean("registryService");
    private final static AtomicInteger cnt = new AtomicInteger(0);
    
    public void testBatchProviderRegistry() {

        int thread = 1000;
//        final int num = 1000;
//        final CountDownLatch latch = new CountDownLatch(thread);
        ExecutorService threadPool = Executors.newFixedThreadPool(200);
        long start = System.currentTimeMillis();
        for (int i = 0; i < thread; i++) {
            threadPool.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
//                            Thread.sleep(1000);
                            testProviderRegistry();
                            break;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
//                    latch.countDown();
                }
            });
        }
        
        Thread thread1 = new Thread(new Runnable() {
            private long last = 0;
            @Override
            public void run() {
                while (true) {
                    long count = cnt.get();
                    long tps = count - last;
                    System.out.println("last 1s invoke: "+ tps);
                    last = count;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"Print-tps-THREAD");
        thread1.start();
        
        logger.info("elapse:{}", (System.currentTimeMillis() - start));
    }

    /**
     * 测试provider注册
     */
    public void testProviderRegistry() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("192.168.75." + Math.abs(new Random().nextInt(255)));  //TODO
        safUrl.setPort(new Random().nextInt(20));          //TODO
        safUrl.setAlias("1bnt_test" + new Random().nextInt(25500));        //TODO
        safUrl.setPid(12345);           //TODO
        safUrl.setStTime((new Date()).getTime());              //TODO
        safUrl.setIface("com.ipd.testsaf.TestHelloService");    //TODO
        safUrl.setProtocol(ProtocolType.jsf.value());
        safUrl.setRandom(false);
        safUrl.setTimeout(3000);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("apppath", "/export/server/tomcat/web/app");
        attrs.put("language", "java");
        attrs.put("safver", String.valueOf(210));
        attrs.put("weight", "10000");
        safUrl.setAttrs(attrs);

        String insKey = "";
        try {
            JsfUrl result = registryService.doRegister(safUrl);
            insKey = result.getInsKey();
            cnt.incrementAndGet();
        } catch (Exception e) {
            logger.error("registry insKey:" + insKey + ", error:" + e.getMessage(), e);
        }

    }

    public void testConsumerRegistryList() {
        int i = 1000;
        while (i-- > 0) {
            testConsumerRegistry();
        }
    }

    /**
     * 测试consumer注册
     */
    public void testConsumerRegistry() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("192.168.75." + new Random().nextInt(255));  //TODO
        safUrl.setAlias("test");        //TODO
        safUrl.setPid(new Random().nextInt(65535));           //TODO
        safUrl.setStTime((new Date()).getTime());              //TODO
        safUrl.setIface("com.ipd.testjsf.HelloBaontService");    //TODO
        safUrl.setProtocol(ProtocolType.jsf.value());
        safUrl.setRandom(false);
        safUrl.setTimeout(3000);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("apppath", "/export/server/tomcat/web/app");
        attrs.put("language", "java");
        attrs.put("safVersion", String.valueOf(210));
        attrs.put("jsfVersion", "1601");
        attrs.put("weight", "10000");
        attrs.put("consumer","1");
        safUrl.setAttrs(attrs);

        String insKey = "";
        try {
            JsfUrl result = registryService.doRegister(safUrl);
            insKey = result.getInsKey();
        } catch (Exception e) {
            logger.error("registry insKey:" + insKey + ", error:" + e.getMessage(), e);
        }
    }

    /**
     * 测试provider取消注册
     */
    public void testProviderUnRegistry() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("192.168.75.10");  //TODO
        safUrl.setPort(32842);          //TODO
        safUrl.setAlias("bnt_test");        //TODO
        safUrl.setPid(12345);           //TODO
        safUrl.setStTime((new Date()).getTime());              //TODO
        safUrl.setIface("com.ipd.testsaf.TestHelloService");    //TODO
        safUrl.setProtocol(ProtocolType.jsf.value());     //区分是consumer还是provider
        safUrl.setInsKey("192.168.75.10_12345_87679");
        try {
            registryService.doUnRegister(safUrl);
        } catch (Exception e) {
        }
    }
    
    /**
     * 测试consumer取消注册
     */
    public void testConsumerUnRegistry() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("192.168.75.10");  //TODO
        safUrl.setAlias("test");        //TODO
        safUrl.setPid(12345);           //TODO
        safUrl.setStTime((new Date()).getTime());              //TODO
        safUrl.setIface("com.ipd.testsaf.TestHelloService");    //TODO
        safUrl.setProtocol(ProtocolType.consumer.value());     //区分是consumer还是provider
        
        try {
            registryService.doUnRegister(safUrl);
        } catch (Exception e) {
        }
    }

    /**
     * 测试心跳
     */
    public void testHeartbeat() {
        Heartbeat hb = new Heartbeat();
        String insKey = getInsKey("192.168.209.100", 10234, System.currentTimeMillis());
        hb.setInsKey(insKey);
        try {
            registryService.doHeartbeat(hb);
        } catch (Exception e) {
        }
    }

    /**
     * 测试获取服务列表
     */
    public void testLookup() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setInsKey(getInsKey("192.168.209.100", 10234, System.currentTimeMillis()));
//        safUrl.setIface("com.ipd.testsaf.TestHelloService");
        safUrl.setIface("com.ipd.testjsf.HelloService");
        safUrl.setAlias("baont");
        safUrl.setProtocol(ProtocolType.jsf.value());
        safUrl.setIp("192.168.209.100");   //TODO 请填写真实IP
        safUrl.setDataVersion(123);          //TODO 需要修改
        SubscribeUrl url = registryService.lookup(safUrl);
        System.out.println(JSON.toJSONString(url));
    }

    /**
     * 测试获取全局配置
     */
    public void testGetGlobalConfig() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("192.168.209.100");   //TODO 请填写真实IP
        safUrl.setPid(12345);              //TODO pid
        safUrl.setStTime(System.currentTimeMillis());   //TODO 启动时间
        safUrl.setDataVersion(0);          //TODO 需要修改
        SubscribeUrl url = registryService.lookup(safUrl);
    }

    /**
     * 测试接口配置
     */
    public void testGetIfaceConfig() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIface("com.ipd.testsaf.TestHelloService");
        safUrl.setDataVersion(0);          //TODO 需要修改
        SubscribeUrl url = registryService.lookup(safUrl);
    }

    /**
     * 获取insKey
     * @param ip
     * @param pid
     * @param startTime
     * @return
     */
    private String getInsKey(String ip, int pid, long startTime) {
        StringBuilder sb = new StringBuilder();
        sb.append(ip);
        sb.append("_");
        sb.append(pid);
        sb.append("_");
        if (String.valueOf(startTime).length() > 8) {
            sb.append(String.valueOf(startTime).substring(8));
        } else {
            sb.append(String.valueOf(startTime).substring(0));
        }
        return sb.toString();
    }

    /**
     * 测试provider注册
     */
    public void testProviderRegistryList() {
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp("192.168.75." + Math.abs(new Random().nextInt(255)));  //TODO
        jsfUrl.setPort(new Random().nextInt(20));          //TODO
        jsfUrl.setAlias("1bnt_test" + new Random().nextInt(25500));        //TODO
        jsfUrl.setPid(12345);           //TODO
        jsfUrl.setStTime((new Date()).getTime());              //TODO
        jsfUrl.setIface("com.ipd.testsaf.TestHelloService");    //TODO
        jsfUrl.setProtocol(ProtocolType.jsf.value());
        jsfUrl.setRandom(false);
        jsfUrl.setTimeout(3000);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put("apppath", "/export/server/tomcat/web/app");
        attrs.put("language", "java");
        attrs.put("safVersion", String.valueOf(210));
        attrs.put("weight", "10000");
        attrs.put("jsfVersion", "1551");
        jsfUrl.setAttrs(attrs);

        String insKey = "";
        try {
        	List<JsfUrl> jsfUrlList = new ArrayList<JsfUrl>();
        	jsfUrlList.add(jsfUrl);
        	List<JsfUrl> result = registryService.doRegisterList(jsfUrlList);
            cnt.incrementAndGet();
        } catch (Exception e) {
            logger.error("registry insKey:" + insKey + ", error:" + e.getMessage(), e);
        }
    }

    /**
     * 测试provider取消注册
     */
    public void testProviderUnRegistryList() {
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp("192.168.75.10");  //TODO
        jsfUrl.setPort(32842);          //TODO
        jsfUrl.setAlias("bnt_test");        //TODO
        jsfUrl.setPid(12345);           //TODO
        jsfUrl.setStTime((new Date()).getTime());              //TODO
        jsfUrl.setIface("com.ipd.testsaf.TestHelloService");    //TODO
        jsfUrl.setProtocol(ProtocolType.jsf.value());     //区分是consumer还是provider
        jsfUrl.setInsKey("192.168.75.10_12345_87679");
        try {
        	List<JsfUrl> jsfUrlList = new ArrayList<JsfUrl>();
        	jsfUrlList.add(jsfUrl);
            registryService.doUnRegisterList(jsfUrlList);
        } catch (Exception e) {
        	e.printStackTrace();
        }
    }
    
}
