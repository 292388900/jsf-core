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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.manager.InterfaceManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestInterfaceManager extends ServiceBaseTest {

    private static Logger logger = LoggerFactory.getLogger(TestInterfaceManager.class);
    
    @Autowired
    InterfaceManager interfaceManagerImpl;

    @Test
    public void get1() throws Exception {
        InterfaceInfo i = interfaceManagerImpl.getByName("com.ipd.saf.service.RegistryService");
        logger.info("name:{}, id:{} ", i.getInterfaceName(), i.getInterfaceId());
        Assert.assertNotNull(i);
    }

    @Test
    public void get2() throws Exception {
        InterfaceInfo i = interfaceManagerImpl.getByName("jingdong123456");
        if (i != null) {
            logger.info("name:{}, id:{} ", i.getInterfaceName(), i.getInterfaceId());
        } else {
            logger.info("interface is null");
        }
        Assert.assertNull(i);
    }
    
    @Test
    public void getListForProvider() throws Exception {
        long start = System.currentTimeMillis();
        List<InterfaceInfo> list = interfaceManagerImpl.getListForProvider();
        long end = System.currentTimeMillis();
        if (list != null) {
            logger.info("interface total count: {},   elapse: {}ms" , list.size(), (end - start));
        }
        Assert.assertNotNull(list);
    }
}
