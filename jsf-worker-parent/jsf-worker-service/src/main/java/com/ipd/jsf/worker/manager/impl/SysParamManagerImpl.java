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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.ipd.jsf.worker.manager.SysParamManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.ipd.jsf.worker.dao.SysParamDao;
import com.ipd.jsf.worker.domain.SysParam;

@Service
public class SysParamManagerImpl implements SysParamManager {
    @Autowired
    private SysParamDao sysParamDao;

    @Override
    public int create(SysParam sysParam) throws Exception {
        return sysParamDao.create(sysParam);
    }

    @Override
    public int update(SysParam sysParam) throws Exception {
        return sysParamDao.update(sysParam);
    }

    @Override
    public SysParam get(String key, int type) throws Exception {
        return sysParamDao.get(key, type);
    }

    @Override
	public String findValueBykey(String key) {
		return sysParamDao.findBykey(key);
	}
	
	@Override
	public List<String> findValuesByKey(String key) {
		List<String> result = new ArrayList<String>();
		String v = sysParamDao.findBykey(key);
		if(StringUtils.hasText(v)){
			result = Arrays.asList(v.split(","));
		}
		return result;
	}

}
