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
package com.ipd.jsf.service.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.transport.Callback;

public class TestRegistryService {
    
    private Callback<SubscribeUrl, String> getCallback() {
        return new Callback<SubscribeUrl, String>() {
            @Override
            public String notify(SubscribeUrl result) {
                return "ok";
            }
        };
    }

//    @Test
    public void testLookupList() {
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
        consumerConfig.setInterfaceId(RegistryService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryService registryService = consumerConfig.refer();
        List<JsfUrl> list = new ArrayList<JsfUrl>();
        //请测试前在lookup方法中设置client ip
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp("127.0.0.1");
        jsfUrl.setInsKey("127.0.0.1_12345_12345");
        jsfUrl.setProtocol(1);
        jsfUrl.setPid(12345);
        jsfUrl.setStTime(new Date().getTime());
        jsfUrl.setIface("com.ipd.testsaf.TestNewSafHelloService");
        jsfUrl.setDataVersion(0);
        jsfUrl.setAlias("jsf");
        list.add(jsfUrl);
        
        jsfUrl = new JsfUrl();
        jsfUrl.setIp("127.0.0.1");
        jsfUrl.setInsKey("127.0.0.1_12345_12345");
        jsfUrl.setProtocol(1);
        jsfUrl.setPid(12345);
        jsfUrl.setStTime(new Date().getTime());
        jsfUrl.setDataVersion(0);
        jsfUrl.setAlias("zhanggeng-6");
        jsfUrl.setIface("com.ipd.testjsf.HelloService");
        list.add(jsfUrl);
        
        List<SubscribeUrl> resultList = registryService.lookupList(list);
        System.out.println("testLookupList>>>>>>>>>>>>>>>>" + resultList);
    }
    
    @Test
    public void testGetConfigList() {
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<RegistryService> consumerConfig = new ConsumerConfig<RegistryService>();
        consumerConfig.setInterfaceId(RegistryService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryService registryService = consumerConfig.refer();
        List<JsfUrl> list = new ArrayList<JsfUrl>();
        //请测试前在lookup方法中设置client ip
        JsfUrl jsfUrl = new JsfUrl();
        jsfUrl.setIp("127.0.0.1");
        jsfUrl.setPid(12345);
        jsfUrl.setStTime(new Date().getTime());
        jsfUrl.setDataVersion(0);
        list.add(jsfUrl);
        
        jsfUrl = new JsfUrl();
        jsfUrl.setDataVersion(0);
//        jsfUrl.setIface("com.ipd.testsaf.TestNewSafHelloService");
//        jsfUrl.setIface("com.ipd.jsf.test.suite.provider.TestSuiteService");
        jsfUrl.setIface("com.ipd.josl.pop.api.WmsProductionServices4Pop");
        list.add(jsfUrl);
        
        List<JsfUrl> resultList = registryService.getConfigList(list);
        System.out.println("testGetConfigList>>>>>>>>>>>>>>>>" + resultList);
    }

}
