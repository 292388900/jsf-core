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
package com.ipd.jsf.worker.service.test.dao;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.dao.InterfaceInfoDao;
import com.ipd.jsf.worker.domain.InterfaceInfo;


public class TestInterfaceDao extends ServiceBaseTest {
    @Autowired
    private InterfaceInfoDao interfaceInfoDao;

//    @Test
    public void test() {
        try {
            InterfaceInfo i = new InterfaceInfo();
            i.setInterfaceName("unit.test.001");
            i.setImportant((byte)1);
            i.setRemark("test");
            interfaceInfoDao.create(i);
            System.out.println(i.getInterfaceId());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @Test
    public void testGetAlarmErps(){
    	
    	List<String> erpList = this.interfaceInfoDao.getAlarmErps("com.ipd.testjsf.HelloService");
    	for(String erp : erpList){
    		System.out.println(erp);
    	}
    }
}
