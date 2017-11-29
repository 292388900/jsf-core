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

import com.ipd.jsf.registry.berkeley.domain.BdbJsfIns;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.local.manager.SafInsLocalManager;
import com.ipd.jsf.registry.manager.SafInsManager;
import com.ipd.jsf.registry.threadpool.NamedThreadFactory;
import com.ipd.jsf.registry.util.RegistryUtil;

public class InstanceSchedule {

    private static final Logger logger = LoggerFactory.getLogger(InstanceSchedule.class);

    //ump上限值
    private int totalCountLimit = 10000;

    //ump报警值
    private String umpBerkeleyDBKey = null;

    //检查totalcount的次数取模
    private int checkMod = 4;

    //检查的次数
    private int checkCount = 1;

    //定时器间隔
    private int interval = 15;

    @Autowired
    private SafInsLocalManager localManager;

    @Autowired
    private SafInsManager jsfInsManage;

    public void start() {
        saveInstanceSchedule();
        logger.info("instance sync Schedule is running...");
    }

    /**
     * 定时将实例写入数据库
     */
    private void saveInstanceSchedule() {
        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("ins_task"));
        scheduledExecutorService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                try {
                    if (RegistryUtil.isOpenWholeBerkeleyDB) {
                        checkTotalCount();
                        saveInstance();
                    }
                } catch (Exception e) {
                    logger.error("saveInstanceSchedule error", e);
                }
            }

        }, 30, interval, TimeUnit.SECONDS);
    }

    /**
     * @throws Exception
     */
    private void saveInstance() throws Exception {
        //检查db是否能连接，再做db操作
        if (RegistryUtil.isDBOK) {
            while(true) {
                //从BerkeleyDB同步到MySql
                List<BdbJsfIns> localDataList = localManager.getJsfInsList();
                if (localDataList == null || localDataList.isEmpty()) {
                    break;
                }
                for (BdbJsfIns bdbJsfIns : localDataList) {
                    JsfIns ins = bdbJsfIns.getIns();
                    try {
                        if (bdbJsfIns.isRegistry()) {
                            //同步到mysql
                        	jsfInsManage.register(ins);
                        }
                        localManager.delete(bdbJsfIns.getKey());
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
                logger.warn(umpBerkeleyDBKey + ",berkeleydb实例表实际记录总数:"
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

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }
    
}
