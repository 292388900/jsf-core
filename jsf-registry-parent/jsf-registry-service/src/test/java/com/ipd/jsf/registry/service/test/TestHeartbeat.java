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
package com.ipd.jsf.registry.service.test;

import java.util.Date;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.service.HeartbeatService;


public class TestHeartbeat extends ServiceBaseTest {
    private static Logger logger = LoggerFactory.getLogger(TestHeartbeat.class);
    private Date startTime = new Date();

    @Autowired
    private HeartbeatService heartbeatService;

    @Test
    public void test() {
        try {
//            testSaveIns();
            for (int n = 0; n < 10; n++) {
                testPutHbCache1();
//                testSaveHb();
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void testSaveIns() throws Exception {
        String regIp = "127.0.0.1";
        for (int i = 0; i < 1000; i++) {
            String ip = "127.0.0." + i%255;
            int pid = i * 10;
            
            JsfIns ins = new JsfIns();
            ins.setIp(ip);
            ins.setLanguage("java");
            ins.setPid(pid);
            ins.setPort(49900);
            ins.setSafVer(210);
            ins.setHb(new Date());
            ins.setStartTime(startTime.getTime());
            ins.setInsKey(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
            ins.setRegIp(regIp);
            long start = System.currentTimeMillis();
            heartbeatService.register(ins);
            long end = System.currentTimeMillis();
            logger.info("saveIns   ---   elapse: {}ms" , (end - start));
        }
    }

    private void testPutHbCache() throws Exception {
        for (int n = 0; n < 15; n++) {
            for (int i = 0; i < 1000; i++) {
                String ip = "127.0.0." + i%255;
                int pid = i * 10;
                String insKey = UniqkeyUtil.getInsKey(ip, pid, startTime.getTime());
                heartbeatService.putHbCache(insKey);
            }
            Thread.sleep(800);
        }
        logger.info("putHbCache ");
    }

    private void testPutHbCache1() throws Exception {
        for (int t = 0; t < 100; t++) {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while(true) {
                        for (int i = 0; i < 1000; i++) {
                            String ip = "127.0.0." + i%255;
                            int pid = i * 10;
                            String insKey = UniqkeyUtil.getInsKey(ip, pid, startTime.getTime());
                            try {
                                long start = System.currentTimeMillis();
                                heartbeatService.putHbCache(insKey);
                                long end = System.currentTimeMillis();
                                logger.info("testPutHbCache1   ---   elapse: {}ms" , (end - start));
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
            thread.start();
        }
        logger.info("putHbCache ");
    }
}
