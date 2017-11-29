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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.JsfIns;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.dao.JsfInsDao;


public class TestSafInsDao extends ServiceBaseTest {
    @Autowired
    private JsfInsDao safInsDao;

    @Test
    public void test() {
        try {
            List<JsfIns> offlist = safInsDao.getOfflineInsBeforeTime(new Date(), null);
            System.out.println(offlist.size());
            List<JsfIns> onlist = safInsDao.getOnlineInsBeforeTime(new Date(), null);
            System.out.println(onlist.size());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Test
    public void test2() {
        try {
            List<JsfIns> list = safInsDao.getOnlineInsBeforeTime(new Date(), null);
            System.out.println(list.size());
            list = safInsDao.getOfflineInsAfterTime(new Date());
            System.out.println(list.size());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
