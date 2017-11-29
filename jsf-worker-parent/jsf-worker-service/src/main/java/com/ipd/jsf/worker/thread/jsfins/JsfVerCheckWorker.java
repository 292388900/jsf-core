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
package com.ipd.jsf.worker.thread.jsfins;

import java.util.List;

import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import com.ipd.jsf.worker.manager.ServerManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.manager.ClientManager;

public class JsfVerCheckWorker extends SingleWorker{
	
	private final static Logger logger = LoggerFactory.getLogger(JsfVerCheckWorker.class);

	@Autowired
    InterfaceInfoManager interfaceInfoManager;
	
	@Autowired
    ServerManager serverManager;
	
	@Autowired
	ClientManager clientManager;
	

	@Override
	public boolean run() {
		try {
			
			logger.info("检查接口是否有jsf客户端worker开始运行。。。");
			List<InterfaceInfo> infos = interfaceInfoManager.getAll();
			if(!CollectionUtils.isEmpty(infos)){
				for(InterfaceInfo info : infos){
					String interfaceName = info.getInterfaceName();
					if(serverManager.hafJsfVer(interfaceName) || clientManager.hafJsfVer(interfaceName)){
						interfaceInfoManager.updateJsfVer(interfaceName);
					}
				}
			}
			return true;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public String getWorkerType() {
		return "jsfVerCheckWorker";
	}

}
