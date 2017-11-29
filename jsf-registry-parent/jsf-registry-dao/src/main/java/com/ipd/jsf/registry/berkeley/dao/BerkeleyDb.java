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
package com.ipd.jsf.registry.berkeley.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.sleepycat.je.Cursor;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

/**
 * Berkeley 本地存储
 *
 */
public class BerkeleyDb {

    private static final Logger logger = LoggerFactory.getLogger(BerkeleyDb.class);

    private static Environment env;

    private static Map<String, Database> dbMap = new HashMap<String, Database>();

    private static Lock lock = new ReentrantLock();

    private File pathFile = null;

    /** dbPath:Berkeley DB存储路径 */
    public BerkeleyDb(String dbPath) throws Exception {
        pathFile = new File(dbPath);
        if (!pathFile.exists()) {
            pathFile.mkdirs();
        }
        if (!pathFile.isDirectory()) {
            logger.error("The path of local db is not a directory:" + dbPath);
            throw new Exception("The path of local db is not a directory.");
        }
        initAndCheck();
    }

	/**
	 * 添加数据
	 * @param dbName
	 * @param key
	 * @param value
	 * @throws Exception
	 */
	public boolean put(String dbName, String key, byte value[]) throws Exception{
        if (key == null || value == null) {
			logger.error("The key and value must be not null!");
			throw new Exception("The put data must be not null!");
		}
//        long time1 = System.currentTimeMillis();
        Database db = this.getDb(dbName);
//        logger.info("######### {}, key:{}, getDB elapse:{}ms", dbName, key, (System.currentTimeMillis() - time1));
		DatabaseEntry deKey = new DatabaseEntry(key.getBytes("utf-8"));

		DatabaseEntry deValue = new DatabaseEntry(value);
		//没有对应key时直接添加，存在时直接覆盖
//		long start = System.currentTimeMillis();
		OperationStatus status = db.put(null, deKey, deValue);
//		logger.info("######### {}, key:{}, put elapse:{}ms", dbName, key, (System.currentTimeMillis() - start));
		if (status.equals(OperationStatus.SUCCESS)) {
		    return true;
		} else {
		    logger.info("false, dbName:{}, key:{}, status", dbName, key, status.name());
		    return false;
		}
	}

	/**
	 * 通过游标查询数据
	 * 当key为null或者""时，返回数据库的所有记录
	 * @param dbName
	 * @param key
	 * @return
	 * @throws Exception
	 */
    public List<BerkeleyBean> get(String dbName, String key) throws Exception {
        List<BerkeleyBean> resultList = null;
        Cursor cursor = null;
        try {
            // 每次取50条
            int itemLimit = 50;
            Database db = this.getDb(dbName);
            cursor = db.openCursor(null, null);
            resultList = new ArrayList<BerkeleyBean>(itemLimit);
            byte keyData[] = null;
            if (key != null && !"".equals(key.trim())) {
                keyData = key.getBytes("utf-8");
            }
            DatabaseEntry foundKey = new DatabaseEntry(keyData);
            DatabaseEntry foundData = new DatabaseEntry();
            // 循环取得数据
            BerkeleyBean bean = null;
            String getKey = null;
            byte data[] = null;
            while (cursor.getNext(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                getKey = new String(foundKey.getData(), "utf-8");
                data = foundData.getData();
                bean = new BerkeleyBean();
                bean.setKey(getKey);
                bean.setValue(data);
                resultList.add(bean);
                if (itemLimit-- <= 0)
                    break;
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return resultList;
    }

    /**
     * 获取总数
     * @param dbName
     * @return
     * @throws Exception
     */
    public int getTotalCount(String dbName) throws Exception {
        int count = 0;
        Cursor cursor = null;
        try {
            Database db = this.getDb(dbName);
            cursor = db.openCursor(null, null);
            DatabaseEntry foundKey = new DatabaseEntry();
            DatabaseEntry foundData = new DatabaseEntry();
            if (cursor.getLast(foundKey, foundData, LockMode.DEFAULT) == OperationStatus.SUCCESS) {
                count = cursor.count();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return count;
    }

    public void delete(String dbName, String key) throws Exception {
        byte[] theKey = key.getBytes("UTF-8");
        this.getDb(dbName).delete(null, new DatabaseEntry(theKey));
    }

    public void flush() {
        initAndCheck();
        env.sync();
        env.cleanLog();
    }

    public void close() {
        for (Map.Entry<String, Database> entry : dbMap.entrySet()) {
        	if (entry != null && entry.getValue() != null) {
        		entry.getValue().close();
        	}
            dbMap.put(entry.getKey(), null);
        }
        env.close();
    }

    public boolean isValue() {
        return env.isValid();
    }

    /**
     * 检查初始化BerkeleyDB环境
     */
    private void initAndCheck() {
        if (env != null && env.isValid()) {
            return;
        }
        // 初始化BerkeleyDB 环境
        synchronized (lock) {
            if (env != null && env.isValid()) {
                return;
            }
            EnvironmentConfig envConfig = new EnvironmentConfig();
            // 自动创建
            envConfig.setAllowCreate(true);
            envConfig.setTransactional(true);
            // 缓存大小 10M
            envConfig.setCacheSize(10 * 1024 * 1024);
            env = new Environment(pathFile, envConfig);
        }
    }

	/**
	 * 根据数据库名取得数据库实例
	 * @param dbName
	 * @return
	 * @throws Exception 
	 */
    private Database getDb(String dbName) throws Exception {
        initAndCheck();
        Database db = dbMap.get(dbName);
        if (db != null) {
            return db;
        }
        synchronized (lock) {
            db = dbMap.get(dbName);
            if (db != null) {
                return db;
            }
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setOverrideBtreeComparator(true);
            dbConfig.setBtreeComparator(KeyComparator.class);
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(true);
            db = env.openDatabase(null, dbName, dbConfig);
            dbMap.put(dbName, db);
            return db;
        }
	}
		
}
