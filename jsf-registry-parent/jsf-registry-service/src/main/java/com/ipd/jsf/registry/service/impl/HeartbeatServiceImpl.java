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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.gd.util.ConcurrentHashSet;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.local.manager.SafInsLocalManager;
import com.ipd.jsf.registry.manager.SafInsManager;
import com.ipd.jsf.registry.service.HeartbeatService;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.registry.threadpool.WorkerThreadPool;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;

/**
 * 心跳处理操作
 */
@Service
public class HeartbeatServiceImpl implements HeartbeatService {
    private Logger logger = LoggerFactory.getLogger(HeartbeatServiceImpl.class);
    private float loadFactor = 0.75f;
//    private volatile boolean isOpenLds = false; 
    //保存实例心跳时间，key=insKey, value=心跳时间
    private ConcurrentHashMap<String, Long> hbMapCache = new ConcurrentHashMap<String, Long>(128, loadFactor, 128);

    //保存数据库中没有的inskey, 然后由心跳通知客户端，重新recover
    private ConcurrentHashSet<String> lossInsKeySet = new ConcurrentHashSet<String>();

    @Autowired
    private SafInsLocalManager safInsLocalManager;

    @Autowired
    private SafInsManager safInsManagerImpl;

    @Autowired
    private SubscribeService subscribeServiceImpl;


    private WorkerThreadPool notifyThreadPool = new WorkerThreadPool(30, 30, "checknotify-hb");

    @PostConstruct
    public void init() {
    	logger.info("init...");
    }

    /**
     * 保存实例信息
     * @param ins
     */
    @Override
    public void register(JsfIns ins) throws Exception {
        if (RegistryUtil.isOpenWholeBerkeleyDB) {
            safInsLocalManager.register(ins);
        } else {
            safInsManagerImpl.register(ins);
        }
    }

    /**
     * 将insKey放入内存中, 与saveHb结合使用
     * 并检查insKey是否在lossInsKeySet中，如果在，说明数据库没有该实例，返回false，需要重新recover
     * @param insKey
     */
    @Override
    public boolean putHbCache(String insKey) throws Exception {
        try {
            if (insKey != null && !insKey.isEmpty()) {
                //inskey在lossInsKeySet中，说明insKey不在数据库中
                if (lossInsKeySet.contains(insKey)) {
                    //从set中清除，避免内存积压
                    lossInsKeySet.remove(insKey);
                    //从cache删除，等recover后的doHeartbeat，再写入db
                    hbMapCache.remove(insKey);
                    return false;
                } else {
                    hbMapCache.put(insKey, RegistryUtil.getSystemCurrentTime());
                }
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error("添加心跳缓存异常,", e);
            return false;
        }
        return true;
    }

    /**
     * 将内存中的心跳数据保存到数据库中
     * 按照秒，来批量更新实例的心跳时间
     * @param regIp
     * @throws Exception
     */
    @Override
    public void saveHb(String regIp) throws Exception {
    	long nowTime = System.currentTimeMillis();
        Set<String> noHbSet = new HashSet<String>();
        Map<Long, List<String>> temp = new HashMap<Long, List<String>>();
        for (Map.Entry<String, Long> entry : hbMapCache.entrySet()) {
            try {
                //将时间省略到秒
                String insKey = entry.getKey();
                Long value = entry.getValue();
                long hbtime = value == null ? 0 : (value.longValue() / 1000) * 1000;
                if (nowTime - hbtime > (HeartbeatConstants.REGISTRY_HEARTBEAT_PERIOD + 2000)) {  //+2000作为时间的误差修正，防止漏掉心跳
	                if (nowTime - hbtime > HeartbeatConstants.JUDGE_DEAD_INSTANCE_TIME) {
	                    //超过死亡时间就删除
	                    hbMapCache.remove(insKey);
	                    noHbSet.add(insKey);
	                }
	                //如果超过心跳时间，就不向数据库更新了
                	continue;
                }
                if (temp.get(hbtime) == null) {
                    temp.put(hbtime, new ArrayList<String>());
                }
                //以时间为key，将insKey放入value列表中
                temp.get(hbtime).add(insKey);
                if (logger.isDebugEnabled()) logger.debug("time:{}, map size:{}", hbtime, temp.size());
            } catch (Exception e) {
                logger.error("保存心跳异常:" + entry.getKey() + ", error:" + e.getMessage(), e);
            }
        }
        long start = System.currentTimeMillis();
        int dbOpCount = 0;
        if (!temp.isEmpty()) {
            //通过mysql保存
            for (Map.Entry<Long, List<String>> entry : temp.entrySet()) {
            	dbOpCount += this.batchUpdateHb(entry.getValue(), DateTimeZoneUtil.getTargetTime(entry.getKey().longValue()), regIp);
            }
        }
        long end = System.currentTimeMillis();
        logger.info("saveHb: hbMap size:{}, convert map size:{}, save db :{}ms, dbOpCount:{}", hbMapCache.size(), temp.size(), (end - start), dbOpCount);
        if (noHbSet.size() > 0) {
            for (final String insKey : noHbSet) {
                notifyThreadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            long hbtime = hbMapCache.get(insKey) == null ? 0 : (hbMapCache.get(insKey).longValue() / 1000) * 1000;
                            if (System.currentTimeMillis() - hbtime > HeartbeatConstants.JUDGE_DEAD_INSTANCE_TIME) {
                                //检查callback是否失效.若失败，就删除该实例
                                if (subscribeServiceImpl.checkCallback(insKey, true) == false) {
                                    //并且删除实例缓存和callback
                                    subscribeServiceImpl.removeInstanceCache(insKey);
                                } else {
                                    //如果callback能连通，但是已经没有心跳，需要远程重新设置客户端定时器. 这种情况有可能是客户端服务器时间被改了
                                    subscribeServiceImpl.resetRemoteClientSchedule(insKey);
                                }
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                });
            }
        }
    }

