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
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.version.common.service.AliasVersionService;
import com.ipd.jsf.worker.dao.InterfaceDataVersionDao;
import com.ipd.jsf.worker.dao.ScanServerDao;
import com.ipd.jsf.worker.dao.ScanStatusLogDao;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.ScanStatusLog;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.manager.ScanServerManager;
import com.ipd.jsf.worker.util.DBLog;
import com.ipd.jsf.worker.util.PropertyUtil;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;

@Service
public class ScanServerManagerImpl implements ScanServerManager {
	
	public final static int DELTYPE_TAGDEL = 5;
	private final int DELTYPE_DELETE = 9;
	private String logCreator = "scanstatus worker";

    private final static Logger logger = LoggerFactory.getLogger(ScanServerManagerImpl.class);
    //是否打开lds写入
    private volatile boolean isLdsOpen = false;

    @Autowired
    private ScanServerDao scanServerDao;

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
     * @see ScanServerManager#updateStatusOffline(java.lang.String)
     */
    @Override
    public int updateStatusOffline(List<Server> serverList) throws Exception {
        return scanServerDao.updateStatusOffline(serverList, InstanceStatus.offline.value().intValue());
    }

	@Override
	public int tagServerToDel(List<Server> serverList) throws Exception {
		if (serverList == null || serverList.isEmpty()) return 0;
		long optTime = DateTimeZoneUtil.getTargetTime().getTime();
		int count = 0;
		List<Integer> idList = new ArrayList<Integer>();
		for (Server server : serverList) {
			idList.add(server.getId());
		}
		/**
		for (Server server : serverList) {
			count += scanServerDao.tagServerToDel(server.getUniqKey(), optTime, InstanceStatus.deleted.value().intValue());
		}
		try {
			updateInterfaceDataVersion(serverList);
			recordLog(serverList, DELTYPE_TAGDEL);
		} catch (Exception e) {
			logger.error("server list:" + serverList.toString() + ", error:" + e.getMessage(), e);
		}
		 */
		try {
			if (!idList.isEmpty()) {
				count += scanServerDao.batchTagServerToDelByIds(idList, optTime, InstanceStatus.deleted.value().intValue());
			}
			if (count > 0) {
				updateInterfaceDataVersion(serverList);
				recordLog(serverList, DELTYPE_TAGDEL, logCreator);
			}
		} catch (Exception e) {
			logger.error("server list:" + serverList.toString() + ", error:" + e.getMessage(), e);
		}
		return count;
	}

