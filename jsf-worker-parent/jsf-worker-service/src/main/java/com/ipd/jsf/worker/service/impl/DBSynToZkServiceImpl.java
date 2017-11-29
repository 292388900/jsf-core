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
package com.ipd.jsf.worker.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ipd.jsf.worker.domain.SynZkDBLog;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import com.ipd.jsf.worker.manager.SynZkDBLogManager;
import com.ipd.jsf.worker.service.DBSynToZkService;
import com.ipd.jsf.worker.service.common.Constants;
import com.ipd.jsf.worker.service.common.URL;
import com.ipd.jsf.worker.thread.syn.util.ZkHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.manager.ServerManager;
import com.ipd.jsf.worker.service.common.utils.NetUtils;
import com.ipd.jsf.worker.thread.syn.util.ConvertUtils;
import com.ipd.jsf.worker.thread.syn.util.SynUtil;

@Component
public class DBSynToZkServiceImpl implements DBSynToZkService {
    private static final Logger logger = LoggerFactory.getLogger(DBSynToZkServiceImpl.class);

    private boolean isSynToZkProvider = false;

    private long lastLoadTime = 1;

    private int zkNodeCountList = 5000;

    private final String provider_ZK_count = "provider.zk.count";

    private final String provider_dbInZk_count = "provider.dbInZk.count";

    private final String provider_db_count = "provider.db.count";

    private final String provider_zkIndb_count = "provider.zkIndb.count";

    private final String provider_zkadd_count = "provider.zkadd.count";

    private final String provider_zkdel_count = "provider.zkdel.count";

    private ExecutorService threadPool = Executors.newFixedThreadPool(5, new WorkerThreadFactory("dbSynToZkServicePool"));

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private InterfaceInfoManager interfaceInfoManager;

    @Autowired
    private SynZkDBLogManager synZkDBLogManager;

    private ZkHelper zkHelper;

    private static String host = null;

