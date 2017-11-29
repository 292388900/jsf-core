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
package com.ipd.jsf.registry.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.domain.CallbackLog;
import com.ipd.jsf.registry.manager.CallbackLogManager;
import com.ipd.jsf.registry.service.CallbackLogService;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.vo.SubscribeUrl;

@Service
public class CallbackLogServiceImpl implements CallbackLogService {
	private Logger logger = LoggerFactory.getLogger(CallbackLogServiceImpl.class);
	// callback日志 队列, 将CallbackLog放入队列
    private LinkedBlockingQueue<CallbackLog> callbackLogQueue = new LinkedBlockingQueue<CallbackLog>(10000);

    //是否开启callback正常日志的记录
    private volatile boolean isOpenCallbackNormalLog = true;
    
    @Autowired
    private CallbackLogManager callbackLogManagerImpl;

    @PostConstruct
    public void init() {
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
            	try {
            		saveCallbackLogAsyn();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
            }
        });
    	thread.start();
    	
    }

    /**
     * 异步保存callback日志
     */
    private void saveCallbackLogAsyn() {
        List<CallbackLog> logList = new ArrayList<CallbackLog>();
        int limit = 5;
        int limitTime = 5000; //5秒
        long time = System.currentTimeMillis();
        while (true) {
            try {
                logList.clear();
                while (true) {
                    try {
                        if (callbackLogQueue.isEmpty()) {
                            Thread.sleep(1000);
                        }
                        CallbackLog log = callbackLogQueue.poll();
                        if (log != null) {
                            logList.add(log);
                        }
                        long currentTime = System.currentTimeMillis();
                        if (logList.size() > limit || (currentTime - time) > limitTime) {
                            time = currentTime;
                            break;
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (!logList.isEmpty()) {
                    callbackLogManagerImpl.create(logList);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }


    /**
     * 保存callback调用的日志
     * @param insKey
     * @param e
     * @param notifyType
     * @param logType
     */
    @Override
    public void saveCallbackLog(String interfaceName, String insKey, Exception e, int logType, SubscribeUrl url, long elapse) {
        int lenLimit = 511;
        try {
        	//如果是callback正常日志，而且正常日志开关关闭，就不保存正常日志了
        	if (e == null && !isOpenCallbackNormalLog) {
        		return;
        	}
            CallbackLog log = new CallbackLog();
            log.setCreateTime(DateTimeZoneUtil.getTargetTime());
            log.setInterfaceName(interfaceName);
            log.setInsKey(insKey);
            log.setIp(RegistryUtil.getIpFormInsKey(insKey));
            if (e == null) {
                log.setLogNote("callback success! elapse:" + elapse + "ms");
            } else {
                log.setLogNote(RegistryUtil.limitString(e.getClass().getCanonicalName() + " " + e.getMessage(), lenLimit));
            }
            log.setLogType(logType);
            if (url.getSourceUrl() != null) {
                if (url.getSourceUrl().getAlias() != null) {
                    log.setAlias(url.getSourceUrl().getAlias());
                }
                if (url.getSourceUrl().getDataVersion() > 0) {
                    log.setDataVersion(DateTimeZoneUtil.getTargetTime(url.getSourceUrl().getDataVersion()));
                }
            }
            log.setParam(RegistryUtil.limitString(url.toString(), lenLimit));
            log.setNotifyType(url.getType());
            log.setRegIp(RegistryUtil.getRegistryIPPort());
            log.setCreator(CallbackLog.REGISTRY_CREATOR);
            callbackLogQueue.add(log);
        } catch (Exception e2) {
            logger.error(e2.getMessage(), e2);
        }
    }

	/**
	 * @return the isOpenCallbackNormalLog
	 */
	public boolean isOpenCallbackNormalLog() {
		return isOpenCallbackNormalLog;
	}

	/**
	 * @param isOpenCallbackNormalLog the isOpenCallbackNormalLog to set
	 */
	public void setOpenCallbackNormalLog(boolean isOpenCallbackNormalLog) {
		this.isOpenCallbackNormalLog = isOpenCallbackNormalLog;
	}


}
