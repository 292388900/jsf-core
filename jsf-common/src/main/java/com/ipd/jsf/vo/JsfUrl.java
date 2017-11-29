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

package com.ipd.jsf.vo;

import java.io.Serializable;
import java.util.Map;

public class JsfUrl implements Serializable {
    private static final long serialVersionUID = 7213436245556457630L;
    /* 实例ip */
    private String ip;
    /* 实例端口 */
    private int port;
    /* 实例pid */
    private int pid;
    /* 接口描述 */
    private String iface;
    /* 服务别名 */
    private String alias;
    /* protocol为0-consumer,或1-jsf,2-rest,3-dubbo */
    private int protocol;
    /* string dubboVersion ,string safVersion, string language, string appPath, i32 weight */
    private Map<String, String> attrs;
    /* 超时时间 */
    private int timeout;
    /* 随机端口, true-随机端口   false-固定端口*/
    private boolean random;
    /* startTime */
    private long stTime;
    /* 实例key 由注册中心生成 */
    private String insKey;
    /* 版本号，订阅时比对 */
    private long dataVersion;
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
     * @return the iface
     */
    public String getIface() {
        return iface;
    }
    /**
     * @param iface the iface to set
     */
    public void setIface(String iface) {
        this.iface = iface;
    }
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * @return the protocol
     */
    public int getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
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
     * @return the attrs
     */
    public Map<String, String> getAttrs() {
        return attrs;
    }
    /**
     * @param attrs the attrs to set
     */
    public void setAttrs(Map<String, String> attrs) {
        this.attrs = attrs;
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
     * @return the random
     */
    public boolean isRandom() {
        return random;
    }
    /**
     * @param random the random to set
     */
    public void setRandom(boolean random) {
        this.random = random;
    }
    /**
     * @return the stTime
     */
    public long getStTime() {
        return stTime;
    }
    /**
     * @param stTime the stTime to set
     */
    public void setStTime(long stTime) {
        this.stTime = stTime;
    }
    /**
     * @return the insKey
     */
    public String getInsKey() {
        return insKey;
    }
    /**
     * @param insKey the insKey to set
     */
    public void setInsKey(String insKey) {
        this.insKey = insKey;
    }
    /**
     * @return the dataVersion
     */
    public long getDataVersion() {
        return dataVersion;
    }
    /**
     * @param dataVersion the dataVersion to set
     */
    public void setDataVersion(long dataVersion) {
        this.dataVersion = dataVersion;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JsfUrl(");
        sb.append("ip:").append(this.ip);
        sb.append(",port:").append(this.port);
        sb.append(",pid:").append(this.pid);
        if (this.iface != null) sb.append(",interface:").append(this.iface);
        sb.append(",alias:").append(this.alias);
        sb.append(",protocol:").append(this.protocol);
        if (this.timeout > 0) sb.append(",timeout:").append(this.timeout);
        if (this.random) sb.append(",random:").append(this.random);
        if (this.stTime > 0) sb.append(",startTime:").append(this.stTime);
        if (this.insKey != null) sb.append(",insKey:").append(this.insKey);
        if (this.dataVersion > 0) sb.append(",dataversion:").append(this.dataVersion);
        if (this.attrs != null && !this.attrs.isEmpty()) {
            sb.append(",attrs:").append(this.attrs.toString());
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * 简化输出
     * @return
     */
    public String getSimpleString() {
        StringBuilder sb = new StringBuilder("JsfUrl(");
        if (this.ip != null) sb.append("ip:").append(this.ip);
        if (this.port > 0) sb.append(",port:").append(this.port);
        if (this.pid > 0) sb.append(",pid:").append(this.pid);
        if (this.iface != null) sb.append(",interface:").append(this.iface);
        if (this.alias != null) sb.append(",alias:").append(this.alias);
        if (this.protocol > 0) sb.append(",protocol:").append(this.protocol);
        if (this.timeout > 0) sb.append(",timeout:").append(this.timeout);
        if (this.stTime > 0) sb.append(",startTime:").append(this.stTime);
        if (this.insKey != null) sb.append(",insKey:").append(this.insKey);
        if (this.dataVersion > 0) sb.append(",dataversion:").append(this.dataVersion);
        if (this.attrs != null && !this.attrs.isEmpty()) {
            sb.append(",attrs:").append(this.attrs.toString());
        }
        sb.append(")");
        return sb.toString();
    }
}
