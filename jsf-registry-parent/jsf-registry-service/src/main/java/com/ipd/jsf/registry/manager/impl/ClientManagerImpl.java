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
package com.ipd.jsf.registry.manager.impl;

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.registry.dao.ClientDao;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.manager.ClientManager;
import com.ipd.jsf.registry.service.EventSynService;
import com.ipd.jsf.registry.service.helper.SubscribeHelper;
import com.ipd.jsf.registry.util.DBLog;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * client操作类
 */
@Service
public class ClientManagerImpl implements ClientManager {

    private static Logger logger = LoggerFactory.getLogger(ClientManagerImpl.class);
    private final int APPPATH_LENGTH = 127;
    private final int URLDESC_LENGTH = 1022;
	private final int CLIENTID_LIMIT = 1000 * 10000;   //小于CLIENTID_LIMIT时，直接用自增id就行

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private EventSynService eventSynServiceImpl;

    @Autowired
    private SubscribeHelper subscribeHelper;

    private volatile int auto_id = 0;
    private final int range = 100;
    private final int retry = 3;
    private LinkedBlockingQueue<Integer> clientIdQueue = new LinkedBlockingQueue<Integer>(range * 10);
	private Random rand = new Random();

    /**
     * 创建或者修改db中的client
     */
    private boolean registerClient(Client client) throws Exception {
        prepareClient(client);
        return createClientDB(client);
    }

	@Override
	public void registerClient(List<Client> clientList) throws Exception {
		if (clientList == null || clientList.isEmpty()) return;
		List<Client> eventClientList = new ArrayList<Client>();
		for (Client client : clientList) {
			if (registerClient(client)) {
				eventClientList.add(client);
			}
		}
		try {
			if (!eventClientList.isEmpty()) {
	            //事件通知
	            eventSynServiceImpl.eventCollectClientRegister(eventClientList);
			}
        } catch (Exception e) {
            logger.error("event syn is error:{}", e.getMessage());
        }
	}

    private void unRegisterClient(Client client) throws Exception {
        if (client == null) return;
        int i = clientDao.updateToUnreg(UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), client.getAlias(), client.getProtocol(), client.getInterfaceId()), DateTimeZoneUtil.getTargetTime().getTime());
        if (i == 0) {
        	DBLog.warn("unregister client fail:{}", client.toString());
        } else {
        	DBLog.info("unregister client succ:{}", client.toString());
        }
        return;
    }

	@Override
	public boolean unRegisterClient(List<Client> clientList) throws Exception {
		if (clientList == null || clientList.isEmpty()) return true;
		for (Client client : clientList) {
			unRegisterClient(client);
		}
		try {
            //事件通知
            eventSynServiceImpl.eventCollectClientUnRegister(clientList);
        } catch (Exception e) {
            logger.error("event syn is error:{}", e.getMessage());
        }
		return true;
	}

    /**
     * 设置client属性
     * @param client
     */
    private void prepareClient(Client client) {
        if (client.getStatus() == 0) {
            client.setStatus(InstanceStatus.online.value());
        }
        client.setAppPath(RegistryUtil.limitString(client.getAppPath(), APPPATH_LENGTH));
        client.setUrlDesc(RegistryUtil.limitString(client.getUrlDesc(), URLDESC_LENGTH));
    }

    /**
     * 创建client到mysql中
     * @param client
     */
    private boolean createClientDB(Client client) throws Exception {
    	Client srcClient = clientDao.getClientByUniqkey(client.getUniqKey());
    	boolean needCreate = true;
    	if (srcClient != null) {
	    	if (!compareClient(srcClient, client)) {   //不一致，就update；一致，不做任何操作
	    		if (clientDao.update(client) > 0) {   //如果更新成功，就不用创建了
	    			DBLog.info("update client:{}", client.toString());
	    			needCreate = false;
	    		}
	    	} else {
	    		logger.info("no operate client:{}", client.toString());
	    		return false;   //如果一致，就不用再创建了
	    	}
    	}
        if (needCreate) {
        	int n = 0;
        	while (n++ < retry) {
	            try {
	            	if (n < retry) { //如果n为3，说明前2次插入失败了（可能有重复的clientId导致的），此时，就直接使用自增
	            		client.setClientId(getId());
	            	}
	                int i = clientDao.create(client);
	                DBLog.info("add client, result:{}, {}", i, client.toString());
	                return true;
	            } catch (DuplicateKeyException e) {
	                logger.warn("client duplicate key:{}, clientId:{}", client.getUniqKey(), client.getClientId());
	            } catch (DataIntegrityViolationException e) {
	            	logger.warn("client duplicate key:{}, clientId:{}", client.getUniqKey(), client.getClientId());
	            }
	            client.setClientId(0);
	            //如果数据库中有重复的clientId，就清空，重新取
	            clientIdQueue.clear();
        	}
        }
        return false;
    }

    /**
     * 不一致返回false，一致返回true
     * @param srcClient
     * @param newClient
     * @return
     */
    private boolean compareClient(Client srcClient, Client newClient) {
    	if (srcClient.getStatus() != newClient.getStatus()) {
    		return false;
    	}
    	return true;
    }

	/**
	 * 计算
	 * @return
	 * @throws Exception
     */
    private int getId() throws Exception {
    	int result = 0;
    	int start = 0;
    	int end;
		if (subscribeHelper.genClientIdSwitch()) {
			try {
				if (auto_id == 0) {
					auto_id = clientDao.getAutoIncrement() - range * 2;
				}
				if (auto_id < CLIENTID_LIMIT) {    //auto_id小于CLIENTID_LIMIT时，就直接用自增就行
					return result;
				}
				while (true) {
					if (clientIdQueue.isEmpty()) {   //先从队列里取，如果没有了，再从db里取一批
						start = rand.nextInt(auto_id);
						end = start + range;
						List<Integer> resultIds = clientDao.getIdList(start, end);
						for (int i = start; i <= end; i++) {
							if (CollectionUtils.isEmpty(resultIds) || !resultIds.contains(i)) {
								clientIdQueue.put(i);
							}
						}
					}
					Integer tmp = clientIdQueue.poll(100, TimeUnit.MICROSECONDS);
					if (tmp != null) {
						result = tmp.intValue();
						break;
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage() + ", start:" + start, e);
			}
		}
    	return result;
    }
}
