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
package com.ipd.jsf.registry.schedule;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.cache.AppCache;
import com.ipd.jsf.registry.cache.AppInsCache;
import com.ipd.jsf.registry.server.RegistryServer;
import com.ipd.jsf.registry.service.AppIfaceInvokeService;
import com.ipd.jsf.registry.service.ConfigService;
import com.ipd.jsf.registry.service.InterfaceVisitService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

public class RegistryCacheLoader {
    private static final Logger logger = LoggerFactory.getLogger(RegistryCacheLoader.class);
    public static boolean firstLoadFlag = true;

    private int refreshCacheInterval = 60;

    private int visitLoadCount = 0;
    
    @Autowired
    private SubscribeService subscribeServiceImpl;

    @Autowired
    private ConfigService configServiceImpl;

    @Autowired
    private InterfaceVisitService interfaceVisitServiceImpl;

    @Autowired
    private AppIfaceInvokeService appIfaceInvokeServiceImpl;

    @Autowired
    private AppCache appCache;

    @Autowired
    private AppInsCache appInsCache;

    private RegistryServer registryServer;

    public void start() {
        refreshCacheOnSchedule();
        logger.info("registryCacheLoader is running...");
    }

    /**
     * 定时加载registry的缓存
     */
    private void refreshCacheOnSchedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("load-cache"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    refreshCache();
                } catch (Exception e) {
                    logger.error("refreshCacheOnSchedule error: ", e);
                } finally {
                }
            }
        }, 5, refreshCacheInterval, TimeUnit.SECONDS);
    }

    /**
     * @throws Exception
     */
    private void refreshCache() throws Exception {
        if (RegistryUtil.isDBOK) {
            long start = 0;
            //每5分钟加载一次
            if (visitLoadCount++ % 5 == 0) {
                //加载配置缓存
                start = System.currentTimeMillis();
                configServiceImpl.refreshCache();
                logger.info("refresh config cache , elapse:{}ms", System.currentTimeMillis() - start);

                //加载接口授权访问者缓存(用于多语言接入)
                start = System.currentTimeMillis();
                interfaceVisitServiceImpl.refreshCache();
                logger.info("refresh interface visitor cache , elapse:{}ms", System.currentTimeMillis() - start);

                //加载接口与app限制访问
                start = System.currentTimeMillis();
                appIfaceInvokeServiceImpl.refreshCache();
                logger.info("refresh interface app invoke cache , elapse:{}ms", System.currentTimeMillis() - start);
                if (visitLoadCount > 10000) {
                    visitLoadCount = 1;
                }
            }

            //加载自动部署应用缓存
            start = System.currentTimeMillis();
            appCache.refreshCache();
            appInsCache.refreshCache();
            logger.info("refresh app cache , elapse:{}ms", System.currentTimeMillis() - start);

            //加载接口信息缓存
            start = System.currentTimeMillis();
            subscribeServiceImpl.refreshCache(refreshCacheInterval);
            logger.info("refresh subscribe cache , elapse:{}ms", System.currentTimeMillis() - start);

            if (firstLoadFlag) {
                try {
                    //发布服务, 只发布一次.当系统刚启动时，先加载缓存，然后在发布服务
                    registryServer.start();
                } finally {
                    firstLoadFlag = false;
                }
            }
        }
    }

    /**
     * @param refreshCacheInterval the refreshCacheInterval to set
     */
    public void setRefreshCacheInterval(int refreshCacheInterval) {
        this.refreshCacheInterval = refreshCacheInterval;
    }

    /**
     * @return the registryServer
     */
    public RegistryServer getRegistryServer() {
        return registryServer;
    }

    /**
     * @param registryServer the registryServer to set
     */
    public void setRegistryServer(RegistryServer registryServer) {
        this.registryServer = registryServer;
    }

}
