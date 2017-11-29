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
package com.ipd.testsaf;

import java.io.File;
import java.util.Random;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestMapDB {
    private final static AtomicInteger cnt = new AtomicInteger(0);
    private static Logger logger = LoggerFactory.getLogger(RegistryServiceTest.class);
    /**
     * @param args
     */
    public static void main(String[] args) {
        final TestMapDB main = new TestMapDB();
        int thread = 2;
        ExecutorService service = Executors.newFixedThreadPool(200);
        long start = System.currentTimeMillis();
        final DB db = DBMaker.newFileDB(new File("D:/mapdb/testdb")).closeOnJvmShutdown().make();
        for (int i = 0; i < thread; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            main.test(db);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        
        Thread thread1 = new Thread(new Runnable() {
            private long last = 0;
            @Override
            public void run() {
                while (true) {
                    long count = cnt.get();
                    long tps = count - last;
                    System.out.println("last 1s invoke: "+ tps);
                    last = count;

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        },"Print-tps-THREAD");
        thread1.start();
        
        logger.info("elapse:{}", (System.currentTimeMillis() - start));


    }

    /**
     * 测试provider注册
     */
    public void test(DB db) {

        
                //create new collection (or open existing)
                ConcurrentNavigableMap map = db.getTreeMap("collectionName");
                map.put(1,"one");
                map.put(new Random().nextInt(25500),"bnt_test" + new Random().nextInt(25005));
                //persist changes into disk, there is also rollback() method
                db.commit();
//                db.close();
                cnt.incrementAndGet();
    }

}
