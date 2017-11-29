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

import com.ipd.jsf.worker.domain.ScanStatusLog;
import com.ipd.jsf.worker.log.dao.ScanStatusLogHistoryDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.dao.ScanStatusLogDao;
import com.ipd.jsf.worker.manager.ScanStatusLogManager;

@Service
public class ScanStatusLogManagerImpl implements ScanStatusLogManager {

    @Autowired
    private ScanStatusLogDao scanStatusLogDao;

    @Autowired
    private ScanStatusLogHistoryDao scanStatusLogHistoryDao;

    @Override
    public List<ScanStatusLog> getListBeforeTime(Date time) throws Exception {
        return scanStatusLogDao.getList(time);
    }

    @Override
    public int insertToHistory(List<ScanStatusLog> list) throws Exception {
        if (list != null && !list.isEmpty()) {
            return scanStatusLogHistoryDao.create(list);
        }
        return 0;
    }

    @Override
    public int batchDelete(Date time) throws Exception {
        return scanStatusLogDao.deleteBeforeTime(time);
    }
}
