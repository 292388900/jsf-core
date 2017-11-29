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

public class ServerSet {
    //接口id
    private int interfaceId;
    //provider的serverId
    private int serverId;
    //设定值
    private String value;
    /**
     * @return the interfaceId
     */
    public int getInterfaceId() {
        return interfaceId;
    }
    /**
     * @param interfaceId the interfaceId to set
     */
    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }
    /**
     * @return the serverId
     */
    public int getServerId() {
        return serverId;
    }
    /**
     * @param serverId the serverId to set
     */
    public void setServerId(int serverId) {
        this.serverId = serverId;
    }
    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    
}
