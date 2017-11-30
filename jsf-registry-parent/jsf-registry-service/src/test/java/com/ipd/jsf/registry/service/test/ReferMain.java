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
package com.ipd.jsf.registry.service.test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.service.RegistryService;
import com.ipd.jsf.vo.JsfUrl;

public class ReferMain {
	
	private static Logger logger = LoggerFactory.getLogger(ReferMain.class);

	/**
	 * Method Name main 
	 * @param args
	 * Return Type void
	 */
	public static void main(String[] args) throws Exception{
		logger.info("Test Main");
		ReferMain refer = new ReferMain();
		ClassPathXmlApplicationContext appContext = new ClassPathXmlApplicationContext("/saf-consumer.xml");
        final RegistryService messageSender = (RegistryService)appContext.getBean("registryService");
        refer.register(messageSender);
	}

    private String register(RegistryService messageSender) {
        JsfUrl safUrl = new JsfUrl();
        safUrl.setIp("127.0.0.1");
        safUrl.setPort(40000);
        safUrl.setAlias("test");
        safUrl.setPid(12345);
        safUrl.setStTime((new Date()).getTime());
        safUrl.setIface(RegistryUtil.getRegistryIfaceName());
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
            JsfUrl result = messageSender.doRegister(safUrl);
            insKey = result.getInsKey();
            logger.info("registry insKey:" + insKey);
        } catch (Exception e) {
            logger.error("registry insKey:" + insKey, e);
        }

        return insKey;
    }

}
