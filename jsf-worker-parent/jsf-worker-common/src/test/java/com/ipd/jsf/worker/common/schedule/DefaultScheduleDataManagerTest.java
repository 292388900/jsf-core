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
package com.ipd.jsf.worker.common.schedule;

import com.ipd.jsf.worker.common.ScheduleDataManager;
import com.ipd.jsf.zookeeper.ZkClient;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Properties;

public class DefaultScheduleDataManagerTest {

    private ZkClient zkClient;

    private ScheduleDataManager scheduleDataManager;

    private Properties zkConfig;

    @Before
    public void setUp() throws Exception {
        try {
            zkConfig = new Properties();
            zkConfig.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("worker.properties"));
            zkClient = new ZkClient(zkConfig.getProperty("zk.address"),Long.valueOf(zkConfig.getProperty("zk.connectionTimeout","10000")),
                    Integer.valueOf(zkConfig.getProperty("zk.sessionTimeout","30000")));
        } catch (IOException e) {
            return;
        }
        scheduleDataManager = new DefaultScheduleDataManager(zkClient,null);
    }

    @Test
    public void testFixMaster() throws Exception {
        //scheduleDataManager.fixMaster("demoWorker","10104$10.12.113.39$F1DA79AC3A424ACB8E6401CDC25BEB99");
//        scheduleDataManager.fixMaster("demoWorker","5480$10.12.113.55$5F7F50FBDC1E4967B57D1630FF6E6A79");

    }

    public Properties getZkConfig() {
        return zkConfig;
    }

}
