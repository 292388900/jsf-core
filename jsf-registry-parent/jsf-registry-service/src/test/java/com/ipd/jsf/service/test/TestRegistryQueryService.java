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

        
        TestRegistryQueryService service = new TestRegistryQueryService();
        service.getInterface();
    }

    public void getInterface() throws Exception {
        ConsumerConfig<RegistryQueryService> consumerConfig = new ConsumerConfig<RegistryQueryService>();
        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Paging page = new Paging();
        page.setPageIndex(1);
        page.setPageSize(10);
        page.getParams().put("interface", "com.ipd.testjsf.HelloBaontService");
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
        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Paging page = new Paging();
        page.setPageIndex(1);
        page.setPageSize(10);
        InstanceResponse ins = service.getInstanceList(page);
        System.out.println(ins.getInstanceList());
        System.out.println(ins.getTotalRecord());
    }
    
    public void getInskey() throws Exception {
        ConsumerConfig<RegistryQueryService> consumerConfig = new ConsumerConfig<RegistryQueryService>();
        consumerConfig.setInterfaceId(RegistryQueryService.class.getName());
        consumerConfig.setAlias("saf");
        consumerConfig.setProtocol("saf");
        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        RegistryQueryService service = consumerConfig.refer();
        Instance vo = service.getInstance("127.0.0.1_4452_92205");
        System.out.println(vo.getProviders());
        System.out.println(vo.getInsKey());
        System.out.println(vo.getConfig());
    }
}
