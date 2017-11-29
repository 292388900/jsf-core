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
package com.ipd.jsf.worker.common.service.impl;

import com.ipd.jsf.worker.common.dao.WorkerDefineDAO;
import com.ipd.jsf.worker.common.domain.WorkerInfo;
import com.ipd.jsf.worker.common.service.WorkerDefineService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("workerDefineServiceImpl")
public class WorkerDefineServiceImpl implements WorkerDefineService {

    @Autowired
    private WorkerDefineDAO workerDefineDAO;

    @Override
    public List<WorkerInfo> loadWorkerInfos() {
        return workerDefineDAO.findWorkerInfos();
    }

    @Override
    public WorkerInfo loadWorkerInfo(int id) {
        return workerDefineDAO.findWorkerInfo(id);
    }

    @Override
    public WorkerInfo loadWorkerInfo(String workerName) {
        return workerDefineDAO.findWorkerInfoByName(workerName);
    }

    @Override
    public List<WorkerInfo> loadWorkerInfos(List<String> workerTypes) {
        return workerDefineDAO.findWorkerInfosByWorkerType(workerTypes);
    }

    public WorkerDefineDAO getWorkerDefineDAO() {
        return workerDefineDAO;
    }

    public void setWorkerDefineDAO(WorkerDefineDAO workerDefineDAO) {
        this.workerDefineDAO = workerDefineDAO;
    }
}
