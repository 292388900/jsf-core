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
import com.ipd.jsf.worker.dao.ClientDao;
import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.manager.ClientManager;
import com.ipd.jsf.worker.util.DBLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class ClientManagerImpl implements ClientManager {

    @Autowired
    private ClientDao clientDao;

    /*
     * (non-Javadoc)
     * @see ClientManager#create(java.util.List)
     */
    @Override
    public int create(List<Client> clientList) throws Exception {
        if (clientList != null && !clientList.isEmpty()) {
            return clientDao.create(clientList);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#update(Client)
     */
    @Override
    public int update(Client client) throws Exception {
        if (client != null) {
            return clientDao.update(client);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#getUniqKeyList(java.util.List)
     */
    @Override
    public List<String> getUniqKeyList(List<String> uniqKeyList) throws Exception {
        return clientDao.getUniqKeyList(uniqKeyList);
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#updateStatusOnlineByIns(java.util.List)
     */
    @Override
    public int updateStatusOnline(List<Client> list) throws Exception {
        if (list != null && list.size() > 0) {
            return clientDao.updateStatusOnline(list);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#updateStatusOfflineByIns(java.util.List)
     */
    @Override
    public int updateStatusOffline(List<Client> list) throws Exception {
        if (list != null && list.size() > 0) {
            return clientDao.updateStatusOffline(list);
        }
        return 0;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#getClientByIns(java.util.List, com.ipd.jsf.common.enumtype.InstanceStatus)
     */
    @Override
    public List<Client> getClientByIns(List<JsfIns> list, InstanceStatus status) throws Exception {
        List<Client> result = null;
        if (list != null && !list.isEmpty()) {
            if (InstanceStatus.online.value().intValue() == status.value().intValue()) {
                result = clientDao.getOnlineClientsByIns(list);
            } else if (InstanceStatus.offline.value().intValue() == status.value().intValue()) {
                result = clientDao.getOfflineClientsByIns(list);
            }
        }
        return result;
    }

    @Override
    public List<Client> getClientByIns(List<JsfIns> list) throws Exception {
        List<Client> result = null;
        if (list != null && !list.isEmpty()) {
            result = clientDao.getClientsByIns(list);
        }
        return result;
    }

    @Override
    public List<Client> getClientsByStatus(InstanceStatus status) throws Exception {
        return clientDao.getClientsByStatus(status.value().intValue());
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#deleteByUniqkey(java.util.List, int)
     */
    @Override
    public int deleteByUniqkey(List<Client> clientList, int srcType) throws Exception {
        int result = 0;
        if (clientList != null && clientList.size() > 0) {
            List<String> uniqkeyList = new ArrayList<String>();
            for (Client client : clientList) {
                if (client.getUniqKey() != null && !client.getUniqKey().isEmpty()) {
                    uniqkeyList.add(client.getUniqKey());
                }
            }
            if (!uniqkeyList.isEmpty()) {
                result = clientDao.deleteByUniqkey(uniqkeyList, srcType);
                //logger.info("sourceType:{}, delete client by uniqkey:{}", srcType, clientList.toString());
                DBLog.info("ClientManagerImpl.deleteByUniqkey, " + "sourceType:{}, delete client by uniqkey:{}", srcType, clientList.toString());
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#getListByInterfaceName(java.lang.String)
     */
    @Override
    public List<Client> getListByInterfaceName(String interfaceName) throws Exception {
        return clientDao.getListByInterfaceName(interfaceName);
    }

    @Override
    public List<Client> getListByInterfaceNameAndAlias(String interfaceName, String alias) throws Exception {
        return clientDao.getListByInterfaceNameAndAlias(interfaceName, alias);
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#deleteById(java.util.List)
     */
    @Override
    public int deleteById(List<Client> clientList, int sourceType) throws Exception {
        int limit = 20;
        int result = 0;
        if (clientList == null || clientList.size() == 0) {
            return result;
        }
        List<Integer> ids = new ArrayList<Integer>();
        for (Client c : clientList) {
            ids.add(c.getId());
            if(ids.size() > limit){
                result  += clientDao.deleteById(ids, sourceType);
                ids.clear();
            }
        }
        if(!ids.isEmpty()){
            result  += clientDao.deleteById(ids, sourceType);
        }
        DBLog.info("ClientManagerImpl.deleteById, source type:{}, delete client by id:{}", sourceType, clientList.toString());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see ClientManager#getListByUpdateTime(java.util.Date)
     */
    @Override
    public List<Client> getNoInsClients(Date date) throws Exception {
        return clientDao.getNoInsClients(date);
    }

    @Override
    public boolean hafJsfVer(String interfaceName) throws Exception {
        return clientDao.getJsfVerCount(interfaceName) > 0 ? true : false;
    }

    @Override
    public List<Client> getJsfClients(String interfaceName) {
        return clientDao.getJsfClients(interfaceName);
    }

}