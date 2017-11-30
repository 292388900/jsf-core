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

import java.util.Date;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.dao.ClientDao;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestClientDao extends ServiceBaseTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    public void testUpdate() {
        Client client = new Client();
        client.setUniqKey("127.0.0.1;154124;SAF_0.0.1;1;2074");
        client.setInsKey("");
        client.setUpdateTime(new Date());
        try {
            System.out.println(clientDao.update(client));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println(new Date(1454516640839l));

    }

}
