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

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.worker.dao.ScanClientDao;
import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.manager.ScanClientManager;
import com.ipd.jsf.worker.util.DBLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScanClientManagerImpl implements ScanClientManager {
    @Autowired
    private ScanClientDao scanClientDao;

    @Override
    public int updateStatusOffline(List<Client> clientList) throws Exception {
        List<Integer> idList = getIdList(clientList);
        if(idList != null && !idList.isEmpty()){
            return scanClientDao.updateStatusOffline(idList, InstanceStatus.offline.value().intValue());
        }
        return 0;
    }

	@Override
	public int tagClientToDel(List<Client> clientList) throws Exception {
        List<Integer> idList = getIdList(clientList);
        if(idList != null && !idList.isEmpty()){
            return scanClientDao.tagClientToDel(idList, DateTimeZoneUtil.getTargetTime().getTime(), InstanceStatus.deleted.value().intValue());
        }
        return 0;
	}

    /*
     * (non-Javadoc)
     * @see ScanClientManager#getOnlineClientsByIns(java.lang.String)
     */
    @Override
    public List<Client> getOnlineClientsByIns(String insKey) throws Exception {
        List<Client> result = null;
        if (insKey != null && !insKey.isEmpty()) {
            result = scanClientDao.getOnlineClientsByIns(insKey);
        }
        return result;
    }

    @Override
    public List<Client> getClientsByIns(String insKey) throws Exception {
        List<Client> result = null;
        if (insKey != null && !insKey.isEmpty()) {
            result = scanClientDao.getClientsByIns(insKey);
        }
        return result;
    }
    
    @Override
    public List<Client> getClientsByInsKeyList(List<String> insKeyList) throws Exception {
    	List<Client> result = null;
    	if (insKeyList != null && !insKeyList.isEmpty()) {
    		result = scanClientDao.getClientsByInsKeyList(insKeyList);
    	}
    	return result;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#deleteByUniqkey(java.util.List, int)
     */
    @Override
    public int deleteByUniqkey(List<Client> clientList) throws Exception {
        int result = 0;
        List<String> uniqkeyList = getUniqKeyList(clientList);
        if (!uniqkeyList.isEmpty()) {
            result = scanClientDao.deleteByUniqkey(uniqkeyList);
            DBLog.info("ScanClientManagerImpl.deleteByUniqkey, delete client by uniqkey:{}", uniqkeyList.toString());
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#deleteById(java.util.List)
     */
    @Override
    public int deleteById(List<Client> clientList) throws Exception {
        int result = 0;
        List<Integer> idList = getIdList(clientList);
        if (!idList.isEmpty()) {
            result = scanClientDao.deleteById(idList);
            DBLog.info("ScanClientManagerImpl.deleteById, delete client by id:{}", idList.toString());
        }
        return result;
    }

	/*
	 * (non-Javadoc)
	 * @see ClientManager#getListByUpdateTime(java.util.Date)
	 */
    @Override
    public List<Client> getNoInsClients(long optTime) throws Exception {
        return scanClientDao.getNoInsClients(optTime);
    }

    private List<String> getUniqKeyList(List<Client> clientList) {
    	List<String> uniqkeyList = new ArrayList<String>();
    	if (clientList != null && clientList.size() > 0) {
            for (Client client : clientList) {
                if (client.getUniqKey() != null && !client.getUniqKey().isEmpty()) {
                    uniqkeyList.add(client.getUniqKey());
                }
            }
    	}
    	return uniqkeyList;
    }

    private List<Integer> getIdList(List<Client> clientList) {
    	List<Integer> idList = new ArrayList<Integer>();
    	if (clientList != null && clientList.size() > 0) {
            for (Client client : clientList) {
                idList.add(client.getId());
            }
    	}
    	return idList;
    }

	@Override
	public int updateClientToRevival(List<JsfIns> insList) throws Exception {
		if (!CollectionUtils.isEmpty(insList)) {
            List<String> insKeyList = new ArrayList<String>();
            for (JsfIns ins : insList) {
                insKeyList.add(ins.getInsKey());
            }
            List<Client> clientList = scanClientDao.getClientsByInsKeyList(insKeyList);
			return scanClientDao.updateClientToRevival(clientList, InstanceStatus.online.value().intValue());
		}
		return 0;
	}
}
