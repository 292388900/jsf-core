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
package com.ipd.jsf.worker.manager.impl;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

import com.ipd.jsf.worker.dao.CheckDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.manager.CheckDBConnManager;

@Service
public class CheckDBConnManagerImpl implements CheckDBConnManager {
    private static Logger logger = LoggerFactory.getLogger(CheckDBConnManagerImpl.class);
    private static volatile boolean isFirst = true;
    private ExecutorService service = Executors.newSingleThreadExecutor(new WorkerThreadFactory("checkDBConnManager"));
    @Autowired
    private CheckDao checkDao;
    int retryTimes = 2;

    /* (non-Javadoc)
     * @see com.ipd.saf.registry.manager.CheckDbConnManager#check()
     */
    @Override
    public boolean check() {
        byte i = 0;
        boolean result = false;
        //检查2次，如果都超时，就返回false
        while (i++ < retryTimes && !result) {
            result = executeTimeControlMethod(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        test();
                        return true;
                    } catch (Exception e) {
                        logger.error("can not connect to mysql db, error" + e.getMessage(), e);
                        return false;
                    }
                }
            }, 1000);
            //如果第一次访问异常，就等会再试
            if (!result && i < retryTimes) {
            	try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
            }
        }
        return result;
    }

    /**
     * 更新数据库，测试数据库连接是否正常
     * @throws Exception
     */
    private void test() throws Exception {
        if (isFirst) {
            checkDao.insert();
            isFirst = false;
        } else {
            checkDao.update();
        }
    }

    /**
     * 控制方法执行超时时间,超时就终止方法执行
     * @param runable
     * @param timeout
     * @return
     */
    private boolean executeTimeControlMethod(Callable<Boolean> runable, long timeout) {
        try {
            Future<Boolean> result = service.submit(runable);
            Boolean i = result.get(timeout, TimeUnit.MILLISECONDS);
            if (i != null && i.booleanValue() == true) {
                return true;
            }
        } catch (RejectedExecutionException e) {
            if (service.isShutdown()) {
                service = Executors.newSingleThreadExecutor(new WorkerThreadFactory("checkDBConnManager"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    public static void main(String[] args) {
        CheckDBConnManagerImpl impl = new CheckDBConnManagerImpl();
        long start = System.currentTimeMillis();
        impl.executeTimeControlMethod(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    Thread.sleep(300);
                    return true;
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }, 1000);
        System.out.println(System.currentTimeMillis() - start);
    }
}
