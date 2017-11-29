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

import java.util.Map;

public class ProviderUrl {
    //provider的ip
    private String ip;

    //provider的端口
    private int port;

    //provider的协议
    private int protocol;

    //provider的别名
    private String alias;

    //参数:jsfVersion, serialization, weight
    private Map<String, String> attrs;

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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ProviderUrl(");
        sb.append("ip:").append(this.ip);
        sb.append(",port:").append(this.port);
        sb.append(",alias:").append(this.alias);
        sb.append(",protocol:").append(this.protocol);
        if (this.attrs != null) {
            sb.append(",attrs:").append(this.attrs.toString());
        }
        sb.append(")");
        return sb.toString();
    }

}
