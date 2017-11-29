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
package com.ipd.jsf.registry.start;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.gd.config.RegistryConfig;
import com.ipd.jsf.gd.registry.RegistryFactory;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.context.RegistryContext;
import com.ipd.jsf.registry.util.RegistryUtil;

public class RegsitryStartup {

	/**
     * 日志
     */
    private static final Logger logger = LoggerFactory.getLogger(RegsitryStartup.class);

    /**
     * @param args
     */
	public static void main(String[] args) {
        try {
        	//设置实例编号，为不同实例加载相应的配置文件 src/main/resources/inst/saf*.properties.  目的是同一台机器可启动多个实例
        	if (System.getProperty(RegistryConstants.SAF_INSTANCE) == null || System.getProperty(RegistryConstants.SAF_INSTANCE).equals("")) {
        		System.setProperty(RegistryConstants.SAF_INSTANCE, RegistryConstants.SAF_INSTANCE_VALUE);
        	}

        	//配置全局的注册中心，手动指定。如果不指定，jsf会自动给出默认注册中心。
            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(RegistryUtil.getRegistryIPPort());
            RegistryFactory.getRegistry(registryConfig);

            new PropertyFactory("global.properties");
            RegistryContext.context = new ClassPathXmlApplicationContext("classpath:/spring/spring-config.xml");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