    static {
        try {
            host = NetUtils.getLocalHost();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void init() {
        if (zkHelper == null) {
            zkHelper = new ZkHelper();
        }
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.worker.service.DBSynToZkService#syn(boolean, boolean)
     */
    @Override
    public void syn(boolean isSynToZkProvider) throws Exception {
        this.isSynToZkProvider = isSynToZkProvider;
        try {
            if (isSynProvider()) {
                init();
                List<InterfaceInfo> interfaceList = getChangeInterfaceList();
                if (interfaceList != null && !interfaceList.isEmpty()) {
                    this.executeSynProvider(interfaceList);
                }
            }
        } catch (Exception e) {
            logger.error("syn DB to ZK error:" + e.getMessage(), e);
        }
    }

    /**
     * 获取变化的接口
     * @return
     * @throws Exception
     */
    private List<InterfaceInfo> getChangeInterfaceList() throws Exception {
        long currentTime = System.currentTimeMillis();
        long loadTime = lastLoadTime;
        if (loadTime > 1) {   //如果不是第一次调用，就往前推3秒加载，防止漏加载
            loadTime = loadTime - 3000;
        }
        List<InterfaceInfo> changeList = interfaceInfoManager.getInterfaceVersionByTime(new Date(loadTime));
        lastLoadTime = currentTime;
        logger.info("db to zookeeper, 接口数  {} 个", changeList.size());
        return changeList;
    }

    /**
     * 同步db中的provider到zk
     * @param interfaceList
     */
    private void executeSynProvider(List<InterfaceInfo> interfaceList) {
        try {
            if (isSynProvider()) {
                final List<Map<String, Integer>> statList = Collections.synchronizedList(new ArrayList<Map<String, Integer>>());
                
                final CountDownLatch latch = new CountDownLatch(interfaceList.size());
                for (final InterfaceInfo interfaceInfo : interfaceList) {
                    threadPool.execute(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Map<String, Integer> statMap = synProvider(interfaceInfo);
                                statList.add(statMap);
                            } catch (Exception e) {
                                logger.error("db单向同步provider节点到zk错误,iface:" + interfaceInfo.getInterfaceName(), e);
                            } finally {
                                latch.countDown();
                            }
                        }
                    });
                }
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    logger.error(e.getMessage());
                }

                //将每个接口统计的数值做累加
                Map<String, Integer> statTotalMap = new HashMap<String, Integer>();
                Integer value = null; 
                for (Map<String, Integer> map : statList) {
                    for (Map.Entry<String, Integer> entry : map.entrySet()) {
                        value = statTotalMap.get(entry.getKey());
                        if (value == null) {
                            value = 0;
                        }
                        value = value.intValue() + entry.getValue().intValue();
                        statTotalMap.put(entry.getKey(), value);
                    }
                }
                if (!statTotalMap.isEmpty()) {
                    logger.info("provider统计，变化的接口中，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，添加到zk中有{}个，从zk删除有{}个",
                            statTotalMap.get(provider_ZK_count),
                            statTotalMap.get(provider_dbInZk_count),
                            statTotalMap.get(provider_db_count),
                            statTotalMap.get(provider_zkIndb_count),
                            statTotalMap.get(provider_zkadd_count),
                            statTotalMap.get(provider_zkdel_count));
                }
            } else {
                logger.info("db单向同步provider到zk的开关未打开，此次不执行db同步provider节点到zk的操作!");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 同步db中的provider到zk
     * @param interfaceInfo
     * @throws Exception
     */
    private Map<String, Integer> synProvider(InterfaceInfo interfaceInfo) throws Exception {
        //db本身的provider
        Map<String, byte[]> dbProviderMap = Maps.newHashMap();
        //db从zk中同步过来的provider
        Map<String, byte[]> zkInDBProvider = Maps.newHashMap();
        //zk中的provider
        Map<String, byte[]> zkProviderMap = Maps.newHashMap();
        //zk从db中同步过来的节点
        Map<String, byte[]> dbInZkProvider = Maps.newHashMap();

        //1. 获取db的provider
        initDBProviderList(interfaceInfo, dbProviderMap, zkInDBProvider);

        //2. 获取zk的provider
        initZkProviders(interfaceInfo.getInterfaceName(), zkProviderMap, dbInZkProvider);

        //3. 比较zk和db的provider
        Map<String, byte[]> zkDel = Maps.difference(dbInZkProvider, dbProviderMap).entriesOnlyOnLeft();

        //4. 将减少的provider从zk中删除
        this.delZkProviders(interfaceInfo.getInterfaceName(), zkDel);
        
        //5. 比较zk和db的provider，过滤掉zk已经创建的节点
        Map<String, byte[]> zkAdd = Maps.difference(dbProviderMap, dbInZkProvider).entriesOnlyOnLeft();

        //6. 将新增provider写到zookeeper
        this.createProviderNode(interfaceInfo.getInterfaceName(), zkAdd);

        //8. 更新zk接口的providers节点的下线状态
        this.checkAndSaveOffline(interfaceInfo.getInterfaceName(), zkAdd, zkDel);

        //7. 统计数量
        Map<String, Integer> statMap = new HashMap<String, Integer>();
        // (1) zk 的provider数
        Integer zkCount = statMap.get(provider_ZK_count);
        if (zkCount == null) {
            zkCount = 0;
        }
        zkCount = zkCount.intValue() + zkProviderMap.size();
        statMap.put(provider_ZK_count, zkCount);

        // (2) db在zk中 的provider数
        Integer dbInZkCount = statMap.get(provider_dbInZk_count);
        if (dbInZkCount == null) {
            dbInZkCount = 0;
        }
        dbInZkCount = dbInZkCount.intValue() + dbInZkProvider.size();
        statMap.put(provider_dbInZk_count, dbInZkCount);
        
        // (3) db中 的provider数
        Integer dbCount = statMap.get(provider_db_count);
        if (dbCount == null) {
            dbCount = 0;
        }
        dbCount = dbCount.intValue() + dbProviderMap.size();
        statMap.put(provider_db_count, dbCount);
        
        // (4) db在zk中 的provider数
        Integer zkIndbCount = statMap.get(provider_zkIndb_count);
        if (zkIndbCount == null) {
            zkIndbCount = 0;
        }
        zkIndbCount = zkIndbCount.intValue() + zkInDBProvider.size();
        statMap.put(provider_zkIndb_count, zkIndbCount);
        
        // (5) 添加到db 的provider数
        Integer dbaddCount = statMap.get(provider_zkadd_count);
        if (dbaddCount == null) {
            dbaddCount = 0;
        }
        dbaddCount = dbaddCount.intValue() + zkAdd.size();
        statMap.put(provider_zkadd_count, dbaddCount);

        // (6) 从db删除 的provider数
        Integer dbdelCount = statMap.get(provider_zkdel_count);
        if (dbdelCount == null) {
            dbdelCount = 0;
        }
        dbdelCount = dbdelCount.intValue() + zkDel.size();
        statMap.put(provider_zkdel_count, dbdelCount);

        if (logger.isDebugEnabled()) {
            logger.debug(interfaceInfo.getInterfaceName() + 
                ", provider统计，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，添加到zk中有{}个，从zk删除有{}个",
                zkProviderMap.size(),
                dbInZkProvider.size(),
                dbProviderMap.size(),
                zkInDBProvider.size(),
                zkAdd.size(),
                zkDel.size());
        }
        return statMap;
    }

    /**
     * 读取db的providers到缓存
     * @param interfaceInfo
     * @param dbProviderMap
     * @param zkInDBProvider
     * @throws Exception
     */
    private void initDBProviderList(InterfaceInfo interfaceInfo, Map<String, byte[]> dbProviderMap, Map<String, byte[]> zkInDBProvider) throws Exception {
        List<Server> serverList = serverManager.getListByInterfaceId(interfaceInfo.getInterfaceId());
        if (serverList != null) {
            for (Server server : serverList) {
                if (server != null) {
                    try {
                        //如果是jsf协议，就将协议转为dubbo，同步到zk
                        if (server.getProtocol() == ProtocolType.jsf.value()) {
                            server.setProtocol(ProtocolType.dubbo.value());
                        }
                        String full = ConvertUtils.convertDBServer2URL(server, interfaceInfo.getInterfaceName(), false);
                        if (SourceType.zookeeper.value() == server.getSrcType()) {
                            zkInDBProvider.put(full, ConvertUtils.status(server.getStatus()));
                        } else {
                            dbProviderMap.put(full, ConvertUtils.status(server.getStatus()));// 加上节点状态 at
                        }
                    } catch (Exception e) {
                        logger.error("when init providers from db ,exception :" + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * 读取zk的provider到缓存
     * @param interfaceName
     * @param zkProviderMap
     * @param dbInZkProvider
     */
    private void initZkProviders(String interfaceName, Map<String, byte[]> zkProviderMap, Map<String, byte[]> dbInZkProvider) throws Exception {
        zkHelper.initZkProviders(interfaceName, zkProviderMap, dbInZkProvider);
    }

    /**
     * create node : 将增加的provider写到zookeeper
     * @param interfaceName
     * @param right
     * @throws Exception
     */
    private void createProviderNode(String interfaceName, Map<String, byte[]> right) throws Exception {
        if(right != null && !right.isEmpty()) {
            if (right.size() >= zkNodeCountList) {
                logger.error("provider子节点数量 = " + right.size() + " 超过最大值" + zkNodeCountList + "，不进行节点创建！");
                return;
            }
            Set<String> log = new HashSet<String>();
            try {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, byte[]> entry : right.entrySet()) {
                    String urlstr = ConvertUtils.filterZKProvider(entry.getKey(), entry.getValue()[0]);
                    builder.append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(interfaceName).append(SynUtil.PROVIDERS).append(SynUtil.SPLITSTR_SLASH).append(URL.encode(urlstr)).append(SynUtil.FROM_DB_ENCODE);
                    String path = builder.toString();
                    zkHelper.createNode(path, entry.getValue(), true);
                    builder.delete(0, builder.length());
                    log.add(urlstr);
                }
            } finally {
                Date createTime = new Date();
                save(interfaceName, log, "ZKAdd", createTime, host);
            }
        }
    }

    /**
     * 从zk上删除provider节点
     * @param interfaceName
     * @param diffList
     * @throws Exception
     */
    private void delZkProviders(String interfaceName, Map<String, byte[]> right) throws Exception {
        if (right != null) {
            Set<String> log = new HashSet<String>();
            try {
                StringBuilder builder = new StringBuilder();
                for (Map.Entry<String, byte[]> entry : right.entrySet()) {
                    String urlstr = ConvertUtils.filterZKProvider(entry.getKey(), entry.getValue()[0]);
                    builder.append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(interfaceName).append(SynUtil.PROVIDERS).append(SynUtil.SPLITSTR_SLASH).append(URL.encode(urlstr)).append(SynUtil.FROM_DB_ENCODE);
                    String path = builder.toString();
                    zkHelper.delete(path);
                    builder.delete(0, builder.length());
                    log.add(urlstr);
                }
            } finally {
                Date createTime = new Date();
                save(interfaceName, log, "ZKDel", createTime, host);
            }
        }
    }

    /**
     * 将强制下线的节点保存到zk中
     * @param iface
     * @param zkAdd
     * @param zkDel
     */
    private void checkAndSaveOffline(String iface, Map<String, byte[]> zkAdd, Map<String, byte[]> zkDel) {
        Set<String> addOfflineSet = null;
        if ((zkAdd != null && !zkAdd.isEmpty())) {
            //从添加的节点中，找到状态为下线的节点
            addOfflineSet = new HashSet<String>();
            String addKey = null;
            for (Map.Entry<String, byte[]> entry : zkAdd.entrySet()) {
                if (ConvertUtils.status(entry.getValue()).value().intValue() == InstanceStatus.offlineAndNotWork.value().intValue()
                        || ConvertUtils.status(entry.getValue()).value().intValue() == InstanceStatus.onlineButNotWork.value().intValue()) {
                    String urlstr = ConvertUtils.filterZKProvider(entry.getKey(), entry.getValue()[0]);
                    URL url = URL.valueOf(urlstr);
                    addKey = url.getIp() + ":" + url.getPort() + ":" + url.getParameter(Constants.GROUP_KEY, "") + ":" + url.getParameter(Constants.VERSION_KEY, "");
                    addOfflineSet.add(addKey);
                }
            }
        }
        Set<String> delOfflineSet = null;
        if ((zkDel != null && !zkDel.isEmpty())) {
            //从删除的节点中，找到状态为强制下线的节点
            delOfflineSet = new HashSet<String>();
            String delKey = null;
            for (Map.Entry<String, byte[]> entry : zkDel.entrySet()) {
                if (ConvertUtils.status(entry.getValue()).value().intValue() == InstanceStatus.offlineAndNotWork.value().intValue()
                        || ConvertUtils.status(entry.getValue()).value().intValue() == InstanceStatus.onlineButNotWork.value().intValue()) {
                    String urlstr = ConvertUtils.filterZKProvider(entry.getKey(), entry.getValue()[0]);
                    URL url = URL.valueOf(urlstr);
                    delKey = url.getIp() + ":" + url.getPort() + ":" + url.getParameter(Constants.GROUP_KEY, "") + ":" + url.getParameter(Constants.VERSION_KEY, "");
                    delOfflineSet.add(delKey);
                }
            }
        }
        zkHelper.saveOfflineTag(iface, addOfflineSet, delOfflineSet);
    }

    /**
     * 保存日志
     * @param interfaceName
     * @param nodeLogs
     * @param type
     * @param createTime
     * @param creator
     */
    private void save(String interfaceName, Set<String> nodeLogs, String type, Date createTime, String creator) {
        try {
            if(nodeLogs == null) return ;
            List<SynZkDBLog> list = new ArrayList<SynZkDBLog>();
            int i = 0;
            for(String nodeLog : nodeLogs){
                i++;
                SynZkDBLog log = new SynZkDBLog();
                log.setNodeLog(nodeLog);
                log.setType(type);
                log.setCreateTime(createTime);
                log.setCreator(creator);
                list.add(log);
                if (i>=100) {
                    synZkDBLogManager.create(list);
                    i = 0;
                    list.clear();
                }
            }
            synZkDBLogManager.create(list);
        } catch (Exception e) {
            logger.error("save log exception: " + e.getMessage(), e);
        }
    }

    private boolean isSynProvider() {
        return isSynToZkProvider;
    }
}