    /**
     * 批量更新心跳数据
     * 当插入数据库的数据量过大时，mysql会抛  com.mysql.jdbc.PacketTooBigException，因此采用每100条批量更新一次sql
     * @param list
     * @param hbTime
     * @param regIp
     * @return  返回执行sql的次数
     * @throws Exception
     */
    private int batchUpdateHb(List<String> list, final Date hbTime, String regIp) throws Exception {
        int page = 0;
        if (list != null && list.size() > 0) {
            List<String> tmpInsKeyList = new ArrayList<String>();
            int pageSize = 100;
            int fromIndex = 0;
            int toIndex = 0;
            int tempCount = 0;
            while (toIndex < list.size()) {
                fromIndex = page * pageSize;
                toIndex = (page + 1) * pageSize;
                toIndex = toIndex > list.size() ? list.size() : toIndex;
                final List<String> subList = list.subList(fromIndex, toIndex);
                tempCount = safInsManagerImpl.saveHb(subList, hbTime, regIp);
                if (tempCount < subList.size()) {
                    //如果需要更新数量与更新结果数量不一致，先将inskey保存起来
                    tmpInsKeyList.addAll(subList);
                }
                page ++;
            }
            //找到心跳状态停止的实例或者不存在的实例
            if (!tmpInsKeyList.isEmpty() && lossInsKeySet.size() < 500) {
                List<String> tmpDBList = safInsManagerImpl.getInsKeyListByInsKey(tmpInsKeyList);
                if (tmpDBList.size() < tmpInsKeyList.size()) {
	                for (String insKey : tmpInsKeyList) {
	                    //找到不在数据库中的inskey
	                    if (!tmpDBList.contains(insKey)) {
	                        lossInsKeySet.add(insKey);
	                    }
	                }
                }
            }
        }
        return page;
    }

    @Override
    public boolean isExist(String insKey) {
        return hbMapCache.containsKey(insKey);
    }
}
