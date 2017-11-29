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
package com.ipd.jsf.worker.common.utils;

import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.zookeeper.ZkClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;


public class ZkClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(ZkClientUtil.class);

    private static ZkClient zkClient;

    private static PropertyFactory propertyFactory;

    static {
        try {
            propertyFactory = new PropertyFactory("worker.properties");
            zkClient = new ZkClient((String) propertyFactory.getProperty("zk.address"),Long.valueOf(propertyFactory.getProperty("zk.connectionTimeout","10000")),
                    Integer.valueOf(propertyFactory.getProperty("zk.sessionTimeout","30000")));
        } catch (IOException e) {
            logger.error("create zkClient error",e);
        }
    }

    public static ZkClient getZkClient(){
        return zkClient;
    }
}
