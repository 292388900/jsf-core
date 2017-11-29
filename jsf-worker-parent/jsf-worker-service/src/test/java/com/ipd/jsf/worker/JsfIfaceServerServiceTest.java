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
package com.ipd.jsf.worker;

import java.util.List;

import com.ipd.jsf.worker.service.JsfIfaceServerService;
import com.ipd.jsf.worker.service.vo.JSONResult;
import junit.framework.TestCase;

import com.alibaba.fastjson.JSON;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.config.RegistryConfig;


public class JsfIfaceServerServiceTest extends TestCase {

	public static void main(String[] args) throws Exception {
		RegistryConfig rgistryConfig = new RegistryConfig();
		rgistryConfig.setIndex("test.i.jsf.ipd.com");
		
		ConsumerConfig<JsfIfaceServerService> consumerConfig = new ConsumerConfig<JsfIfaceServerService>();
		consumerConfig.setInterfaceId(JsfIfaceServerService.class.getCanonicalName());
		consumerConfig.setAlias("jsf_jos");
		consumerConfig.setRegister(true);
		consumerConfig.setRegistry(rgistryConfig);
		consumerConfig.setParameter(".token", "jos_call");
		consumerConfig.setTimeout(1000000);
		
		JsfIfaceServerService jsfService = consumerConfig.refer();
		
		for(int i=0; i < 10; i++){
			String result = jsfService.getServiceInfo("bjyfwutao", "com.ipd.demoapp.service.SimpleService");
			JSONResult r = JSON.parseObject(result, JSONResult.class);
			System.out.println(r.getResult());
		}
		
		List<String> interfaceList = jsfService.getInterfacesWithErp("xxxx");
		for(String interfaceName : interfaceList){
			System.out.println(interfaceName);
		}
	} 
}
