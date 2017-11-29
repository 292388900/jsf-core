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

import com.ipd.jsf.worker.domain.SynZkDBLog;
import com.ipd.jsf.worker.manager.SynZkDBLogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.log.dao.SynZkDBLogDao;

@Service
public class SynZkDBLogManagerImpl implements SynZkDBLogManager {

    @Autowired
    private SynZkDBLogDao synZkDBLogDao;

    @Override
    public void create(List<SynZkDBLog> logList) throws Exception {
        if (logList != null && logList.size() > 0) {
            synZkDBLogDao.create(logList);
        }
    }

    @Override
    public int deleteByTime(Date time) throws Exception {
        return synZkDBLogDao.deleteByTime(time);
    }

}
