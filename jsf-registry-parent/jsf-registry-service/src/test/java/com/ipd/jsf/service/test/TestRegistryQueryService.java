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

import java.util.List;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.service.RegistryQueryService;
import com.ipd.jsf.service.vo.Instance;
import com.ipd.jsf.service.vo.InstanceResponse;
import com.ipd.jsf.service.vo.InterfaceInfoVo;
import com.ipd.jsf.service.vo.Paging;
import com.ipd.jsf.service.vo.Provider;

public class TestRegistryQueryService {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        // 服务提供者连接注册中心，设置属性
//        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
//        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
//        consumerConfig.setProtocol("saf");
//        consumerConfig.setAlias("saf");
////        consumerConfig.setUrl("saf://127.0.0.1:40660");
//        consumerConfig.setUrl("saf://192.168.209.74:40660");
//        consumerConfig.setRegister(false);//打开注释表示不走注册中心
//        consumerConfig.setGeneric(true);
//        GenericService service = (GenericService)consumerConfig.refer();
//        String[] paramType = {"String"};
//        Object[] object = {"com.ipd.testsaf.HelloService"};
//        System.out.println(service.$invoke("getProviders", paramType, object));
        
        
        TestRegistryQueryService service = new TestRegistryQueryService();
        service.getInterface();
//        service.getInskeyList();
//        service.getInskey();
    }

    public void getInterface() throws Exception {
        ConsumerConfig<RegistryQueryService> consumerConfig = new ConsumerConfig<RegistryQueryService>();
        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
//        consumerConfig.setUrl("jsf://192.168.151.142:40660");
        consumerConfig.setUrl("jsf://10.12.165.67:40660");
//        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Paging page = new Paging();
        page.setPageIndex(1);
        page.setPageSize(10);
        page.getParams().put("interface", "com.ipd.testjsf.HelloBaontService");
//        page.getParams().put("ip", "10.12.104.232");
        page.getParams().put("alias", "baont");
        page.getParams().put("protocol", "1");
        InterfaceInfoVo vo = service.getProviders(page);
        List<Provider> providers = vo.getProviders();
        System.out.println(providers);
        System.out.println(vo.getConfigs());
    }

    public void getInskeyList() throws Exception {
        ConsumerConfig<RegistryQueryService> consumerConfig = new ConsumerConfig<RegistryQueryService>();
        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
        consumerConfig.setAlias("saf");
        consumerConfig.setProtocol("saf");
//        consumerConfig.setUrl("saf://192.168.209.74:40660");
        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Paging page = new Paging();
        page.setPageIndex(1);
        page.setPageSize(10);
//        page.getParams().put("inskey", "10.12.122.28_7528_19434");
        InstanceResponse ins = service.getInstanceList(page);
        System.out.println(ins.getInstanceList());
        System.out.println(ins.getTotalRecord());
    }
    
    public void getInskey() throws Exception {
        ConsumerConfig<RegistryQueryService> consumerConfig = new ConsumerConfig<RegistryQueryService>();
        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
        consumerConfig.setAlias("saf");
        consumerConfig.setProtocol("saf");
//        consumerConfig.setUrl("saf://192.168.209.74:40660");
        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Instance vo = service.getInstance("10.12.122.28_4452_92205");
        System.out.println(vo.getProviders());
        System.out.println(vo.getInsKey());
        System.out.println(vo.getConfig());
    }
}
