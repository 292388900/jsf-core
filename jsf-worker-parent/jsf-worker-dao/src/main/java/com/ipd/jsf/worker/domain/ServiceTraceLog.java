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

import java.util.Date;

public class ServiceTraceLog {
    public static byte online = 1;
    public static byte offline = 0;
    public static byte provider = 1;
    public static byte consumer = 0;
    
    private String interfaceName;
    private int interfaceId;
    private String ip;
    private int port;
    private int pid;
    private String alias;
    private String protocol;
    //saf版本号
    private int safVer;
    //是否服务端 1服务端0客户端
    private byte pcType;
    //saf上下线 0下线 1上线
    private byte onoffType;
    //创建者
    private String creator;
    //上下线时间
    private Date eventTime;

    private int count;

    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }
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
     * @param interfaceName the interfaceName to set
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
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
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
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
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    /**
     * @return the safVer
     */
    public int getSafVer() {
        return safVer;
    }
    /**
     * @param safVer the safVer to set
     */
    public void setSafVer(int safVer) {
        this.safVer = safVer;
    }
    /**
     * @return the pcType
     */
    public byte getPcType() {
        return pcType;
    }
    /**
     * @param pcType the pcType to set
     */
    public void setPcType(byte pcType) {
        this.pcType = pcType;
    }
    /**
     * @return the onoffType
     */
    public byte getOnoffType() {
        return onoffType;
    }
    /**
     * @param onoffType the onoffType to set
     */
    public void setOnoffType(byte onoffType) {
        this.onoffType = onoffType;
    }
    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }
    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }
    /**
     * @return the eventTime
     */
    public Date getEventTime() {
        return eventTime;
    }
    /**
     * @param eventTime the eventTime to set
     */
    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
