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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.MethodConfig;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.service.RegistryCtrlService;
import com.ipd.jsf.vo.SubscribeUrl;

public class TestRegistryCtrlService {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        TestRegistryCtrlService service = new TestRegistryCtrlService();
//        service.pinpointCallbackInterfaceConfig1();
//        service.consumerConfig();
//        service.disConnect();
//        service.recover();
        service.testResetDruidDs();
    }

    
    public void pinpointCallbackInterfaceConfig() {

        // 服务提供者连接注册中心，设置属性
        
        ConsumerConfig<RegistryCtrlService> consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId(RegistryCtrlService.class.getName());
        consumerConfig.setAlias("saf");
        consumerConfig.setProtocol("saf");
        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        Map<String, String> attribute = new HashMap<String, String>();
        attribute.put("weight", "1");
        RegistryCtrlService service = consumerConfig.refer();
        List<String> insKeyList = new ArrayList<String>();
        insKeyList.add("127.0.0.1_8008_57795");
        
        service.pinpointCallbackInterfaceConfig("com.ipd.testsaf.HelloService", insKeyList, attribute, true);
    }
    
    
    public void pinpointCallbackInterfaceConfig1() {

        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<RegistryCtrlService> consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId(RegistryCtrlService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        Map<String, String> attribute = new HashMap<String, String>();
        attribute.put("alias", "jsf");
        RegistryCtrlService service = consumerConfig.refer();
        List<String> insKeyList = new ArrayList<String>();
        insKeyList.add("127.0.0.1_7564_04428");
        
        service.pinpointCallbackInterfaceConfig("com.ipd.testjsf.HelloService", insKeyList, attribute, false);
    }
    
    public void consumerConfig() {

        // 服务提供者连接注册中心，设置属性
        
        ConsumerConfig<RegistryCtrlService> consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId(RegistryCtrlService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryCtrlService service = consumerConfig.refer();
        System.out.println(service.consumerConfig("com.ipd.testjsf.HelloService", "baont", 1, "127.0.0.1_6500_57172", (byte)1));
    }
    
    public void disConnect() {
        
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<RegistryCtrlService> consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId(RegistryCtrlService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryCtrlService service = consumerConfig.refer();
        System.out.println(service.insCtrl("127.0.0.1_5540_34076", (byte)1));
    }
    
    public void recover() {
        
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<RegistryCtrlService> consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId(RegistryCtrlService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryCtrlService service = consumerConfig.refer();
        System.out.println(service.insCtrl("127.0.0.1_6604_12058", (byte)2));
    }
    

    @Test
    public void testCustomCallback() throws Exception {
        ConsumerConfig<RegistryCtrlService> consumerConfig;
        consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId("com.ipd.jsf.service.RegistryCtrlService");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("reg");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
//        consumerConfig.setCluster(Constants.CLUSTER_TRANSPORT_PINPOINT);

        List<MethodConfig> methods = new ArrayList<MethodConfig>();
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setName("customCallback");
        methodConfig.setParameter(Constants.HIDDEN_KEY_TOKEN, "xxx");
        methods.add(methodConfig);
        consumerConfig.setMethods(methods);

        RegistryCtrlService service = consumerConfig.refer();
        
        System.out.println(service.customCallback("", new SubscribeUrl()));
    }
    
    @Test
    public void testResetDruidDs() throws Exception {
        ConsumerConfig<RegistryCtrlService> consumerConfig;
        consumerConfig = new ConsumerConfig<RegistryCtrlService>();
        consumerConfig.setInterfaceId("com.ipd.jsf.service.RegistryCtrlService");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("reg");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");

        RegistryCtrlService service = consumerConfig.refer();
        
        System.out.println(service.resetDruidDS());
    }
}
