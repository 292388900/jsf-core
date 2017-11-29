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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.worker.domain.RegistryReq;
import com.ipd.jsf.worker.log.dao.JsfRegReqDao;
import com.ipd.jsf.worker.service.JsfRegReqService;

@Service
public class JsfRegReqServiceImpl implements JsfRegReqService{
    private final static Logger logger = LoggerFactory.getLogger(JsfRegReqServiceImpl.class);
    private long interval = 30 * 24 * 3600000L;
	@Autowired
	private JsfRegReqDao safRegReqDao;


	@Override
	public void insert(RegistryReq registryReq) {
		safRegReqDao.insert(registryReq);
	}


    @Override
    public void deleteByTime() throws Exception {
        long start = System.currentTimeMillis();
        Date time = new Date(System.currentTimeMillis() - interval);
        try {
            int result = safRegReqDao.deleteByTime(time);
            logger.info("registry request log delete size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    
    }
	
}
