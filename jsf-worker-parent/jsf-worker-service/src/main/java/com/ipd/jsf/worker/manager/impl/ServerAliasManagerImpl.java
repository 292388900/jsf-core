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

import com.ipd.jsf.worker.dao.ServerAliasDao;
import com.ipd.jsf.worker.dao.UserActionDAO;
import com.ipd.jsf.worker.domain.ServerAlias;
import com.ipd.jsf.worker.domain.UserAction;
import com.ipd.jsf.worker.manager.ServerAliasManager;
import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.version.common.service.AliasVersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class ServerAliasManagerImpl implements ServerAliasManager {

    @Autowired
    private ServerAliasDao serverAliasDao;

    @Autowired
    private AliasVersionService aliasVersionService;

    @Autowired
    private UserActionDAO userActionDAO;

    @Override
    @Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED, readOnly = false)
    public void batchInsert(List<ServerAlias> serverAliases) throws Exception {
        serverAliasDao.batchInsert(serverAliases);
    }

    @Override
    @Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED, readOnly = false)
    public void batchCancel(List<Integer> cancelServerAliasList) throws Exception {
        serverAliasDao.batchCancel(cancelServerAliasList);
    }

    @Override
    public List<ServerAlias> exists(ServerAlias serverAlias) {
        return serverAliasDao.exists(serverAlias);
    }

    @Override
    @Transactional(value = "transactionManager", isolation = Isolation.READ_COMMITTED, readOnly = false)
    public void dynamicGrouping(List<ServerAlias> newServerAliasList,
                                List<Integer> cancelServerAliasList,
                                List<IfaceServer> updateServerVersionList,
                                UserAction action) throws Exception {
        try {
            if (cancelServerAliasList != null && !cancelServerAliasList.isEmpty()) {
                batchCancel(cancelServerAliasList);
            }

            if (newServerAliasList != null && !newServerAliasList.isEmpty()) {
                batchInsert(newServerAliasList);
            }

            if (updateServerVersionList != null && !updateServerVersionList.isEmpty()) {
                aliasVersionService.updateByServerList(updateServerVersionList, new Date());
            }

            action.setIsSucc(1);
        } catch (Exception e) {
            action.setIsSucc(0);
            throw new RuntimeException(e);
        } finally {
            userActionDAO.insert(action);
        }
    }

}