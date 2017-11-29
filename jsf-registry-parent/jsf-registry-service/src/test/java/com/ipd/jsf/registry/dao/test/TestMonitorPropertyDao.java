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
package com.ipd.jsf.registry.dao.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.IfacePropertyType;
import com.ipd.jsf.registry.dao.InterfacePropertyDao;
import com.ipd.jsf.registry.domain.InterfaceProperty;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestMonitorPropertyDao extends ServiceBaseTest {

    @Autowired
    private InterfacePropertyDao interfacePropertyDao;

    @Test
    public void testList() {
        List<InterfaceProperty> list;
        try {
            List<Integer> ifaceIdList = new ArrayList<Integer>();
            ifaceIdList.add(1);
            list = interfacePropertyDao.getListByInterfaceIdList(IfacePropertyType.MONITOR.value(), ifaceIdList);
            System.out.println(list.size());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
