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

import java.io.Serializable;

public class Ipwblist implements Serializable {
    public static byte WHITETYPE = 1;
    public static byte BLACKTYPE = 2;
    private static final long serialVersionUID = 1L;
    private int id;
    private Integer interfaceId;
    private String interfaceName;
    private String alias;
    private String ip;
    private String regular;
    private byte wbType;
    private byte valid;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the interfaceId
     */
    public Integer getInterfaceId() {
        return interfaceId;
    }
    /**
     * @param interfaceId the interfaceId to set
     */
    public void setInterfaceId(Integer interfaceId) {
        this.interfaceId = interfaceId;
    }
    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }
    /**
     * @param interfaceName the interfaceName to set
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }
    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }
    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }
    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * @return the regular
     */
    public String getRegular() {
        return regular;
    }
    /**
     * @param regular the regular to set
     */
    public void setRegular(String regular) {
        this.regular = regular;
    }
    /**
     * @return the wbType
     */
    public byte getWbType() {
        return wbType;
    }
    /**
     * @param wbType the wbType to set
     */
    public void setWbType(byte wbType) {
        this.wbType = wbType;
    }
    /**
     * @return the valid
     */
    public byte getValid() {
        return valid;
    }
    /**
     * @param valid the valid to set
     */
    public void setValid(byte valid) {
        this.valid = valid;
    }
}
