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
package com.ipd.jsf.bdb.test;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class BDBTest {
    private static Logger logger = LoggerFactory.getLogger(BDBTest.class);
    private final static AtomicInteger cnt = new AtomicInteger(0);
    private static Database bdb; // 数据源
    private static Environment exampleEnv;// 环境对象
    private static boolean isrunning = false;// 判断是否运行
 
    /**
     * 打开数据库方法
     */
    public static void start(String path) {
        if (isrunning) {
            return;
        }
        /******************** 文件处理 ***********************/
        File envDir = new File(path);// 操作文件
        if (!envDir.exists())// 判断文件路径是否存在，不存在则创建
        {
            envDir.mkdir();// 创建
        }
 
        /******************** 环境配置 ***********************/
        EnvironmentConfig envConfig = new EnvironmentConfig();
        envConfig.setTransactional(false); // 不进行事务处理
        envConfig.setAllowCreate(true); // 如果不存在则创建一个
        exampleEnv = new Environment(envDir, envConfig);// 通过路径，设置属性进行创建
 
        /******************* 创建适配器对象 ******************/
        DatabaseConfig dbConfig = new DatabaseConfig();
        dbConfig.setTransactional(false); // 不进行事务处理
        dbConfig.setAllowCreate(true);// 如果不存在则创建一个
        dbConfig.setSortedDuplicates(true);// 数据分类
 
        bdb = exampleEnv.openDatabase(null, "simpleDb", dbConfig); // 使用适配器打开数据库
        isrunning = true; // 设定是否运行
    }
 
    /**
     * 关闭数据库方法
     */
    public static void stop() {
        if (isrunning) {
            isrunning = false;
            bdb.close();
            exampleEnv.close();
        }
    }
 
    public static boolean isrunning() {
        return isrunning;
    }
 
    /**
     * 数据存储方法 set(Here describes this method function with a few words)
     * 
     * TODO(Here describes this method to be suitable the condition - to be
     * possible to elect)
     * 
     * @param key
     * @param data
     * 
     *            void
     */
    public static void set(byte[] key, byte[] data) {
        DatabaseEntry keyEntry = new DatabaseEntry();
        DatabaseEntry dataEntry = new DatabaseEntry();
        keyEntry.setData(key); // 存储数据
        dataEntry.setData(data);
 
        OperationStatus status = bdb.put(null, keyEntry, dataEntry);// 持久化数据
 
        if (status != OperationStatus.SUCCESS) {
            throw new RuntimeException("Data insertion got status " + status);
        }
    }

    /*
     * 执行获取,根据key值获取
     */
    public static void selectByKey(String aKey) {
        DatabaseEntry theKey =null;
        DatabaseEntry theData = new DatabaseEntry();
        try {
             theKey = new DatabaseEntry(aKey.getBytes("utf-8"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
     
         
        if (bdb.get(null,theKey, theData,
                LockMode.DEFAULT) == OperationStatus.SUCCESS) { //根据key值，进行数据查询
            // Recreate the data String.
            byte[] retData = theData.getData();
            String foundData = new String(retData);
            System.out.println("For key: '" + aKey + "' found data: '"
                    + foundData + "'.");
        }
         
    }
     
     
    /**
     * 查询所有，可遍历数据
     * selectAll(Here describes this method function with a few words)   
     * 
     * TODO(Here describes this method to be suitable the condition - to be possible to elect)
     *  
     * 
     * void
     */
    public static void selectAll() {
        Cursor cursor = null;
        cursor=bdb.openCursor(null, null);
        DatabaseEntry theKey=null;
        DatabaseEntry theData=null;     
        theKey = new DatabaseEntry();
        theData = new DatabaseEntry();
         
        while (cursor.getNext(theKey, theData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
            System.out.println(new String(theData.getData()));
        }
        cursor.close();
         
    }
     
     
    /**
     *  删除方法
     * delete(Here describes this method function with a few words)   
     * 
     * TODO(Here describes this method to be suitable the condition - to be possible to elect)
     * 
     * @param key 
     * 
     * void
     */
    public static void delete(String key) {
        DatabaseEntry keyEntry =null;
        try {
            keyEntry = new DatabaseEntry(key.getBytes("utf-8"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        bdb.delete(null, keyEntry);
    }
    
    public static void main(String[] args) {
        BDBTest.start("J:/bdb");
        int thread = 2;
        ExecutorService service = Executors.newFixedThreadPool(thread);
        long start = System.currentTimeMillis();
        for (int i = 0; i < thread; i++) {
            service.execute(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            String key = String.valueOf(System.currentTimeMillis());
                            BDBTest.set(key.getBytes(), ("abc"+String.valueOf(System.currentTimeMillis())).getBytes());
                            cnt.incrementAndGet();
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
        
//        
//        
//        BDBTest.set("key1".getBytes(), "abc1".getBytes());
//        BDBTest.set("key2".getBytes(), "abc2".getBytes());
//        BDBTest.set("key3".getBytes(), "abc3".getBytes());
//        BDBTest.set("key4".getBytes(), "abc4".getBytes());
//        BDBTest.set("key5".getBytes(), "abc5".getBytes());
//        BDBTest.selectByKey("key1");
//        BDBTest.selectAll();
//        BDBTest.stop();
    }
}
