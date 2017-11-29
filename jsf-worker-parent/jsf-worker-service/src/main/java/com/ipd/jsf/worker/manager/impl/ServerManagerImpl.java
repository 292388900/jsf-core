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

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.version.common.service.AliasVersionService;
import com.ipd.jsf.worker.dao.InterfaceDataVersionDao;
import com.ipd.jsf.worker.dao.ScanStatusLogDao;
import com.ipd.jsf.worker.dao.ServerDao;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.ScanStatusLog;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.manager.ServerManager;
import com.ipd.jsf.worker.util.DBLog;
import com.ipd.jsf.worker.util.PropertyUtil;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;


@Service
public class ServerManagerImpl implements ServerManager {

    private final static Logger logger = LoggerFactory.getLogger(ServerManagerImpl.class);

    //是否打开lds写入
    private volatile boolean isLdsOpen = false;

    @Autowired
    private ServerDao serverDao;

    @Autowired
    private InterfaceDataVersionDao interfaceDataVersionDao;

    @Autowired
    private AliasVersionService aliasVersionService;

    @Autowired
    private ScanStatusLogDao scanStatusLogDao;

    @PostConstruct
    public void init() {
        try {
            String ldsOpenFlag = PropertyUtil.getProperties("lds.open.flag");
            if (ldsOpenFlag != null) {
                isLdsOpen = Boolean.parseBoolean(ldsOpenFlag);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        if (isLdsOpen) {
            logger.info("writing to lds is open...");
        } else {
            logger.info("writing to lds is closed...");
        }
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#create(java.util.List)
     */
    @Override
    public int create(List<Server> serverList) throws Exception {
        int result = 0;
        if (serverList != null && !serverList.isEmpty()) {
            try {
                result = serverDao.create(serverList);
                Set<Integer> interfaceIdSet = new HashSet<Integer>();
                for (Server server : serverList) {
                    interfaceIdSet.add(server.getInterfaceId());
                }
                if (!interfaceIdSet.isEmpty()) {
                    List<Integer> interfaceIdList = new ArrayList<Integer>();
                    interfaceIdList.addAll(interfaceIdSet);
                    interfaceDataVersionDao.update(interfaceIdList, new Date());
                    aliasVersionService.updateByInterfaceIdList(interfaceIdList, new Date());
                }
            } finally {
            }
        }
        return result;
    }

    @Override
    public int update(Server server) throws Exception {
        int result = 0;
        if (server != null) {
            try {
                result = serverDao.update(server);
            } finally {
            }
        }
        return 0;
    }

    @Override
    public List<String> getUniqKeyList(List<String> uniqKeyList) throws Exception {
        return serverDao.getUniqKeyList(uniqKeyList);
    }

    /*
     * (non-Javadoc)
     * @see ServerManager#updateStatusOnline(Server)
     */
    @Override
    public int updateStatusOnline(Server server) throws Exception {
        return serverDao.updateStatusOnline(server.getId().intValue());
    }

    /*
     * (non-Javadoc)
     * @see ServerManager#updateStatusOffline(Server, long)
     */
    @Override
    public int updateStatusOffline(Server server) throws Exception {
        return serverDao.updateStatusOffline(server.getId().intValue());
    }

    /*
     * (non-Javadoc)
     * @see ServerManager#updateStatusOfflineNotwork(int, long)
     */
    @Override
    public int updateStatusOfflineNotwork(Server server) throws Exception {
        List<Integer> interfaceIdList = new ArrayList<Integer>();
        interfaceIdList.add(server.getInterfaceId());
        int result = serverDao.updateStatusOfflineNotwork(server.getId().intValue());
        interfaceDataVersionDao.update(interfaceIdList, new Date());
        aliasVersionService.updateByInterfaceIdList(interfaceIdList, new Date());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#batchDelete(java.util.List)
     */
    @Override
    public int batchDelete(List<Server> serverList) throws Exception {
        int result = 0;
        if (serverList != null && !serverList.isEmpty()) {
            try {
                //删除server
                result = deleteServer(serverList);
            } catch (Exception e) {
                logger.error("error:{}, delete server list:{}", e.getMessage(), serverList.toString());
                throw e;
            } finally {
            }
        }
        return result;
    }

    /**
     * 在做状态扫描时，记录删除provider日志，仅适用于jsf版本
     * @param serverList
     * @return
     * @throws Exception
     */
    @Transactional
    private int deleteServer(List<Server> serverList) throws Exception {
        int result = 0;
        int limit = 20;
        //1.批量删除provider
        result = deleteById(serverList, SourceType.registry.value());
        //2.记录删除日志
        Date nowTime = new Date();
        String creator = "scanstatus worker";
        List<ScanStatusLog> logList = new ArrayList<ScanStatusLog>();
        ScanStatusLog log = null;
        for (Server server : serverList) {
            log = new ScanStatusLog();
            log.setIp(server.getIp());
            log.setPid(server.getPid());
            log.setInsKey(server.getInsKey());
            log.setInterfaceName(server.getInterfaceName());
            log.setDetailInfo("delete " + server.toString());
            log.setCreateTime(nowTime);
            log.setCreator(creator);
            log.setCreatorIp(WorkerUtil.getWorkerIP());
            log.setType(DataEnum.ScanStatusLogType.server.getValue());
            logList.add(log);
            if (logList.size() > limit) {
                scanStatusLogDao.create(logList);
                logList.clear();
            }
        }
        if (!logList.isEmpty()) {
            scanStatusLogDao.create(logList);
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#getOnlineServersByIns(java.util.List)
     */
    @Override
    public List<Server> getOnlineServersByIns(List<JsfIns> insList) throws Exception {
        if (insList.isEmpty()) return null;
        return serverDao.getOnlineServersByIns(insList);
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#getOfflineServersByIns(java.util.List)
     */
    @Override
    public List<Server> getOfflineServersByIns(List<JsfIns> insList) throws Exception {
        if (insList != null && !insList.isEmpty()) {
            return serverDao.getOfflineServersByIns(insList);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#getOfflineServers()
     */
    @Override
    public List<Server> getOfflineServers() throws Exception {
        return serverDao.getOfflineServers();
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.ServerManager#getListByInterfaceName(java.lang.String)
     */
    @Override
    public List<Server> getListByInterface(InterfaceInfo interfaceInfo) throws Exception {
        if (interfaceInfo.getInterfaceId() != null && interfaceInfo.getInterfaceId().intValue() > 0) {
            return serverDao.getListByInterfaceId(interfaceInfo.getInterfaceId());
        }
        return serverDao.getListByInterfaceName(interfaceInfo.getInterfaceName());
    }

    @Override
    public List<Server> getListByInterfaceId(int interfaceId) throws Exception {
        return serverDao.getListByInterfaceId(interfaceId);
    }

    @Override
    public List<Server> getNoInsServers() throws Exception {
        return serverDao.getNoInsServers();
    }

    private int deleteById(List<Server> serverList, int sourceType) throws Exception {
        int limit = 10;
        int result = 0;
        if (serverList == null || serverList.size() <= 0) {
            return result;
        }
        Set<Integer> interfaceIdSet = new HashSet<Integer>();
        List<Integer> serverIds = new ArrayList<Integer>();
        for (Server s : serverList) {
            serverIds.add(s.getId());
            interfaceIdSet.add(s.getInterfaceId());
            if (serverIds.size() > limit) {
                result += serverDao.deleteById(serverIds, sourceType);
                serverIds.clear();
                if (!interfaceIdSet.isEmpty()) {
                    Date nowTime = new Date();
                    List<Integer> interfaceIdList = new ArrayList<Integer>();
                    interfaceIdList.addAll(interfaceIdSet);
                    interfaceDataVersionDao.update(interfaceIdList, nowTime);
                    aliasVersionService.updateByInterfaceIdList(interfaceIdList, nowTime);
                    interfaceIdSet.clear();
                }
            }
        }
        if (!serverIds.isEmpty()) {
            result += serverDao.deleteById(serverIds, sourceType);
            serverIds.clear();
            if (!interfaceIdSet.isEmpty()) {
                Date nowTime = new Date();
                List<Integer> interfaceIdList = new ArrayList<Integer>();
                interfaceIdList.addAll(interfaceIdSet);
                interfaceDataVersionDao.update(interfaceIdList, nowTime);
                interfaceIdSet.clear();
            }
        }
        DBLog.info("ServerManagerImpl.deleteById, " + "source type:{}, delete server by id:{}", sourceType, serverList.toString());
        return result;
    }

    @Override
    public List<Server> getServersByIns(List<JsfIns> insList) throws Exception {
        if (insList != null && !insList.isEmpty()) {
            return serverDao.getServersByIns(insList);
        }
        return null;
    }

    @Override
    public boolean hafJsfVer(String interfaceName) throws Exception {
        return serverDao.getJsfVerCount(interfaceName) > 0 ? true : false;
    }

    @Override
    public List<Server> getListByInterfaceName(String interfaceName) throws Exception {
        return serverDao.getListByInterfaceName(interfaceName);
    }

    @Override
    public List<Server> getListByInterfaceNameAndAlias(String interfaceName, String alias) throws Exception {
        return serverDao.getListByInterfaceNameAndAlias(interfaceName, alias);
    }

    @Override
    public List<Server> getJsfServers(String interfaceName) {
        return serverDao.getJsfServers(interfaceName);
    }

    @Override
    public int deleteServer4ZkSyn(List<Server> serverList, int sourceType) throws Exception {
        int result = 0;
        if (serverList != null && !serverList.isEmpty()) {
            try {
                //批量删除provider
                result = deleteById(serverList, SourceType.zookeeper.value());
            } finally {
            }
        }
        return result;
    }

    @Override
    public List<Server> getServersForDynamicGrouping(Map<String, Object> params) {
        return serverDao.getServersForDynamicGrouping(params);
    }

}