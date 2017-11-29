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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.worker.dao.ScanInsDao;
import com.ipd.jsf.worker.dao.ScanInsServerDao;
import com.ipd.jsf.worker.dao.ScanStatusLogDao;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.ScanStatusLog;
import com.ipd.jsf.worker.manager.ScanInsManager;
import com.ipd.jsf.worker.util.DBLog;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ScanInsManagerImpl implements ScanInsManager {
	private final static Logger logger = LoggerFactory.getLogger(ScanInsManagerImpl.class);
	private int deleteLimit = 20;
	
	private final int DELTYPE_TAGDEL = 5;
	private final int DELTYPE_DELETE = 9;

    @Autowired
    private ScanInsDao scanInsDao;
    
    @Autowired
    private ScanInsServerDao scanInsServerDao;

    @Autowired
    private ScanStatusLogDao scanStatusLogDao;

    @Override
    public int updateStatusOffline(String insKey) throws Exception {
        if (insKey!= null && !insKey.isEmpty()) {
            return scanInsDao.updateStatusOffline(insKey, InstanceStatus.offline.value().intValue());
        }
        return 0;
    }

    @Transactional
    @Override
    public int tagInsToDel(JsfIns ins) throws Exception {
        int result = 0;
        if (ins.getInsKey() != null && !ins.getInsKey().isEmpty()) {
            long optTime = DateTimeZoneUtil.getTargetTime().getTime();
            result = scanInsDao.tagInsToDel(ins.getInsKey(), optTime, InstanceStatus.deleted.value().intValue());
            DBLog.info("ScanInsManagerImpl.tagInsToDel, inskey:{}", ins.getInsKey());
            try {
            	recordLog(ins, DELTYPE_TAGDEL);
			} catch (Exception e) {
				logger.error("deleteByInsKey: " + ins.toString() + ", error:" + e.getMessage(), e);
			}
        }
        return result;
    }

    @Override
    public int deleteByInsKey(JsfIns jsfIns) throws Exception {
        int result = 0;
        if (jsfIns != null) {
            result = scanInsDao.deleteByInsKey(jsfIns.getInsKey());
            DBLog.info("ScanInsManagerImpl.deleteByInsKey ins:{}", jsfIns.toString());
            //删除实例与server的关联表
            scanInsServerDao.deleteByInsKey(jsfIns.getInsKey());
            try {
            	recordLog(jsfIns, DELTYPE_DELETE);
			} catch (Exception e) {
				logger.error("deleteByInsKey: " + jsfIns.toString() + ", error:" + e.getMessage(), e);
			}
        }
        return result;
    }

    /**
     * @throws Exception 
     * 
     */
    private void recordLog(JsfIns jsfIns, int delType) throws Exception {
    	Date createTime = new Date();
    	String creator = "scanstatus worker";
    	List<ScanStatusLog> logList = new ArrayList<ScanStatusLog>();
    	ScanStatusLog log = new ScanStatusLog();
        log.setIp(jsfIns.getIp());
        log.setPid(jsfIns.getPid());
        log.setInsKey(jsfIns.getInsKey());
        log.setType(DataEnum.ScanStatusLogType.instance.getValue());
        log.setDetailInfo("delete " + jsfIns.toString());
        log.setCreateTime(createTime);
        log.setCreator(creator);
        log.setCreatorIp(WorkerUtil.getWorkerIP());
        log.setDelType(delType);
        logList.add(log);
        if (logList.size() > deleteLimit) {
            scanStatusLogDao.create(logList);
            logList.clear();
        }
        if (!logList.isEmpty()) {
            scanStatusLogDao.create(logList);
            logList.clear();
        }
    }

    @Override
    public List<JsfIns> getOnlineInsBeforeTime(Date time, List<String> registryList) throws Exception {
        return scanInsDao.getOnlineInsBeforeTime(time, registryList);
    }

    @Override
    public List<JsfIns> getOnlineInsBeforeTimeByRooms(Date time, List<String> registryList, List<Integer> rooms) throws Exception {
        return scanInsDao.getOnlineInsBeforeTimeByRooms(time, registryList, rooms);
    }

    @Override
    public List<JsfIns> getOfflineInsBeforeTime(Date time, List<String> registryList) throws Exception {
        return scanInsDao.getOfflineInsBeforeTime(time, registryList);
    }

    @Override
    public List<JsfIns> getOfflineInsBeforeTimeByRooms(Date time, List<String> registryList, List<Integer> rooms) throws Exception {
        return scanInsDao.getOfflineInsBeforeTimeByRooms(time, registryList, rooms);
    }

    @Override
    public List<JsfIns> getDelInsBeforeTime(Date time, int type) throws Exception {
    	if (type == 1) {
    		return scanInsDao.getDelUnregInsBeforeTime(time);
    	} else if (type == 2) {
    		return scanInsDao.getDelInsBeforeTime(time);
    	}
    	return null;
    }

    @Override
    public List<JsfIns> getDelInsBeforeTimeByRooms(Date time, int type, List<Integer> rooms) throws Exception {
        if (type == 1) {
            return scanInsDao.getDelUnregInsBeforeTimeByRooms(time, rooms);
        } else if (type == 2) {
            return scanInsDao.getDelInsBeforeTimeByRooms(time, rooms);
        }
        return null;
    }

	@Override
	public List<JsfIns> getRevivalInsListByServer() throws Exception {
		return scanInsDao.getRevivalInsListByServer();
	}

    @Override
    public List<JsfIns> getRevivalInsListByServerAndRooms(List<Integer> rooms) throws Exception {
        return scanInsDao.getRevivalInsListByServerAndRooms(rooms);
    }

	@Override
	public List<JsfIns> getRevivalInsListByClient() throws Exception {
		return scanInsDao.getRevivalInsListByClient();
	}

    @Override
    public List<JsfIns> getRevivalInsListByClientAndRooms(List<Integer> rooms) throws Exception {
        return scanInsDao.getRevivalInsListByClientAndRooms(rooms);
    }

}
