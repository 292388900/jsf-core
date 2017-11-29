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

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.dao.ServerDao;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.Server;


public class TestServerDao extends ServiceBaseTest {
    @Autowired
    private ServerDao serverDao;
    
//    @Test
    public void test() {
        try {
            List<JsfIns> insList = new ArrayList<JsfIns>();
            JsfIns ins = new JsfIns();
            ins.setIp("192.168.75.10");
            ins.setPid(12345);
            insList.add(ins);
            
            List<Server> list = serverDao.getOnlineServersByIns(insList);
            System.out.println(list.size());
            List<Server> list1 = serverDao.getOfflineServersByIns(insList);
            System.out.println(list1.size());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

//    @Test
    public void test1() {
        try {
            int i = serverDao.updateStatusOnline(11);
            
            System.out.println(i);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
//    @Test
    public void test2() {
        try {
            int i = serverDao.updateStatusOffline(11);
            
            System.out.println(i);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test4() {
        try {
            List<Server> i = serverDao.getListByInterfaceName("com.ipd.saf.service.RegistryService");
            for (Server server : i) {
                System.out.println(server.getStartTime());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
