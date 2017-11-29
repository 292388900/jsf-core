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

import java.util.HashMap;
import java.util.Map;

import com.ipd.fastjson.JSON;

public class LookupParam {
    //客户端ip
    private String ip;

    //接口名
    private String iface;

    //别名
    private String alias;

    /* protocol为0-consumer,或1-jsf,2-rest,3-dubbo */
    private int protocol;

    //实例key
    private String insKey;

    //版本号
    private long dataVer;
    
    /* string language ,string jsfVersion, string consumer, string serialization */
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
     * @return the dataVer
     */
    public long getDataVer() {
        return dataVer;
    }

    /**
     * @param dataVer the dataVer to set
     */
    public void setDataVer(long dataVer) {
        this.dataVer = dataVer;
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
        StringBuilder sb = new StringBuilder("LookupParam(");
        sb.append("ip:").append(this.ip);
        sb.append(",iface:").append(this.iface);
        sb.append(",alias:").append(this.alias);
        sb.append(",protocol:").append(this.protocol);
        sb.append(",insKey:").append(this.insKey);
        sb.append(",dataVer:").append(this.dataVer);
        if (this.attrs != null) {
            sb.append(",attrs:").append(this.attrs.toString());
        }
        sb.append(")");
        return sb.toString();
    }
    
    public static void main(String[] args) {
        LookupParam p = new LookupParam();
        p.setAttrs(new HashMap<String, String>());
        p.getAttrs().put("visitname", "jdgbuyautset");
        System.out.println(JSON.toJSONString(p));
    }
}
