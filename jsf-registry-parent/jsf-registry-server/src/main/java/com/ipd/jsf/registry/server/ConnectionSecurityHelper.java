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
package com.ipd.jsf.registry.server;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.registry.recoder.ConnectionRecoder;

public class ConnectionSecurityHelper {
	private static final Logger logger = LoggerFactory.getLogger(ConnectionSecurityHelper.class);
    /** 连接数阈值 **/
    private int con_threshold = 8000;
    /** 达到连接数阈值后，一分钟允许增长的连接数 **/
    private int con_delta = 200;
    /** 分钟取余 **/
    private int mod = 5;
    /** 记录5分钟新增连接数 **/
    private AtomicInteger[] bucket = new AtomicInteger[mod];

    private void initSchedule() {
    	ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                try {
                	int num = getCurrentMunite();
                	for (int i = 0; i < bucket.length; i++) {
                		if (i != num) {
                			bucket[i].set(0);
                		}
                	}
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    public ConnectionSecurityHelper(int con_threshold, int con_delta) {
    	this.con_threshold = con_threshold;
    	this.con_delta = con_delta;
    	for (int i = 0; i < bucket.length; i++) {
    		bucket[i] = new AtomicInteger(0);
    	}
    	initSchedule();
    }

	/**
     * 增量连接数是否达到每分钟增量上限
     * @return
     */
    private boolean isConOverflow() {
    	int num = getCurrentMunite();
    	if (bucket[num].get() > con_delta) {
    		return true;
    	}
    	bucket[num].incrementAndGet();
    	return false;
    }

    /**
     * 获取当前分钟并取余
     * @return
     */
    private int getCurrentMunite() {
    	return (int) (System.currentTimeMillis() / 60000 % mod);
    }

    /**
     * 连接数保护是否开启
     * true ：开启保护策略
     * false：关闭保护策略
     * @return
     */
    public boolean connectionProtect() {
		//如果阈值为0，关闭保护策略
		if (con_threshold == 0) {
			return false;
		}
    	try {
    		//如果当前注册中心的连接数超过阈值，就判断是否超过每分钟的增量阀值
    		if (ConnectionRecoder.getConnectionTotalCount() > con_threshold) {
    			return isConOverflow();
        	}
		} catch (Exception e) {
			logger.error("connectionProtect is failed. " + e.getMessage(), e);
		}
    	return false;
    }
}
