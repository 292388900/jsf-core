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
package com.ipd.jsf.worker.service.impl;

import com.ipd.jsf.worker.log.dao.IfaceMethodDao;
import com.ipd.jsf.worker.service.JSFInterfaceMethodService;
import com.ipd.jsf.worker.domain.MonitorInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JSFInterfaceMethodServiceImpl implements JSFInterfaceMethodService {

    @Autowired
    private IfaceMethodDao ifaceMethodDao;

    @Override
    public int getCount(String interfaceName) {
        MonitorInterface i  = new MonitorInterface();
        i.setInterfaceName(interfaceName);
        return ifaceMethodDao.count(i);
    }

    @Override
    public void insert(MonitorInterface ifaceInfo) {
        ifaceMethodDao.insert(ifaceInfo);
    }

    @Override
    public void update(MonitorInterface ifaceInfo) {
        ifaceMethodDao.update(ifaceInfo);
    }

    @Override
    public void saveOrUpdate(MonitorInterface ifaceInfo) {
        String methods = "";//ifaceMethodDao.getMethods(ifaceInfo.getInterfaceName());
        List<MonitorInterface> monitorInterfaceList = ifaceMethodDao.getMethodsByInterfaceName(ifaceInfo.getInterfaceName());
        if (monitorInterfaceList != null && monitorInterfaceList.size() > 1) {
            List<Integer> ids = new ArrayList<Integer>();
            for (int n = 1; n < monitorInterfaceList.size(); n++) {
                ids.add(monitorInterfaceList.get(n).getId());
            }
            if (!ids.isEmpty()) {
                ifaceMethodDao.deleteByIds(ids);
            }
        }
        if (monitorInterfaceList != null && !monitorInterfaceList.isEmpty()) {
            methods = monitorInterfaceList.get(0).getMethod();
        }
        if (methods == null) {
            methods = "";
        }
        if (!monitorInterfaceList.isEmpty() && !methods.equals(ifaceInfo.getMethod())) {
            ifaceInfo.setUpdateDate(new Date());
            ifaceMethodDao.update(ifaceInfo);
        } else {
            ifaceInfo.setCreateDate(new Date());
            ifaceInfo.setUpdateDate(new Date());
            ifaceMethodDao.insert(ifaceInfo);
        }
    }
}
