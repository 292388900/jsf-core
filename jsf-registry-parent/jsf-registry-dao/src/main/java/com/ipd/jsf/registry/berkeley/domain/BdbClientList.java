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
package com.ipd.jsf.registry.berkeley.domain;

import java.util.List;

import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;

public class BdbClientList {
    //bdb中的key
    private String key;
    //client
    private List<Client> clients;
    //用于berkeleyDB存储区分，true -- 注册，false--取消注册  
    private boolean isRegistry;
    //实例
    private JsfIns ins;
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }
    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }
    
    /**
	 * @return the clients
	 */
	public List<Client> getClients() {
		return clients;
	}
	/**
	 * @param clients the clients to set
	 */
	public void setClients(List<Client> clients) {
		this.clients = clients;
	}
	/**
     * @return the isRegistry
     */
    public boolean isRegistry() {
        return isRegistry;
    }
    /**
     * @param isRegistry the isRegistry to set
     */
    public void setRegistry(boolean isRegistry) {
        this.isRegistry = isRegistry;
    }
	/**
	 * @return the ins
	 */
	public JsfIns getIns() {
		return ins;
	}
	/**
	 * @param ins the ins to set
	 */
	public void setIns(JsfIns ins) {
		this.ins = ins;
	}
    
}
