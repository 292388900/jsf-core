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

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.registry.berkeley.domain.BdbServerList;
import com.ipd.jsf.registry.local.manager.ServerLocalManager;
import com.ipd.jsf.registry.service.ServerService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

/**
 * 同步Server数据
 * 
 * 从本地数据库同步到MySql
 *
 */
public class ServerSyncSchedule {

	private static Logger logger = LoggerFactory.getLogger(ServerSyncSchedule.class);

    //ump上限值
    private int totalCountLimit = 10000;

    //ump报警值
    private String umpBerkeleyDBKey = null;

    //检查totalcount的次数取模
    private int checkMod = 4;

    private int checkCount = 1;

	private int interval = 15;

	@Autowired
	private ServerLocalManager localManager;

	@Autowired
	private ServerService serverService;
	
	@Autowired
    private SubscribeService subscribeServiceImpl;

    public void start() {
		syncServerSchedule();
        logger.info("server sync Schedule is running...");
    }
	
	private void syncServerSchedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("server"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if (RegistryUtil.isOpenProviderBerkeleyDB) {
                        checkTotalCount();
                        syncToDB();
                    }
                } catch (Exception e) {
                    logger.error("sync server data error:", e);
                }
            }
        }, 30, interval, TimeUnit.SECONDS);
    }

	/**
	 * @throws Exception
	 */
	private void syncToDB() throws Exception {
        if (RegistryUtil.isDBOK) {
    	    while(true) {
                List<BdbServerList> list = localManager.getServers();
                if (list == null || list.isEmpty()) {
                    return;
                }
                for (BdbServerList bdbServer : list) {
    	            try {
                        if (bdbServer.isRegistry()) {
                            serverService.saveServer(bdbServer.getServers(), bdbServer.getIns());
        	            } else {
        	            	serverService.removeServer(bdbServer.getServers(), bdbServer.getIns());
        	            }
        	            localManager.delete(bdbServer.getKey());
    	            } catch (Exception e) {
    	                logger.error(e.getMessage(), e);
    	            }
    	        }
    	        localManager.flush();
    	    }
	    }
	}

    /**
     * 获取总数
     */
    private void checkTotalCount() {
        //如果取模为0就执行
        if (checkCount++ % checkMod == 0) {
            checkCount = 1;

            int totalCount = 0;
            try {
                totalCount = localManager.getTotalCount();
                if (totalCount < totalCountLimit) {
                    return;
                }
                logger.warn(umpBerkeleyDBKey + ",berkeleydb服务提供者表实际记录总数:"
                        + totalCount + ",超过阈值" + totalCountLimit + "," + RegistryUtil.getRegistryIP());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * @return the totalCountLimit
     */
    public int getTotalCountLimit() {
        return totalCountLimit;
    }

    /**
     * @param totalCountLimit the totalCountLimit to set
     */
    public void setTotalCountLimit(int totalCountLimit) {
        this.totalCountLimit = totalCountLimit;
    }

    /**
     * @return the umpBerkeleyDBKey
     */
    public String getUmpBerkeleyDBKey() {
        return umpBerkeleyDBKey;
    }

    /**
     * @param umpBerkeleyDBKey the umpBerkeleyDBKey to set
     */
    public void setUmpBerkeleyDBKey(String umpBerkeleyDBKey) {
        this.umpBerkeleyDBKey = umpBerkeleyDBKey;
    }

    /**
     * @return the interval
     */
    public int getInterval() {
        return interval;
    }

    /**
     * @param interval the interval to set
     */
    public void setInterval(int interval) {
        this.interval = interval;
    }

	
}
