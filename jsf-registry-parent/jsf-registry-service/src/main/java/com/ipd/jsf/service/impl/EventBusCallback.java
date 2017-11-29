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
package com.ipd.jsf.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.service.SubscribeService;
import com.ipd.jsf.worker.service.vo.CliEvent;
import com.ipd.jsf.gd.transport.Callback;

public class EventBusCallback implements Callback<List<CliEvent>, String> {
    static Logger logger = LoggerFactory.getLogger(EventBusCallback.class);

    private SubscribeService subscribeService;
    
    @Override
    public String notify(List<CliEvent> result) {
    	if (result != null && result.size() > 0) {
//    		logger.info("collect: {}", result);
    		List<IfaceAliasVersion> list = new ArrayList<IfaceAliasVersion>();
    		for (CliEvent event : result) {
    			try {
    				if (event.getInterfaceId() > 0 && event.getAliasVersionMap() != null) {
    					for (Map.Entry<String, Long> entry : event.getAliasVersionMap().entrySet()) {
		    				IfaceAliasVersion version = new IfaceAliasVersion();
		    				version.setInterfaceId(event.getInterfaceId());
		    				version.setAlias(entry.getKey());
		    				version.setDataVersion(entry.getValue());
		    				list.add(version);
    					}
    				}
    			} catch (Exception e) {
    				logger.error("event notify: reload cache is error:" + e.getMessage(), e);
    			}
    		}
    		if (!list.isEmpty()) {
    			try {
//    				logger.info("ifaceAliasVersion:collect: {}", list);
    				subscribeService.putIfaceAliasToQueue(list);
    			} catch (Exception e) {
    				logger.error(e.getMessage(), e);
    			}
    		}
    	}
    	return null;
    }

    /**
     * @param subscribeService the subscribeService to set
     */
    public void setSubscribeService(SubscribeService subscribeService) {
        this.subscribeService = subscribeService;
    }
}
