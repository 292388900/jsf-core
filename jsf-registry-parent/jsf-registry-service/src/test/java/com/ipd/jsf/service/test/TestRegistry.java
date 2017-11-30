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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;
import com.ipd.jsf.vo.SubscribeUrl;
import com.ipd.testsaf.TestHelloService;

public class TestRegistry extends ServiceBaseTest {
    private static Logger logger = LoggerFactory.getLogger(TestRegistry.class);
    @Autowired
    RegistryService registryService;
    
    @Autowired
    SubscribeService subscribeServiceImpl;
    
    @Before
    public void init(){
    	try {
			subscribeServiceImpl.refreshCache(10);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    @Test
    public void testRegistry() {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("127.0.0.1");
        safUrl.setPort(40000);
        safUrl.setAlias("test");
        safUrl.setPid(12345);
        safUrl.setStTime((new Date()).getTime());
        safUrl.setIface(TestHelloService.class.getName());
        safUrl.setProtocol(ProtocolType.jsf.value());
        safUrl.setRandom(false);
        Map<String, String> attrs = new HashMap<String, String>();
        attrs.put(RegistryConstants.APPPATH, "/export/server/tomcat/web/app");
        attrs.put(RegistryConstants.LANGUAGE, "java");
        attrs.put(RegistryConstants.SAFVERSION, String.valueOf(RegistryConstants.SAFVERSION_VALUE));
        attrs.put(RegistryConstants.WEIGHT, "10000");
        safUrl.setAttrs(attrs);

        String insKey = "";
        try {
            JsfUrl result = registryService.doRegister(safUrl);
            insKey = result.getInsKey();
            logger.info("registry insKey:" + insKey);
        } catch (Exception e) {
            logger.error("registry insKey:" + insKey, e);
        }

    }
    
    @Test
    public void testScribe(){
//    	 List<SafUrl> doSubscribe(SafUrl safUrl, Callback<SubscribeUrl> subscribeData);
    	//请测试前在doSubscribe方法中设置client ip
    	 JsfUrl safUrl = new JsfUrl();
    	 safUrl.setInsKey("1025");
    	 safUrl.setIface("com.ztesoft.zsmart.bss.jd.interfaces.api.ValueAddedCharge");
    	 safUrl.setAlias("");
    	 safUrl.setProtocol(0);

//    	 SafRegistryCallback callback = new SafRegistryCallback();
//    	 List<SafUrl> list = registryService.doSubscribe(safUrl, callback);
//    	 System.out.println("testScribe>>>>>>>>>>>>>>>>" + list.size());
    }
    
    @Test
    public void testLookup(){
    	//请测试前在lookup方法中设置client ip
    	JsfUrl safUrl = new JsfUrl();
	   	 safUrl.setInsKey("1025");
	   	 safUrl.setIface("com.ztesoft.zsmart.bss.jd.interfaces.api.ValueAddedCharge");
	   	 safUrl.setAlias("");
	   	 safUrl.setProtocol(0);
	   	 
	   	SubscribeUrl url = registryService.lookup(safUrl);
	   	System.out.println("testLookup>>>>>>>>>>>>>>>>" + url.getProviderList().size());
    }
}
