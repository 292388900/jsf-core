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

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.dao.SysParamDao;
import com.ipd.jsf.registry.domain.SysParam;
import com.ipd.jsf.registry.manager.SysParamManager;

@Service
public class SysParamManagerImpl implements SysParamManager {
    @Autowired
    private SysParamDao sysParamDao;

    @Override
    public List<SysParam> getListByType(List<Integer> typeList) throws Exception {
        return sysParamDao.getListByType(typeList);
    }

    @Override
    public String getByTypeAndKey(String key, int type) throws Exception {
        return sysParamDao.getByTypeAndKey(key, type);
    }
}
