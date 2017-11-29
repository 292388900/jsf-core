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
package com.ipd.jsf.worker.domain;

import java.io.Serializable;

public class ServerAlias implements Serializable {

    private Integer id;

    private Integer serverId;

    private Integer interfaceId;

    private String aliasName;

    private String serverUnikey;

    private int srcType;

    private int aliasType;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Integer interfaceId) {
        this.interfaceId = interfaceId;
    }

    public String getAliasName() {
        return aliasName;
    }

    public void setAliasName(String aliasName) {
        this.aliasName = aliasName;
    }

    public int getSrcType() {
        return srcType;
    }

    public void setSrcType(int srcType) {
        this.srcType = srcType;
    }

    public Integer getServerId() {
        return serverId;
    }

    public void setServerId(Integer serverId) {
        this.serverId = serverId;
    }

    public int getAliasType() {
        return aliasType;
    }

    public void setAliasType(int aliasType) {
        this.aliasType = aliasType;
    }

    public String getServerUnikey() {
        return serverUnikey;
    }

    public void setServerUnikey(String serverUnikey) {
        this.serverUnikey = serverUnikey;
    }

    @Override
    public String toString() {
        return "{serverId: " + serverId + ", interfaceId: " + interfaceId + ", alias: " + aliasName + ", aliasType: " + aliasType + "}";
    }

}
