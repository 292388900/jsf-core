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

import java.util.HashMap;
import java.util.Map;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.service.RegistryHttpService;
import com.ipd.jsf.vo.LookupParam;
import com.ipd.jsf.vo.LookupResult;

public class TestRegistrySimpleService {

    /**
     * @param args
     */
    public static void main(String[] args) {
        TestRegistrySimpleService service = new TestRegistrySimpleService();
        service.testLookup2();
    }

    private void testLookup2() {
        ConsumerConfig<RegistryHttpService> consumerConfig = new ConsumerConfig<RegistryHttpService>();
        consumerConfig.setInterfaceId(RegistryHttpService.class.getName());
        consumerConfig.setAlias("reg");
        consumerConfig.setProtocol("jsf");
        consumerConfig.setUrl("jsf://127.0.0.1:40660");
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
        consumerConfig.setParameter(".token", "1qaz2wsx");
        Map<String, String> map = new HashMap<String, String>();
        map.put("visitname", "tests12");
        LookupParam param = new LookupParam();
        param.setAlias("baont");
        param.setDataVer(0);
        param.setIface("com.ipd.testjsf.HelloBaontService");
        param.setIp("127.0.0.1");
        param.setProtocol(1);
        param.setInsKey("127.0.0.1_123_12345");
        param.setAttrs(map);
        RegistryHttpService registrySimpleService = consumerConfig.refer();

        
        while(true) {
        	LookupResult result = registrySimpleService.lookup(param);
        	try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	System.out.println(result.toString());
        }
    }
}
