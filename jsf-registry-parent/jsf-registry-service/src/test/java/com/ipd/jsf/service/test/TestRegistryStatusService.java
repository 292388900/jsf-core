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

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.service.RegistryStatusService;

public class TestRegistryStatusService {

    /**
     * @param args
     * @throws Exception 
     */
    public static void main(String[] args) throws Exception {
        // 服务提供者连接注册中心，设置属性
        ConsumerConfig<GenericService> consumerConfig = new ConsumerConfig<GenericService>();
        consumerConfig.setInterfaceId(RegistryStatusService.class.getName());
//      consumerConfig.setRegistry(registries);
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("reg");
//        consumerConfig.setUrl("saf://127.0.0.1:40660");
        consumerConfig.setUrl("jsf://10.12.122.28:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        
        consumerConfig.setGeneric(true);
        
        
        GenericService service = (GenericService)consumerConfig.refer();
        System.out.println(service.$invoke("stat", null, null));
    }

}
