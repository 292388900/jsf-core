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
package com.ipd.jsf.registry.domain;

public class BdbServer {
    //bdb中的key
    private String key;
    //server
    private Server server;
    //用于berkeleyDB存储区分，true -- 注册，false--取消注册  
    private boolean isRegistry;
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
     * @return the server
     */
    public Server getServer() {
        return server;
    }
    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
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
    
}
