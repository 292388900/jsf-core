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
package com.ipd.jsf.worker.vo;

import java.util.Map;

import com.ipd.jsf.worker.domain.InterfaceInfo;

public class InstanceVo {
    private String ip;
    private int pid;
    private Map<Integer, InterfaceInfo> map;
    // 上下线，true：上线，false：下线
    private boolean isOnline;
    private String jsfVersion;
    private String regIpPort;
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
     * @return the pid
     */
    public int getPid() {
        return pid;
    }
    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    /**
     * @return the map
     */
    public Map<Integer, InterfaceInfo> getMap() {
        return map;
    }
    /**
     * @param map the map to set
     */
    public void setMap(Map<Integer, InterfaceInfo> map) {
        this.map = map;
    }
    /**
     * @return the isOnline
     */
    public boolean isOnline() {
        return isOnline;
    }
    /**
     * @param isOnline the isOnline to set
     */
    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
    /**
     * @return the jsfVersion
     */
    public String getJsfVersion() {
        return jsfVersion;
    }
    /**
     * @param jsfVersion the jsfVersion to set
     */
    public void setJsfVersion(String jsfVersion) {
        this.jsfVersion = jsfVersion;
    }
    /**
     * @return the regIpPort
     */
    public String getRegIpPort() {
        return regIpPort;
    }
    /**
     * @param regIpPort the regIpPort to set
     */
    public void setRegIpPort(String regIpPort) {
        this.regIpPort = regIpPort;
    }
}
