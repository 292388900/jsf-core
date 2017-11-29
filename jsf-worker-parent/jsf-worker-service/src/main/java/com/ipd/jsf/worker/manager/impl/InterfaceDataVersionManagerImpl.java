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
import com.ipd.jsf.worker.manager.InterfaceDataVersionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.version.common.service.AliasVersionService;

@Service
public class InterfaceDataVersionManagerImpl implements InterfaceDataVersionManager {
    @Autowired
    private InterfaceDataVersionDao interfaceDataVersionDao;
    @Autowired
    private AliasVersionService aliasVersionService;

    /* (non-Javadoc)
     * @see com.ipd.saf.worker.manager.InterfaceDataVersionManager#update(java.util.List, java.util.Date)
     */
    @Override
    public void update(List<Integer> list, Date updateTime) throws Exception {
        if (list != null && !list.isEmpty()) {
            int i = interfaceDataVersionDao.update(list, updateTime);
            aliasVersionService.updateByInterfaceIdList(list, new Date());
            //如果不一致就，说明系统中没有这个接口，逐条插入
            if (i < list.size()) {
                for (Integer interfaceId : list) {
                    interfaceDataVersionDao.create(interfaceId, updateTime);
                }
            }
        }
    }

}
