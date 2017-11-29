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
package com.ipd.jsf.registry.manager.test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.manager.IpwbManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestIpwbManager extends ServiceBaseTest {

    private static Logger logger = LoggerFactory.getLogger(TestIpwbManager.class);

    @Autowired
    private IpwbManager ipwbManager;

    @Test
    public void getList() throws Exception {
        try {
            
//            Map<String, Map<String, List<Ipwblist>>> result = ipwbManager.getListByInterfaceIdList(list);
//            logger.info("size:{}, result:{}", result.size(), result.toString());
        } catch (Exception e) {
            logger.error("ip getlist error: ", e);
        }
    }

}
