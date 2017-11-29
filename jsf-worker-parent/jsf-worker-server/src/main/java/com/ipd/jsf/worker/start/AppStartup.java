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
package com.ipd.jsf.worker.start;

import com.ipd.jsf.worker.common.BeanFactory;
import com.ipd.jsf.worker.common.PropertyFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.Map;
import java.util.Properties;

/**
 * 管理worker的jsf服务
 * 
 */
public class AppStartup {
    private static final Logger logger = LoggerFactory.getLogger(AppStartup.class);

    public static void main(String[] args) {
    	try {
	        new PropertyFactory("global.properties");

	        // TODO for test 打印下自动部署的配置参数 
	        Properties properties = System.getProperties();
	        for (Map.Entry<Object, Object> property : properties.entrySet()) {
	            logger.info("property {} : {}", property.getKey(), property.getValue());
	        }
	        Map<String, String> envs = System.getenv();
	        for (Map.Entry<String, String> env : envs.entrySet()) {
	            logger.info("env {} : {}", env.getKey(), env.getValue());
	        }
	        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("/spring/spring-config.xml");
	        BeanFactory.setApplicationContext(context);
            Thread.sleep(10000);
        } catch (Exception e) {
            logger.error("worker start error:" + e.getMessage(), e);
        }
    }
}
