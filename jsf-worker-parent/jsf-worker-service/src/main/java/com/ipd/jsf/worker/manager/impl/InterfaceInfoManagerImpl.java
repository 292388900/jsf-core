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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.dao.InterfaceDataVersionDao;
import com.ipd.jsf.worker.dao.InterfaceInfoDao;
import com.ipd.jsf.worker.domain.UserResource;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipd.jsf.worker.domain.InterfaceInfo;

@Service
public class InterfaceInfoManagerImpl implements InterfaceInfoManager {

    private static final Logger logger = LoggerFactory.getLogger(InterfaceInfoManagerImpl.class);

    @Autowired
    private InterfaceInfoDao interfaceInfoDao;

    @Autowired
    private InterfaceDataVersionDao interfaceDataVersionDao;

    /* (non-Javadoc)
     * @see com.ipd.saf.worker.manager.InterfaceInfoManager#newInterfaceInfo(com.ipd.saf.worker.domain.InterfaceInfo)
     */
    @Override
    @Transactional
    public void newInterfaceInfo(InterfaceInfo interfaceInfo) throws Exception {
        try {
            interfaceInfoDao.create(interfaceInfo);
            interfaceDataVersionDao.create(interfaceInfo.getInterfaceId(), new Date());
        } catch (DuplicateKeyException e) {
            InterfaceInfo temp = interfaceInfoDao.getByInterfaceName(interfaceInfo.getInterfaceName());
            if (temp != null && temp.getInterfaceId() != null) {
                interfaceInfo.setInterfaceId(temp.getInterfaceId());
            }
            logger.warn("duplicateKey:" + interfaceInfo.getInterfaceName());
        }
    }

    @Override
    public void updateInterfaceInfo(InterfaceInfo interfaceInfo) throws Exception {
        interfaceInfoDao.update(interfaceInfo);
    }

    @Override
    public List<Integer> getCrossLangInterfaceIds() throws Exception {
    	return interfaceInfoDao.getCrossLangInterfaceIds();
    }

    @Override
    public void updateCrossLang(int interfaceId) throws Exception {
        interfaceInfoDao.updateCrossLang(interfaceId);
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.worker.manager.InterfaceInfoManager#getByInterfaceName(java.lang.String)
     */
    @Override
    public InterfaceInfo getByInterfaceName(String interfaceName)  throws Exception {
        return interfaceInfoDao.getByInterfaceName(interfaceName);
    }

    /*
     * (non-Javadoc)
     * @see com.ipd.saf.worker.manager.InterfaceInfoManager#getAll()
     */
    @Override
    public List<InterfaceInfo> getAll() throws Exception {
        return interfaceInfoDao.getAll();
    }

    @Override
    public List<InterfaceInfo> getInterfaceByCreateTime(Date time) throws Exception {
        return interfaceInfoDao.getInterfaceByCreateTime(time);
    }

    @Override
    public List<InterfaceInfo> getInterfaceVersionByTime(Date time) throws Exception {
        return interfaceInfoDao.getInterfaceVersionByTime(time);
    }

    @Override
    public List<InterfaceInfo> getErps() throws Exception {
        return interfaceInfoDao.getErps();
    }

	@Override
	public List<InterfaceInfo> getAllWithJsfclient(Date date) throws Exception {
		return interfaceInfoDao.getAllWithJsfclient(date);
	}

	@Override
	public int updateJsfVer(String interfaceName) throws Exception {
		return interfaceInfoDao.updateJsfVer(interfaceName);
	}

	@Override
	public List<String> getOwnerUsers(InterfaceInfo info) {
		return interfaceInfoDao.getOwnerUsers(info);
	}

	@Override
	public void batchInsert(List<UserResource> urs) {
		interfaceInfoDao.batchInsertResource(urs);
	}

	@Override
	public List<String> findAuthErps(Integer interfaceId, int value) {
		return interfaceInfoDao.findAuthErps(interfaceId, value);
	}

	@Override
	public void deleteToSave(InterfaceInfo info) {
		interfaceInfoDao.deleteToSave(info);
	}

	@Override
	public void updateByName(InterfaceInfo isExistInfo) {
		interfaceInfoDao.updateByName(isExistInfo);
	}

	@Override
	public int create(InterfaceInfo interfaceInfo) throws Exception {
		return interfaceInfoDao.create(interfaceInfo);
	}

    @Override
    public void sumProviderAndConsumer() {
        interfaceInfoDao.sumProviderAndConsumer();
    }

}