    private void updateInterfaceDataVersion(List<Server> serverList) {
    	try {
    		int limit = 20;
    		if (serverList == null || serverList.size() <= 0) {
    			return;
    		}
    		Set<Integer> interfaceIdSet = new HashSet<Integer>();
    		List<Integer> interfaceIdList = new ArrayList<Integer>();
    		for (Server s : serverList) {
    			interfaceIdSet.add(s.getInterfaceId());
    			if (interfaceIdSet.size() >= limit) {
    				Date nowTime = new Date();
    				interfaceIdList.addAll(interfaceIdSet);
    				interfaceDataVersionDao.update(interfaceIdList, nowTime);
//    				aliasVersionService.updateByInterfaceIdList(interfaceIdList, nowTime);
    				interfaceIdSet.clear();
    				interfaceIdList.clear();
    			}
    		}
    		if (!interfaceIdSet.isEmpty()) {
    			Date nowTime = new Date();
    			interfaceIdList.addAll(interfaceIdSet);
    			interfaceDataVersionDao.update(interfaceIdList, nowTime);
//    			aliasVersionService.updateByInterfaceIdList(interfaceIdList, nowTime);
    			interfaceIdSet.clear();
    			interfaceIdList.clear();
    		}
    		aliasVersionService.updateByServerList(getIfaceServers(serverList), new Date());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
    }

    /**
     * 在做状态扫描时，记录删除provider日志，仅适用于jsf版本
     * @param serverList
     * @return
     * @throws Exception
     */
    private int recordLog(List<Server> serverList, int delType, String creator) throws Exception {
        int result = 0;
        int limit = 20;
        //2.记录删除日志
        Date nowTime = new Date();
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
        	log.setDelType(delType);
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

	@Override
	public int recordLog_Tagdel(List<Server> serverList, String creator) throws Exception {
		return recordLog(serverList, DELTYPE_TAGDEL, creator);
	}

    @Override
    public int deleteServerByUniqkey(List<Server> serverList) throws Exception {
		int result = 0;
		if (serverList != null && !serverList.isEmpty()) {
			List<String> uniqkeyList = new ArrayList<String>();
			for (Server server : serverList) {
				uniqkeyList.add(server.getUniqKey());
			}
			scanServerDao.deleteServerByUniqkey(uniqkeyList);
			DBLog.info("ScanServerManagerImpl.deleteByUniqkey, delete server by uniqkey:{}", uniqkeyList.toString());
			try {
				recordLog(serverList, DELTYPE_DELETE, logCreator);
			} catch (Exception e) {
				logger.error("update record log error:{}, delete server uniqkey list:{}", e.getMessage(), uniqkeyList.toString());
			} finally {
			}
		}
		return result;
    }

    @Override
    public int deleteServerById(List<Server> serverList) throws Exception {
		int result = 0;
		if (serverList != null && !serverList.isEmpty()) {
			List<Integer> idList = new ArrayList<Integer>();
			for (Server server : serverList) {
				idList.add(server.getId());
			}
			scanServerDao.deleteServerById(idList);
			DBLog.info("ScanServerManagerImpl.deleteServerById, delete server by id:{}", idList.toString());
			try {
				recordLog(serverList, DELTYPE_DELETE, logCreator);
			} catch (Exception e) {
				logger.error("update record log error:{}, delete server id list:{}", e.getMessage(), idList.toString());
			}
		}
		return result;
    }

    /*
     * (non-Javadoc)
     * @see ScanServerManager#getOnlineServersByIns(java.lang.String)
     */
    @Override
    public List<Server> getOnlineServersByIns(String insKey) throws Exception {
        if (insKey == null || insKey.isEmpty()) return null;
        return scanServerDao.getOnlineServersByIns(insKey);
    }

	@Override
	public List<Server> getNoInsServers(long optTime) throws Exception {
		return scanServerDao.getNoInsServers(optTime);
	}

	@Override
	public List<Server> getServersByInsKeyList(List<String> insKeyList) throws Exception {
		if (insKeyList != null && !insKeyList.isEmpty()) {
            return scanServerDao.getServersByInsKeyList(insKeyList);
        }
        return null;
	}

	@Override
	public int updateServerToRevival(List<JsfIns> insList) throws Exception {
		if (!CollectionUtils.isEmpty(insList)) {
			List<String> insKeyList = new ArrayList<String>();
			for (JsfIns ins : insList) {
				insKeyList.add(ins.getInsKey());
			}
			List<Server> serverList = scanServerDao.getServersByInsKeyList(insKeyList);
			int result = scanServerDao.updateServerToRevival(serverList, InstanceStatus.online.value().intValue());
			updateInterfaceDataVersion(serverList);
			return result;
		}
		return 0;
	}
    
    private List<IfaceServer> getIfaceServers(List<Server> serverList) {
    	List<IfaceServer> result = new ArrayList<IfaceServer>();
    	if (!CollectionUtils.isEmpty(serverList)) {
    		for (Server server: serverList) {
    			IfaceServer ifaceServer = new IfaceServer();
    			ifaceServer.setAlias(server.getAlias());
    			ifaceServer.setInterfaceId(server.getInterfaceId());
    			ifaceServer.setUniqKey(server.getUniqKey());
    			result.add(ifaceServer);
    		}
    	}
    	return result;
    }
}
