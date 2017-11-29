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

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.ipd.jsf.worker.domain.*;
import com.ipd.jsf.worker.manager.*;
import com.ipd.jsf.worker.saf1.manager.Saf1InterfaceInfoManager;
import com.ipd.jsf.worker.service.common.Constants;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.common.enumtype.DataEnum.ResourceTypeEnum;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.service.ZkSynToDBService;
import com.ipd.jsf.worker.service.common.URL;
import com.ipd.jsf.worker.service.common.utils.NetUtils;
import com.ipd.jsf.worker.thread.syn.util.ConvertUtils;
import com.ipd.jsf.worker.thread.syn.util.SynUtil;
import com.ipd.jsf.worker.thread.syn.util.ZkHelper;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ZkSynToDBServiceImpl implements ZkSynToDBService {

    private static final Logger logger = LoggerFactory.getLogger(ZkSynToDBServiceImpl.class);
    // 接口缓存
    private ConcurrentHashMap<String, InterfaceInfo> interfaceInfoCacheMap = null;
    //删除日志间隔
    private long interval = 30 * 24 * 3600000L;
    //provider同步开关
    private boolean isSynToDBProvider = false;

    //consumer同步开关
    private boolean isSynToDBConsumer = false;

    private final String provider_ZK_count = "provider.zk.count";

    private final String provider_dbInZk_count = "provider.dbInZk.count";

    private final String provider_db_count = "provider.db.count";

    private final String provider_zkIndb_count = "provider.zkIndb.count";

    private final String provider_dbnew_count = "provider.dbnew.count";

    private final String provider_dbupdate_count = "provider.dbupdate.count";

    private final String provider_dbdel_count = "provider.dbdel.count";

    private final String consumer_ZK_count = "consumer.zk.count";

    private final String consumer_dbInZk_count = "consumer.dbInZk.count";

    private final String consumer_db_count = "consumer.db.count";

    private final String consumer_zkIndb_count = "consumer.zkIndb.count";

    private final String consumer_dbnew_count = "consumer.dbnew.count";

    private final String consumer_dbupdate_count = "consumer.dbupdate.count";

    private final String consumer_dbdel_count = "consumer.dbdel.count";

    private final static String syn_interface_version = "saf1.interface.updateversion";
    
    private static SysParam globalSysParam;

    private ExecutorService threadPool = Executors.newFixedThreadPool(5, new WorkerThreadFactory("zkSynToDBServicePool"));

    @Autowired
    private ServerManager serverManager;

    @Autowired
    private ClientManager clientManager;

    @Autowired
    private InterfaceInfoManager interfaceInfoManager;

    @Autowired
    private InterfaceDataVersionManager interfaceDataVersionManager;

    @Autowired
    private Saf1InterfaceInfoManager saf1InterfaceInfoManager;

    @Autowired
    private SynZkDBLogManager synZkDBLogManager;

    @Autowired
    private SysParamManager sysParamManager;

    private ZkHelper zkHelper;

    private static String host = null;

    static {
        try {
            host = NetUtils.getLocalHost();
            globalSysParam = new SysParam();
            globalSysParam.setKey(syn_interface_version);
            globalSysParam.setName("saf1.0接口信息(部门,erp,备注)同步时间戳");
            globalSysParam.setType(DataEnum.SysParamType.Worker.getValue());
            globalSysParam.setValue("1");
            globalSysParam.setNote("synzkdbworker");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void init() {
        if (zkHelper == null) {
            zkHelper = new ZkHelper();
        }
    }

    @Override
    public void syn(boolean isSynToDBProvider, boolean isSynToDBConsumer) throws Exception {
        List<InterfaceInfo> zkToDBInterfaceInfoList = null;
        try {
            this.isSynToDBProvider = isSynToDBProvider;
            this.isSynToDBConsumer = isSynToDBConsumer;
            if (this.isSynToDBProvider == true || this.isSynToDBConsumer == true) {
                init();
                zkToDBInterfaceInfoList = getZkToDBServiceInfoList();
                if (zkToDBInterfaceInfoList != null && zkToDBInterfaceInfoList.size() > 0) {
                    this.executeSynProvider(zkToDBInterfaceInfoList);
                    this.executeSynConsumer(zkToDBInterfaceInfoList);
                }
                updateInterfaceInfo();
                logger.info("interfaceInfoCacheMap size:{}", interfaceInfoCacheMap.size());
            } else {
                logger.info("zk单向同步provider和consumer到zk的开关都没有打开");
            }
        } catch (Exception e) {
            logger.error("syn error:" + e.getMessage(), e);
        } finally {
            zkToDBInterfaceInfoList = null;
        }
    }

    /**
     * 找出发生变化的interfaceInfo信息
     * 从db和zk中分别获取interfaceInfo信息，然后比较cversion和dataversion信息，如果发生变化，就放入list中
     * 将zk中新增的interfaceInfo放入db中
     * @return
     * @throws Exception 
     */
    private List<InterfaceInfo> getZkToDBServiceInfoList() throws Exception {
        List<InterfaceInfo> zkInterfaceInfoList = zkHelper.getInterfaceInfoList();
        //第一次启动worker时，全部加载，然后return
        if (interfaceInfoCacheMap == null || interfaceInfoCacheMap.isEmpty()) {
            interfaceInfoCacheMap = getInterfaceMapFromList(zkInterfaceInfoList);
            //第一次比较，比较zk比db新增的interface，新增到数据库中
            Map<String, InterfaceInfo> dbInterfaceMap = getInterfaceMapFromList(interfaceInfoManager.getAll());
            List<InterfaceInfo> newInterfaceList = new ArrayList<InterfaceInfo>();
            for (Map.Entry<String, InterfaceInfo> zkIfaceEntry : interfaceInfoCacheMap.entrySet()) {
                if (dbInterfaceMap.get(zkIfaceEntry.getKey()) != null) {
                    zkIfaceEntry.getValue().setInterfaceId(dbInterfaceMap.get(zkIfaceEntry.getKey()).getInterfaceId());
                } else {
                    newInterfaceList.add(zkIfaceEntry.getValue());
                }
            }
            //将新增的interfaceInfo保存到数据库中
            for (InterfaceInfo iface : newInterfaceList) {
                newInterfaceInfo(iface);
            }
            logger.info("zk to db, 刚启动，第一次同步，需要比较全部的接口，接口数  {} 个", zkInterfaceInfoList.size());
            return zkInterfaceInfoList;
        }

        //如果不是第一次
        List<InterfaceInfo> newInterfaceList = new ArrayList<InterfaceInfo>();   //需要新增的接口
        List<InterfaceInfo> zkToDBInterfaceInfoList = new ArrayList<InterfaceInfo>();   //需要比较的接口
        for (InterfaceInfo zkInterfaceInfo : zkInterfaceInfoList) {
            InterfaceInfo cacheInterfaceInfo = interfaceInfoCacheMap.get(zkInterfaceInfo.getInterfaceName());
            if (cacheInterfaceInfo != null) {
                if (isSynProvider()) {
                    //比较版本号，如果版本号不一致，则需要同步
                    if (zkInterfaceInfo.getZkNodeInfo().getServerCversion() != cacheInterfaceInfo.getZkNodeInfo().getServerCversion()
                            || zkInterfaceInfo.getZkNodeInfo().getServerDversion() != cacheInterfaceInfo.getZkNodeInfo().getServerDversion()) {
                        zkToDBInterfaceInfoList.add(zkInterfaceInfo);
                        continue;
                    }
                }
                if (isSynConsumer()) {
                    //比较版本号，如果版本号不一致，则需要同步
                    if (zkInterfaceInfo.getZkNodeInfo().getClientCversion() != cacheInterfaceInfo.getZkNodeInfo().getClientCversion()
                            || zkInterfaceInfo.getZkNodeInfo().getClientDversion() != cacheInterfaceInfo.getZkNodeInfo().getClientDversion()) {
                        zkToDBInterfaceInfoList.add(zkInterfaceInfo);
                        continue;
                    }
                }
            } else {   //不在db中的zk接口信息，需要同步接口
                newInterfaceList.add(zkInterfaceInfo);
            }
        }
        if (newInterfaceList.size() > 0) {
            for (InterfaceInfo iface : newInterfaceList) {
                newInterfaceInfo(iface);
            }
        }
        zkToDBInterfaceInfoList.addAll(newInterfaceList);
        logger.info("zk to db, zk 有变化的接口api有{}个", zkToDBInterfaceInfoList.size());
        return zkToDBInterfaceInfoList;
    }

    /**
     * 将list转换为map
     * @param list
     * @return
     */
    private ConcurrentHashMap<String, InterfaceInfo> getInterfaceMapFromList(List<InterfaceInfo> list) {
        ConcurrentHashMap<String, InterfaceInfo> map = new ConcurrentHashMap<String, InterfaceInfo>();
        for (InterfaceInfo iface : list) {
            if (iface != null && map.get(iface.getInterfaceName().trim()) == null) {
                map.put(iface.getInterfaceName().trim(), iface);
            }
        }
        return map;
    }


    /**
     * 1.初始化Map
     * 2.从zk中获取需要同步的数据
     * 3.从db中获取需要同步的数据
     * 4.同步到db
     * 5.保存同步日志
     * @param zkToDBServiceInfoList
     */
    private void executeSynProvider(List<InterfaceInfo> zkToDBServiceInfoList) {
        if (isSynProvider()) {
            //统计所有接口的zk和db节点操作数量，每个接口对应list中的一个Map
            final List<Map<String, Integer>> statList = Collections.synchronizedList(new ArrayList<Map<String,Integer>>(zkToDBServiceInfoList.size()));
            final CountDownLatch latch = new CountDownLatch(zkToDBServiceInfoList.size());
            final List<Integer> updateInterfaceVersionList = Collections.synchronizedList(new ArrayList<Integer>(zkToDBServiceInfoList.size()));
            for (final InterfaceInfo interfaceInfo : zkToDBServiceInfoList) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //同步每个接口的provider, 并获取统计信息
                            Map<String, Integer> statMap = synProvider(interfaceInfo);
                            statList.add(statMap);
                            //将同步成功的接口的cversion和dversion更新到缓存中
                            if (interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()) != null) {
                                interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()).getZkNodeInfo().setServerCversion(interfaceInfo.getZkNodeInfo().getServerCversion());
                                interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()).getZkNodeInfo().setServerDversion(interfaceInfo.getZkNodeInfo().getServerDversion());
                            } else {
                                interfaceInfoCacheMap.put(interfaceInfo.getInterfaceName(), interfaceInfo);
                            }
                            //记录同步成功的接口id，然后更新saf21库中的接口版本号
                            if (interfaceInfo.getInterfaceId() != null) {
                                updateInterfaceVersionList.add(interfaceInfo.getInterfaceId());
                            }
                        } catch (Exception e) {
                            logger.error("iface:" + interfaceInfo.getInterfaceName() + ",error:" + e.getMessage(), e);
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

            //只有provider同步时，才更新saf21库中的接口版本号
            if (updateInterfaceVersionList.size() > 0) {
                try {
                    interfaceDataVersionManager.update(updateInterfaceVersionList, new Date());
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
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

            logger.info(
                    "provider统计，变化的接口中，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，新增到db的有{}个，更新到db的有{}个，从db删除有{}个",
                    statTotalMap.get(provider_ZK_count),
                    statTotalMap.get(provider_dbInZk_count),
                    statTotalMap.get(provider_db_count),
                    statTotalMap.get(provider_zkIndb_count),
                    statTotalMap.get(provider_dbnew_count),
                    statTotalMap.get(provider_dbupdate_count),
                    statTotalMap.get(provider_dbdel_count));
            statList.clear();
            updateInterfaceVersionList.clear();
        } else {
            logger.info("zk单向同步provider节点到db的开关未打开，此次不执行zk同步provider到db的操作!");
        }
    }

    private Map<String, Integer> synProvider(InterfaceInfo interfaceInfo) throws Exception {
        //zk中的provider
        Map<String, byte[]> zkProviderMap = Maps.newHashMap();
        //zk中已经从db同步到zk上的provider节点
        Map<String, byte[]> dbInZkProvider = Maps.newHashMap();
        //db中的provider节点
        Map<String, byte[]> dbProviderMap = Maps.newHashMap();
        //db中已经从zk同步到db上的provider节点
        Map<String, byte[]> zkInDBProvider = Maps.newHashMap();
        //需要往db中添加的provider节点
        Map<String, byte[]> dbProviderAdd = null;
        //需要从db中删除的provider节点
        Map<String, byte[]> dbProviderDel = null;
        //统计数量map
        Map<String, Integer> statMap = new HashMap<String, Integer>();
        try {
            // 1. 获取zk的provider
            zkHelper.initZkProviders(interfaceInfo.getInterfaceName(), zkProviderMap, dbInZkProvider);

            // 2. 获取db的provider
            initDBProviders(interfaceInfo, dbProviderMap, zkInDBProvider);
    
            // 3. 将zk的provider和已经在db中的zk的provider进行比较，并过滤，生成需要添加到db的provider
            Map<String, byte[]> zkInDBMapTemp = Maps.newHashMap();
            for (String key : zkInDBProvider.keySet()) {
                URL url = URL.valueOf(key);
                url = url.removeParameter(Constants.ID_KEY);// 忽略id比较
                zkInDBMapTemp.put(url.toFullString(), zkInDBProvider.get(key));
            }
            dbProviderAdd = Maps.difference(zkProviderMap, zkInDBMapTemp).entriesOnlyOnLeft();
            //过滤特殊字符
            dbProviderAdd = filterGroupOrVersion(dbProviderAdd);
            
            // 4.保存provider到数据库中
            int newcount = saveProviderToDB(interfaceInfo.getInterfaceName(), dbProviderAdd, SynUtil.ZK);
            int updatecount = dbProviderAdd.size() - newcount;

            // 5.从数据库中删除减少的provider
            dbProviderDel = deleteProviderFromDB(interfaceInfo.getInterfaceName(), zkInDBProvider, zkProviderMap);
    
            // 6. 统计数量
            // (1) zk 的provider数
            statMap.put(provider_ZK_count, zkProviderMap.size());
    
            // (2) db在zk中 的provider数
            statMap.put(provider_dbInZk_count, dbInZkProvider.size());
            
            // (3) db中 的provider数
            statMap.put(provider_db_count, dbProviderMap.size());
            
            // (4) db在zk中 的provider数
            statMap.put(provider_zkIndb_count, zkInDBProvider.size());
            
            // (5) 新增到db 的provider数
            statMap.put(provider_dbnew_count, newcount);
            // 更新到db的provider数
            statMap.put(provider_dbupdate_count, updatecount);
            
            // (6) 从db删除 的provider数
            statMap.put(provider_dbdel_count, dbProviderDel.size());

            if (logger.isDebugEnabled()) {
                logger.debug(interfaceInfo.getInterfaceName() + 
                    ", provider统计，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，新增到db的有{}个，更新到db的有{}个，从db删除有{}个",
                    zkProviderMap.size(),
                    dbInZkProvider.size(),
                    dbProviderMap.size(),
                    zkInDBProvider.size(),
                    newcount,
                    updatecount,
                    dbProviderDel.size());
            }
        } finally {
            //清空
            zkProviderMap = null;
            dbInZkProvider = null;
            dbProviderMap = null;
            zkInDBProvider = null;
            dbProviderAdd = null;
            dbProviderDel = null;
        }
        return statMap;
    }

    /**
     * @param interfaceInfo
     * @param dbProviderMap
     * @param zkInDBProvider
     * @throws Exception
     */
    private void initDBProviders(InterfaceInfo interfaceInfo, Map<String, byte[]> dbProviderMap, Map<String, byte[]> zkInDBProvider) throws Exception {
        List<Server> serverList = serverManager.getListByInterface(interfaceInfo);
        if (serverList != null) {
            for (Server server : serverList) {
                try {
                    String url = ConvertUtils.convertDBServer2URL(server, interfaceInfo.getInterfaceName(), true);
                    if (SourceType.zookeeper.value() == server.getSrcType()) {
                        zkInDBProvider.put(url, ConvertUtils.status(server.getStatus()));
                    } else {
                        dbProviderMap.put(url + SynUtil.FROM_DB, ConvertUtils.status(server.getStatus()));// 加上节点状态
                    }
                } catch (Exception e) {
                    logger.error("when init providers from db, server: {}, error: {}", server.toString(), e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * save provider to db
     * @param source
     * @return
     * @throws Exception
     */
    private int saveProviderToDB(String interfaceName, Map<String, byte[]> urlMap, String source) throws Exception {
        if(urlMap == null || urlMap.size() == 0) return 0;
        //url->server->servicInfo
        List<Server> serverList = convertServer(interfaceName, ConvertUtils.convertUrlServer(urlMap));
        
        //先更新provider，如果没有记录，就放入新增list中
        List<Server> newServerList = new ArrayList<Server>();
        for (Server server : serverList) {
            //更新返回0，说明没该记录，加入新增list中
            if (serverManager.update(server) == 0) {
                newServerList.add(server);
            }
        }

        //新增provider
        int itemNumPerPage = 10;
        if (newServerList.size() <= itemNumPerPage) {
            createServer(newServerList);
        } else {
            int totalSize = newServerList.size();
            int totalPage = totalSize % itemNumPerPage == 0 ? totalSize / itemNumPerPage : totalSize / itemNumPerPage + 1;
            int start = 0;
            int end = 0;
            for (int i = 0; i < totalPage; i++) {
                start = i * itemNumPerPage;
                end = (i + 1) * itemNumPerPage;
                end = end > totalSize ? totalSize : end;
                List<Server> temp = newServerList.subList(start, end);
                createServer(temp);
            }
        }
        if (serverList.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.info(interfaceName + ", add provider size:{}", serverList.size());
            }
        }
        savelog(interfaceName, urlMap, "DBAdd", new Date(), host);
        return newServerList.size();
    }

    /**
     * 创建server
     * @param list
     */
    private void createServer(List<Server> list) {
        try {
            serverManager.create(list);
        } catch (Exception e) {
            //如果主键冲突,就找出哪些在数据库中，然后将不在数据库中的server插入, 对于冲突的就不在做处理了，因为之前已经处理过了
            if (e instanceof DuplicateKeyException || e instanceof DataIntegrityViolationException || e instanceof MySQLIntegrityConstraintViolationException) {
                try {
                    logger.warn("server uniqkey DuplicatKey: " + list.toString());
                    //找到哪些server在数据库中
                    List<String> uniqKeyList = new ArrayList<String>();
                    for (Server server : list) {
                        uniqKeyList.add(server.getUniqKey());
                    }
                    List<String> resultUniqKeyList = serverManager.getUniqKeyList(uniqKeyList);
                    //比较server的uniqkey，找出需要插入的server
                    List<Server> temp = new ArrayList<Server>();
                    for (Server server : list) {
                        if (!resultUniqKeyList.contains(server.getUniqKey())) {
                            try {
                                temp.add(server);
                                //将没有冲突的server，再重新插入一遍
                                serverManager.create(temp);
                                temp.clear();
                            } catch (Exception e1) {
                                logger.warn("create server error: " + e.getMessage() + ", server" + server.toString());
                            }
                        }
                    }
                } catch (Exception e1) {
                    logger.error(e1.getMessage(), e1);
                }
            } else {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 入库前转为server
     * @param serversMap
     * @return
     */
    private List<Server> convertServer(String interfaceName, List<Server> serverList) throws Exception {
        if (serverList == null) return null;
        List<Server> servers = new ArrayList<Server>();
        try {
            InterfaceInfo info = interfaceInfoCacheMap.get(interfaceName);
            if (info == null || info.getInterfaceId() == null || info.getInterfaceId().intValue() <= 0) {
                // 存入db，同时获取id,一次遍历，只存一次
                info = new InterfaceInfo();
                info.setInterfaceName(interfaceName);
                newInterfaceInfo(info);
            }
            List<Server> temp = ConvertUtils.setInfoToServer(serverList, info);
            if (temp != null && !temp.isEmpty()) {
                servers.addAll(temp);
            }
        } catch (Exception e) {
            logger.error(interfaceName + ", server:" + JSON.toJSONString(serverList) + ", error:" + e.getMessage(), e);
            throw e;
        }
        return servers;
    }

    /**
     * delete providers from db
     * @throws Exception
     */
    private Map<String, byte[]> deleteProviderFromDB(String interfaceName, Map<String, byte[]> zkInDBProvider, Map<String, byte[]> zkProviderMap) throws Exception {
        Map<String, byte[]> dbProviderDel = Maps.newHashMap();
        Map<String, byte[]> zkInDBMapTemp = Maps.newHashMap();
        Map<String, byte[]> zkTemp = Maps.newHashMap();
        Map<String, String> zkInDBMap = Maps.newHashMap();
        try {
            for (String key : zkInDBProvider.keySet()) {
                try {
                    URL url = URL.valueOf(key);
                    //删除server时，需要serverId
                    url = url.removeParameter(Constants.ID_KEY);
                    url = url.removeParameter(Constants.STATUS_KEY);
                    url = url.removeParameter(Constants.WEIGHT_KEY);
                    zkInDBMap.put(url.toFullString(), key);
                    zkInDBMapTemp.put(url.toFullString(), zkInDBProvider.get(key));
                } catch (Exception e) {
                    logger.error("deleteProviderFromDB, key:{}, value:{}, error: {}", key, zkInDBProvider.get(key), e.getMessage());
                    throw e;
                }
            }

            for (String key : zkProviderMap.keySet()) {
                try {
                    URL url = URL.valueOf(key);
                    url = url.removeParameter(Constants.STATUS_KEY);
                    url = url.removeParameter(Constants.WEIGHT_KEY);
                    zkTemp.put(url.toFullString(), zkProviderMap.get(key));
                } catch (Exception e) {
                    logger.error("deleteProviderFromDB, key:{}, value:{}, error: {}", key, zkProviderMap.get(key), e.getMessage());
                    throw e;
                }
            }

            Map<String, byte[]> diffMap = Maps.difference(zkTemp, zkInDBMapTemp).entriesOnlyOnRight();
            for (String temp : diffMap.keySet()) {
                dbProviderDel.put(zkInDBMap.get(temp), diffMap.get(temp));
            }
            List<Server> servers = ConvertUtils.convertUrlServer(dbProviderDel);
            if (servers != null && !servers.isEmpty()) {
                int interfaceId = interfaceInfoCacheMap.get(interfaceName).getInterfaceId();
                for (Server s : servers) {
                    s.setInterfaceId(interfaceId);
                    s.setInterfaceName(interfaceName);
                }
                serverManager.deleteServer4ZkSyn(servers, SourceType.zookeeper.value());
                if (logger.isDebugEnabled()) {
                    logger.info("interfaceName:{}, delete provider size: {}", interfaceName, servers.size());
                }
            }
            savelog(interfaceName, dbProviderDel, "DBDel", new Date(), host);
        } finally {
            zkInDBMapTemp = null;
            zkTemp = null;
            zkInDBMap = null;
        }
        return dbProviderDel;
    }

    /**
     * @param zkToDBServiceInfoList
     */
    private void executeSynConsumer(List<InterfaceInfo> zkToDBServiceInfoList) {
        if (isSynConsumer()) {
            final List<Map<String, Integer>> statList = Collections.synchronizedList(new ArrayList<Map<String,Integer>>());
            final CountDownLatch latch = new CountDownLatch(zkToDBServiceInfoList.size());
            for (final InterfaceInfo interfaceInfo : zkToDBServiceInfoList) {
                threadPool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //同步consumer
                            Map<String, Integer> statMap = synConsumer(interfaceInfo);
                            statList.add(statMap);
                            //如果同步成功，将zk接口上的version更新到缓存中
                            if (interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()) != null) {
                                interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()).getZkNodeInfo().setClientCversion(interfaceInfo.getZkNodeInfo().getClientCversion());
                                interfaceInfoCacheMap.get(interfaceInfo.getInterfaceName()).getZkNodeInfo().setClientDversion(interfaceInfo.getZkNodeInfo().getClientDversion());
                            } else {
                                interfaceInfoCacheMap.put(interfaceInfo.getInterfaceName(), interfaceInfo);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
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
            //汇总统计
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

            logger.info(
                    "consumer统计，变化的接口中，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，新增到db的有{}个，更新到db的有{}个，从db删除有{}个",
                    statTotalMap.get(consumer_ZK_count),
                    statTotalMap.get(consumer_dbInZk_count),
                    statTotalMap.get(consumer_db_count),
                    statTotalMap.get(consumer_zkIndb_count),
                    statTotalMap.get(consumer_dbnew_count),
                    statTotalMap.get(consumer_dbupdate_count),
                    statTotalMap.get(consumer_dbdel_count));
            statList.clear();
        } else {
            logger.info("zk单向同步consumer节点到db的开关未打开，此次不执行zk同步consumer到db的操作!");
        }
    }

    /**
     * 1.初始化Map
     * 2.从zk中获取需要同步的数据
     * 3.从db中获取需要同步的数据
     * 4.保存到db
     * 5.从db删除
     * 6.保存同步日志
     * @param interfaceInfo
     * @throws Exception
     */
    private Map<String, Integer> synConsumer(InterfaceInfo interfaceInfo) throws Exception {
        //zk中的consumer
        Map<String, byte[]> zkConsumerMap = Maps.newHashMap();
        //zk中已经从db同步到zk上的consumer节点
        Map<String, byte[]> dbInZkConsumer = Maps.newHashMap();
        //db中的consumer节点
        Map<String, byte[]> dbConsumerMap = Maps.newHashMap();
        //db中已经从zk同步到db上的consumer节点
        Map<String, byte[]> zkInDBConsumer = Maps.newHashMap();
        //需要往db中添加的consumer节点
        Map<String, byte[]> dbConsumerAdd = null;
        //需要从db中删除的consumer节点
        Map<String, byte[]> dbConsumerDel = null;
        //统计数量
        Map<String, Integer> statMap = new HashMap<String, Integer>();

        try {
            // 1. 获取zk上的consumer节点
            zkHelper.initZkConsumers(interfaceInfo.getInterfaceName(), zkConsumerMap, dbInZkConsumer);
    
            // 2.获取db上的consumer节点
            List<Client> clientList = clientManager.getListByInterfaceName(interfaceInfo.getInterfaceName());
            if (clientList != null) {
                for (Client client : clientList) {
                    try {
                        String url = ConvertUtils.convertDBConsumer2URL(client, interfaceInfo.getInterfaceName());
                        if (SourceType.zookeeper.value() == client.getSrcType()) {
                            zkInDBConsumer.put(url, SynUtil.useful);
                        } else {
                            dbConsumerMap.put(url + SynUtil.FROM_DB, SynUtil.useful);
                        }
                    } catch (Exception e) {
                        logger.error("when init consumers from db, server: {}, error: {}", JSON.toJSONString(client), e.getMessage());
                        throw e;
                    }
                }
            }
    
            // 3. 将zk的consumer和已经在db中的zk的consumer进行比较，并过滤，生成需要添加到db的consumer
            Map<String, byte[]> zkInDBMapTemp = Maps.newHashMap();
            for (Entry<String, byte[]> entry: zkInDBConsumer.entrySet()) {
                URL url = URL.valueOf(entry.getKey());
                url = url.removeParameter(Constants.ID_KEY);//忽略id比较
                zkInDBMapTemp.put(url.toFullString(), entry.getValue());
            }
            dbConsumerAdd = Maps.difference(zkConsumerMap, zkInDBMapTemp).entriesOnlyOnLeft();
            dbConsumerAdd = filterGroupOrVersion(dbConsumerAdd);

            // 4.保存consumer到数据库中
            int newcount = saveConsumerToDB(interfaceInfo.getInterfaceName(), dbConsumerAdd, SynUtil.ZK);
            int updatecount = dbConsumerAdd.size() - newcount;
            
            // 5.从数据库中删除减少的consumer
            dbConsumerDel = deleteConsumerFromDB(interfaceInfo.getInterfaceName(), zkInDBConsumer, zkConsumerMap);

            // 6. 统计数量
            // (1) zk 的consumer数
            statMap.put(consumer_ZK_count, zkConsumerMap.size());

            // (2) db在zk中 的consumer数
            statMap.put(consumer_dbInZk_count, dbInZkConsumer.size());

            // (3) db中 的consumer数
            statMap.put(consumer_db_count, dbConsumerMap.size());

            // (4) db在zk中 的consumer数
            statMap.put(consumer_zkIndb_count, zkInDBConsumer.size());

            // (5) 新增到db 的consumer数
            statMap.put(consumer_dbnew_count, newcount);
            // 更新到db的consumer数
            statMap.put(consumer_dbupdate_count, updatecount);

            // (6) 从db删除 的consumer数
            statMap.put(consumer_dbdel_count, dbConsumerDel.size());

            if (logger.isDebugEnabled()) {
                logger.debug(interfaceInfo.getInterfaceName() + 
                    ", consumer统计，同步之前，zk中有{}个，db已同步到zk有{}个; db中有{}个，zk已同步到db有{}个。本次同步，新增到db的有{}个，更新到db的有{}个，从db删除有{}个",
                    zkConsumerMap.size(),
                    dbInZkConsumer.size(),
                    dbConsumerMap.size(),
                    zkInDBConsumer.size(),
                    newcount,
                    updatecount,
                    dbConsumerDel.size());
            }
        } finally {
            zkConsumerMap = null;
            dbInZkConsumer = null;
            dbConsumerMap = null;
            zkInDBConsumer = null;
            dbConsumerAdd = null;
            dbConsumerDel = null;
        }
        return statMap;
    }

    /**
     * 保存 db
     * @param clientMap
     * @param source
     * @return
     * @throws Exception
     */
    private int saveConsumerToDB(String interfaceName, Map<String, byte[]> clientMap, String source) throws Exception {
        if(clientMap == null || clientMap.size() == 0) return 0;
        List<Client> clientList = convertClient(interfaceName, ConvertUtils.convertUrlClient(clientMap));
        
        //先更新consumer，如果没有记录，就放入新增list中
        List<Client> newClientList = new ArrayList<Client>();
        for (Client client : clientList) {
            //更新返回0，说明没该记录，加入新增list中
            if (clientManager.update(client) == 0) {
                newClientList.add(client);
            }
        }

        //新增consumer
        int itemNumPerPage = 20;
        if (newClientList.size() <= itemNumPerPage) {
            createClient(newClientList);
        } else {
            int totalSize = newClientList.size();
            int totalPage = totalSize % itemNumPerPage == 0 ? totalSize / itemNumPerPage : totalSize / itemNumPerPage + 1;
            int start = 0;
            int end = 0;
            for (int i = 0; i < totalPage; i++) {
                start = i * itemNumPerPage;
                end = (i + 1) * itemNumPerPage;
                end = end > totalSize ? totalSize : end;
                List<Client> temp = newClientList.subList(start, end);
                createClient(temp);
            }
        }
        if (clientList.size() > 0) {
            if (logger.isDebugEnabled()) {
                logger.debug(interfaceName + ", add consumer size:{}", clientList.size());
            }
        }
        savelog(interfaceName, clientMap, "DBAdd", new Date(), host);
        return newClientList.size();
    }

    /**
     * 创建client
     * @param list
     */
    private void createClient(List<Client> list) {
        try {
            clientManager.create(list);
        } catch (Exception e) {
            //如果主键冲突,就找出哪些在数据库中，然后将不在数据库中的client插入, 对于冲突的就不在做处理了，因为之前已经处理过了
            if (e instanceof DuplicateKeyException || e instanceof DataIntegrityViolationException || e instanceof MySQLIntegrityConstraintViolationException) {
                logger.warn("client uniqkey DuplicatKey: " + list.toString());
                List<String> uniqKeyList = new ArrayList<String>();
                try {
                    //找到哪些client在数据库中
                    for (Client client : list) {
                        uniqKeyList.add(client.getUniqKey());
                    }
                    List<String> resultUniqKeyList = clientManager.getUniqKeyList(uniqKeyList);
                    //比较client的uniqkey，找出需要插入的client
                    Map<String, Client> temp = new HashMap<String, Client>();
                    for (Client client : list) {
                        if (!resultUniqKeyList.contains(client.getUniqKey())) {
                            temp.put(client.getUniqKey(), client);
                        }
                    }
                    //将没有冲突的client，再重新插入一遍
                    clientManager.create(new ArrayList<Client>(temp.values()));
                } catch (Exception e1) {
                    logger.error("uniqkeylist: " + uniqKeyList.toString() + ", error:" + e1.getMessage(), e1);
                }
            } else {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private List<Client> convertClient(String interfaceName, List<Client> clientList) throws Exception {
        if (clientList == null) return null;
        List<Client> clients = new ArrayList<Client>();
        try {
            InterfaceInfo info = interfaceInfoCacheMap.get(interfaceName);
            if (info == null || info.getInterfaceId() == null || info.getInterfaceId().intValue() <= 0) {
                // 存入db，同时获取id,一次遍历，只存一次
                info = new InterfaceInfo();
                info.setInterfaceName(interfaceName);
                newInterfaceInfo(info);
            }
            clients.addAll(ConvertUtils.setInfoToClient(clientList, info));
        } catch (Exception e) {
            logger.error(interfaceName + ", client:" + JSON.toJSONString(clientList) + ", error:" + e.getMessage(), e);
            throw e;
        }
        return clients;
    }

    /**
     * 从数据库中将consumer删除
     * @param interfaceName
     * @param zkInDBConsumer
     * @param zkConsumerMap
     * @return
     * @throws Exception
     */
    private Map<String, byte[]> deleteConsumerFromDB(String interfaceName, Map<String, byte[]> zkInDBConsumer, Map<String, byte[]> zkConsumerMap) throws Exception {
        Map<String, byte[]> dbConsumerDel = Maps.newHashMap();
        Map<String, byte[]> zkInDBMapTemp = Maps.newHashMap();
        Map<String, byte[]> zkTemp = Maps.newHashMap();
        Map<String, String> agent = Maps.newHashMap();
        try {
            for (String key : zkInDBConsumer.keySet()) {
                try {
                    URL url = URL.valueOf(key);
                    url = url.removeParameter(Constants.ID_KEY);
                    url = url.removeParameter(Constants.SAFVERSION_KEY);
                    agent.put(url.toFullString(), key);
                    zkInDBMapTemp.put(url.toFullString(), zkInDBConsumer.get(key));
                } catch (Exception e) {
                    logger.error("zkInDBConsumer, key:{}, value:{}, error: {}", key, zkInDBConsumer.get(key), e.getMessage());
                    throw e;
                }
            }
    
            for (String key : zkConsumerMap.keySet()) {
                try {
                    URL url = URL.valueOf(key);
                    url = url.removeParameter(Constants.SAFVERSION_KEY);
                    zkTemp.put(url.toFullString(), zkConsumerMap.get(key));
                } catch (Exception e) {
                    logger.error("zkConsumerMap, key:{}, value:{}, error: {}", key, zkConsumerMap.get(key), e.getMessage());
                    throw e;
                }
            }
    
            Map<String, byte[]> diffMap = Maps.difference(zkTemp, zkInDBMapTemp).entriesOnlyOnRight();
            for (String temp : diffMap.keySet()) {
                dbConsumerDel.put(agent.get(temp), diffMap.get(temp));
            }
            List<Client> clients = ConvertUtils.convertUrlClient(dbConsumerDel);

            clientManager.deleteById(clients, SourceType.zookeeper.value());
            if (clients != null && !clients.isEmpty()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("interfaceName:{}, delete consumer size: {}", interfaceName, clients.size());
                }
            }
            savelog(interfaceName, dbConsumerDel, "DBDel", new Date(), host);
        } finally {
            zkInDBMapTemp = null;
            zkTemp = null;
            agent = null;
        }
        return dbConsumerDel;
    }

    /**
     * 过滤一下：不符合标准的version, 含有： %
     * @return
     */
    private Map<String, byte[]> filterGroupOrVersion(Map<String, byte[]> dbAdd) throws Exception {
        Map<String, byte[]> invalid = Maps.newHashMap();
        for (Entry<String, byte[]> entry: dbAdd.entrySet()) {
            try {
                URL url = URL.valueOf(entry.getKey());
                if (StringUtils.hasText(url.getParameter(Constants.VERSION_KEY))) {
                    if(url.getParameter(Constants.VERSION_KEY).contains("%")){
                        logger.warn("节点{}的version包含不支持的字符, 过滤不同步到db", url.toFullString());
                        invalid.put(url.toFullString(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                logger.error("filterGroupOrVersion error, key:" + entry.getKey(), e);
                throw e;
            }
        }
        
        return Maps.difference(dbAdd, invalid).entriesOnlyOnLeft();
    }

    /**
     * 保存日志
     * @param nodeLogs
     * @param createTime
     * @param type
     * @param creator
     */
    private void savelog(String interfaceName, Map<String, byte[]> nodeLogs, String type, Date createTime, String creator) {
        try {
            if(nodeLogs == null) return ;
            List<SynZkDBLog> list = new ArrayList<SynZkDBLog>();
            byte i = 0;
            SynZkDBLog log = null;
            for(String nodeLog : nodeLogs.keySet()){
                log = new SynZkDBLog();
                log.setInterfaceName(interfaceName);
                log.setNodeLog(nodeLog);
                log.setType(type);
                log.setCreateTime(createTime);
                log.setCreator(creator);
                list.add(log);
                i++;
                if (i >= 50) {
                    synZkDBLogManager.create(list);
                    i = 0;
                    list.clear();
                }
            }
            synZkDBLogManager.create(list);
            list = null;
        } catch (Exception e) {
            logger.error("save log exception: " + e.getMessage(), e);
        }
    }

    private void newInterfaceInfo(InterfaceInfo iface) throws Exception {
        if (iface.getInterfaceName().equals("/")) {
            throw new Exception("invalid interface : /");
        }
        Saf1InterfaceInfo saf1Iface = saf1InterfaceInfoManager.getSaf1InterfaceByName(iface.getInterfaceName());
        String[] ps = null;

        if (saf1Iface != null) {
        	String ownerUser = saf1Iface.getViewUsers();
            if(ownerUser != null){
            	ownerUser = ownerUser.replace(";", ",").replace("，", ",").replace("；", ",");
            	ps = ownerUser.split(",");
            }
        	
            Date date  = new Date();
            iface.setOwnerUser(ownerUser);
            iface.setDepartment(saf1Iface.getDepartment());
            iface.setRemark(saf1Iface.getRemark());
            iface.setCreator("synzkdbworker");
            iface.setSource((byte)1);
            iface.setModifier(saf1Iface.getCreator());
            iface.setImportant(saf1Iface.getImportant());
            iface.setCreateTime(date);
            iface.setUpdateTime(date);
        }
        interfaceInfoManager.newInterfaceInfo(iface);
        InterfaceInfo interfaceInfo = interfaceInfoCacheMap.get(iface.getInterfaceName());
        if (interfaceInfo != null && iface.getInterfaceId() != null) {
            interfaceInfo.setInterfaceId(iface.getInterfaceId());
        }
        
        //插入资源erp关联表 -- start
        try {
			if(ps != null && interfaceInfo != null){
				List<UserResource> urs = new ArrayList<UserResource>();
				for(String pin : ps ){
					UserResource ur = new UserResource();
					ur.setPin(pin);
					ur.setResType(ResourceTypeEnum.INTERFACE.getValue());
					ur.setResId(interfaceInfo.getInterfaceId());
					
					urs.add(ur);
				}
				if(urs.size() > 0){
					interfaceInfoManager.batchInsert(urs);
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        // --end
    }

    /**
     * 将saf1.0数据库中的接口信息，同步到saf2.1的数据库中
     * 1. 获取上一次的执行时间.(如果数据库中没有，就创建一个)
     * 2. 根据执行时间，获取执行时间以后的变化的saf1.0的接口信息
     * 3. 将变化的接口信息保存saf2.0数据库中
     * 4. 保存执行时间到内存和数据库中
     */
    private void updateInterfaceInfo() {
        try {
            Date currentDate = new Date();
            Date lastQueueTime = new Date(Long.parseLong(globalSysParam.getValue()));
            //1. 获取上一次的执行时间.(如果数据库中没有，就创建一个)
            if (lastQueueTime.getTime() == 1) {   //如果第一次执行, 先从数据库中读取上次执行时间
                SysParam temp = sysParamManager.get(globalSysParam.getKey(), globalSysParam.getType());
                if (temp == null) {  //如果数据库中没有执行时间，就创建一个
                    globalSysParam.setCreateTime(currentDate);
                    globalSysParam.setUpdateTime(currentDate);
                    sysParamManager.create(globalSysParam);
                } else {
                    globalSysParam.setId(temp.getId());
                    lastQueueTime = new Date(Long.parseLong(temp.getValue()));
                }
            }
            //2. 根据执行时间，获取执行时间以后的变化的saf1.0的接口信息
            List<Saf1InterfaceInfo> saf1IfaceList = saf1InterfaceInfoManager.getSaf1InterfaceAfterTime(lastQueueTime);
            if (saf1IfaceList != null && !saf1IfaceList.isEmpty()) {
                for (Saf1InterfaceInfo saf1Iface : saf1IfaceList) {
                	
                	String ownerUser = saf1Iface.getViewUsers();
                    String[] ps = null;
                    if(ownerUser != null){
                    	ownerUser = ownerUser.replace(";", ",").replace("，", ",").replace("；", ",");
                    	ps = ownerUser.split(",");
                    }
                	
                    //3. 将变化的接口信息保存saf2.0数据库中
                    InterfaceInfo iface = new InterfaceInfo();
                    iface.setInterfaceName(saf1Iface.getInterfaceName());
                    iface.setDepartment(saf1Iface.getDepartment());
                    iface.setOwnerUser(ownerUser);
                    iface.setRemark(saf1Iface.getRemark());
                    iface.setUpdateTime(currentDate);
                    interfaceInfoManager.updateInterfaceInfo(iface);
                    
                    
                    //更新资源erp关联表  --start
            		try {
            			if(ps != null){
            				InterfaceInfo ifaceInfo = interfaceInfoManager.getByInterfaceName(saf1Iface.getInterfaceName());
            				List<String> erps = interfaceInfoManager.findAuthErps(ifaceInfo.getInterfaceId(), ResourceTypeEnum.INTERFACE.getValue());
    						List<UserResource> urs = new ArrayList<UserResource>();
    						for(String pin : ps ){
    							if(erps.contains(pin)){
    								continue;
    							}
    							UserResource ur = new UserResource();
    							ur.setPin(pin);
    							ur.setResType(ResourceTypeEnum.INTERFACE.getValue());
    							ur.setResId(ifaceInfo.getInterfaceId());
    							
    							urs.add(ur);
    						}
    						if(urs.size() > 0){
    							interfaceInfoManager.batchInsert(urs);
    						}
            			}
					} catch (Exception e) {
						logger.error(saf1Iface.getInterfaceName() + ", " + e.getMessage(), e);
					}
            		// --end
                    
                }
                //4. 保存执行时间到内存和数据库中
                lastQueueTime = currentDate;
                globalSysParam.setUpdateTime(currentDate);
                globalSysParam.setValue(String.valueOf(lastQueueTime.getTime()));
                sysParamManager.update(globalSysParam);
                logger.info("同步接口信息(部门，负责人erp，备注)完成, 变化接口有：{}个", saf1IfaceList.size());
            } else {
                logger.info("同步接口信息(部门，负责人erp，备注)，没有需要同步的");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    
    private boolean isSynProvider() {
        return isSynToDBProvider;
    }
    
    private boolean isSynConsumer() {
        return isSynToDBConsumer;
    }

    @Override
    public void deleteByTime() throws Exception {
        long start = System.currentTimeMillis();
        Date time = new Date(System.currentTimeMillis() - interval);
        try {
            int result = synZkDBLogManager.deleteByTime(time);
            logger.info("synzkdb log delete size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    
    }

}
