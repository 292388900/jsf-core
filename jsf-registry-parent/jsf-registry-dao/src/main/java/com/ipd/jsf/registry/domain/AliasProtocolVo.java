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

import java.util.HashSet;
import java.util.Set;

import com.ipd.fastjson.JSON;


/**
 * 记录consumer端的信息
 */
public class AliasProtocolVo {
    //alias
    private String alias;
    //协议
    private int protocol;
    //jsf版本
    private String jsfVersion;
    //序列化类型
    private String serialization;

    public AliasProtocolVo(String alias, int protocol, String jsfVersion, String serialization) {
        this.alias = alias;
        this.protocol = protocol;
        this.jsfVersion = jsfVersion;
        this.serialization = serialization;
    }

    @Override
    public int hashCode() {
        int hc = this.alias.hashCode();
        hc = hc * 31 + this.protocol;
        return hc;
    }

    public boolean equals(Object obj){
        if(this == obj) {
            return true;
        }
        if(!(obj instanceof AliasProtocolVo)){
            return false;
        }
        AliasProtocolVo vo = (AliasProtocolVo)obj;
        if ((alias == null && vo.alias != null) || (alias != null && vo.alias == null) || !alias.equals(vo.alias)) {
            return false;
        }
        if (protocol != vo.protocol) {
            return false;
        }
        return true;
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
     * @return the serialization
     */
    public String getSerialization() {
        return serialization;
    }
    /**
     * @param serialization the serialization to set
     */
    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public static void main(String[] args) {
        Set<AliasProtocolVo> set = new HashSet<AliasProtocolVo>();
        AliasProtocolVo vo = new AliasProtocolVo("baont", 1, "1000", "bac");
        AliasProtocolVo vo1 = new AliasProtocolVo("baont1", 1, "1100", "bac11");
        AliasProtocolVo vo2 = new AliasProtocolVo("baont", 2, "1200", "bac22");
        set.add(vo2);
        set.add(vo1);
        set.add(vo);
        System.out.println(JSON.toJSONString(set));
    }
}
