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
package com.ipd.jsf.sqllite.test;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.sqllite.test.dao.TestDao;
import com.ipd.jsf.sqllite.test.domain.TestObj;

public class TestTestObj extends ServiceBaseTest {
    @Autowired
    private TestDao testDao;
    
//    @Test
    public void create() {
        try {
            testDao.create();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    @Test
    public void insert() {

        TestObj obj = new TestObj();
        obj.setName("baont");
        obj.setOccupation("soft engineer");
        try {
            System.out.println(testDao.insert(obj));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void list() {

        try {
            List<TestObj> list = testDao.list();
            System.out.println(list);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {

    }

}
