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
package com.ipd.jsf.worker.dao;

import com.ipd.jsf.worker.domain.JsfCheckHistory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring-database.xml" })
public class JsfCheckHistoryDaoTest {

    @Autowired
    private JsfCheckHistoryDao jsfCheckHistoryDao;

    @Test
    public void testInsert() throws Exception {
        JsfCheckHistory history = new JsfCheckHistory();
        history.setSrcIP("172.1.1.1");
        history.setSrcRoom("yz");
        history.setDstIP("172.2.2.2");
        history.setDstPort(44444);
        history.setDstRoom("lf");
        history.setResultCode(1);
        history.setResultMessage("adasdsadsadsad");
        history.setCheckTime(new Date());
        history.setCreateTime(new Date());
        jsfCheckHistoryDao.insert(history);
    }

    @Test
    public void testBatchInsert() throws Exception {
        List<JsfCheckHistory> list = new ArrayList<JsfCheckHistory>();
        for (int i = 1; i < 10; i++) {
            JsfCheckHistory history = new JsfCheckHistory();
            history.setSrcIP("172.3.1." + i);
            history.setSrcRoom("yz");
            history.setDstIP("172.4.2." + i);
            history.setDstPort(44444);
            history.setDstRoom("lf");
            history.setResultCode(1);
            history.setResultMessage("adasdsadsadsad");
            history.setCheckTime(new Date());
            history.setCreateTime(new Date());
            list.add(history);
        }
        jsfCheckHistoryDao.batchInsert(list);
    }

    @Test
    public void testDeleteByTime() throws Exception {
        jsfCheckHistoryDao.deleteByTime(new Date(System.currentTimeMillis() - 7 * 86400000));
    }
}