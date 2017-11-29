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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.cache.AppCache;
import com.ipd.jsf.registry.cache.AppInsCache;
import com.ipd.jsf.registry.dao.JsfInsDao;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.manager.SafInsManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestSafInsManager extends ServiceBaseTest {

    private static Logger logger = LoggerFactory.getLogger(TestSafInsManager.class);
    
    @Autowired
    SafInsManager safInsManagerImpl;
    
    @Autowired
    JsfInsDao safInsDao;

    @Autowired
    AppCache appCache;
    
    @Autowired
    AppInsCache appInsCache;

//    @Test
    public void create() throws Exception {
        String regIp = "192.168.75.10";
        for (int i = 0; i < 100; i++) {
            String ip = "192.168.1." + (new Random()).nextInt(10);
            int pid = (new Random()).nextInt(100);
            Date startTime = new Date();
            JsfIns ins = new JsfIns();
            ins.setIp(ip);
            ins.setLanguage("java");
            ins.setPid(pid);
            ins.setPort(49901);
            ins.setSafVer(210);
            ins.setStartTime(startTime.getTime());
            ins.setInsKey(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
            ins.setRegIp(regIp);
            long start = System.currentTimeMillis();
            int result = safInsManagerImpl.register(ins);
            long end = System.currentTimeMillis();
            logger.info("create   ---   result: {},   elapse: {}ms" , result, (end - start));
            Assert.assertNotSame(0, result);
        }
    }
    
    @Test
    public void create1() throws Exception {
    	appCache.refreshCache();
    	appInsCache.refreshCache();
    	String regIp = "192.168.75.10";
		String ip = "192.168.1.123";
		int pid = 12345;
		Date startTime = new Date();
		JsfIns ins = new JsfIns();
		ins.setIp(ip);
		ins.setLanguage("java");
		ins.setPid(pid);
		ins.setPort(49901);
		ins.setSafVer(210);
		ins.setStartTime(startTime.getTime());
		ins.setInsKey(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
		ins.setRegIp(regIp);
		ins.setAppId(30011);
		ins.setAppInsId("I500001");
		ins.setAppName("baonttest");
        ins.setHb(new Date());
        ins.setCreateTime(new Date());
		long start = System.currentTimeMillis();
		int result = safInsManagerImpl.register(ins);
		long end = System.currentTimeMillis();
		logger.info("create   ---   result: {},   elapse: {}ms" , result, (end - start));
		Assert.assertNotSame(0, result);
    }

//    @Test
//    public void updateList() throws Exception {
//        String regIp = "192.168.75.10";
//        List<String> list = mockList();
//        long start = System.currentTimeMillis();
//        int result = safInsManagerImpl.batchUpdateHb(list, new Date(), regIp);
//        long end = System.currentTimeMillis();
//        logger.info("getList   ---   result: {},   elapse: {}ms" , result, (end - start));
//        Assert.assertNotSame(0, result);
//    }
    
    private List<String> mockList() {
        List<String> list = new ArrayList<String>();
        for (int i = 0 ; i < 1501; i++) {
            String ip = "192.168.1." + (new Random()).nextInt(10);
            int pid = (new Random()).nextInt(100);
            Date startTime = new Date();
            list.add(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
        }
        return list;
    }
    
//    @Test
//    public void testBatchUpdateHb() throws Exception {
//        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();
//        ExecutorService t = new ThreadPoolExecutor(100, 100, 0, TimeUnit.MILLISECONDS, queue);
//        
//        for (int n = 0; n < 10000; n++) {
//            final int k = n;
//            t.execute(new Runnable() {
//                @Override
//                public void run() {
//                    List<String> list = new ArrayList<String>();
//                    for (int i = 0; i < 1000; i++) {
//                        String ip = "192.168." + (k % 255) + "." + (i % 255);
//                        int pid = i * 20;
//                        Date startTime = new Date();
//                        list.add(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
//                    }
//                    try {
//                        safInsManagerImpl.batchUpdateHb(list, new Date(), "172.16.10.1");
//                        System.out.println("complete!!! -- " + k);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            });
//        }
//        Thread.sleep(50000);
//    }

    /**
     * 批量插入数据，用来模拟心跳表的数据
     * @throws Exception
     */
//    @Test
    public void batchCreate() throws Exception {
        ExecutorService muti = Executors.newFixedThreadPool(60);
        for (int i = 0; i < 100000; i++) {
            muti.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        String regIp = "192.168.75.10";
                        String ip = "192.168.100." + (new Random()).nextInt(255);
                        int pid = (new Random()).nextInt(100);
                        Date startTime = new Date();
                        JsfIns ins = new JsfIns();
                        ins.setIp(ip);
                        ins.setLanguage("java");
                        ins.setPid(pid);
                        ins.setPort(49901);
                        ins.setSafVer(210);
                        ins.setHb(startTime);
                        ins.setStartTime(startTime.getTime());
                        ins.setInsKey(UniqkeyUtil.getInsKey(ip, pid, startTime.getTime()));
                        ins.setRegIp(regIp);
                        long start = System.currentTimeMillis();
                        int result = safInsManagerImpl.register(ins);
                        long end = System.currentTimeMillis();
                        logger.info("create   ---   result: {},   elapse: {}ms" , result, (end - start));
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
           
        }
        Thread.sleep(600000);
    }

//    @Test
//    public void updateList1() throws Exception {
//        try {
//            String regIp = "192.168.75.10";
////            List<SafIns> list = safInsDao.getList();
//            //TODO 需要加getList才能测试
//            List<SafIns> list = new ArrayList<SafIns>();
//            List<String> strList = new ArrayList<String>();
//            for (SafIns ins : list) {
//                strList.add(ins.getInsKey());
//            }
//            long start = System.currentTimeMillis();
//            int result = safInsManagerImpl.batchUpdateHb(strList, new Date(), regIp);
//            long end = System.currentTimeMillis();
//            logger.info("getList   ---   result: {},   elapse: {}ms" , result, (end - start));
//            Assert.assertNotSame(0, result);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
