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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.dao.IpwblistDao;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

/**
 * 获取黑白名单
 */
public class TestIpwblistDao extends ServiceBaseTest {

    @Autowired
    private IpwblistDao ipwblistDao;

    @Test
    public void getList() {
        try {
//            List<Ipwblist> list = ipwblistDao.getIpwblistAfterTime(new Date());
//            System.out.println(list.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
