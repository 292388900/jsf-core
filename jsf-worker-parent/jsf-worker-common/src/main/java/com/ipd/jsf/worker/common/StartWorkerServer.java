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
package com.ipd.jsf.worker.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class StartWorkerServer {

    private static final Logger logger = LoggerFactory.getLogger(StartWorkerServer.class);

    public static void main(String[] args){
        ClassPathXmlApplicationContext cac = new ClassPathXmlApplicationContext("classpath*:worker.xml","classpath*:spring/spring-worker-db.xml");
        cac.start();
        /*GenericApplicationContext gac = new GenericApplicationContext();
        XmlBeanDefinitionReader xmlReader = new XmlBeanDefinitionReader(gac);
        xmlReader.loadBeanDefinitions(new ClassPathResource("worker.xml"));
        xmlReader.loadBeanDefinitions(new ClassPathResource("spring/spring-worker-db.xml"));
//        PropertiesBeanDefinitionReader propReader = new PropertiesBeanDefinitionReader(gac);
//        propReader.loadBeanDefinitions(new ClassPathResource("db.properties"));
        gac.refresh();*/
        logger.info("Worker Server服务启动完成");
        // 启动本地服务，然后hold住本地服务
        synchronized (StartWorkerServer.class) {
            while (true) {
                try {
                    StartWorkerServer.class.wait();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
