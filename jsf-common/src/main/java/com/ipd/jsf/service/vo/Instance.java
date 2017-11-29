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

package com.ipd.jsf.service.vo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Instance {
    /*实例key*/
    private String insKey;

    /*保存接口对应的分组和协议  key= interfaceName, value= alias:protocol*/
    private Map<String, Set<String>> ifaceAliasProtocolMap = new HashMap<String, Set<String>>();

    //provider 列表
    private List<Provider> providers = new ArrayList<Provider>();

    private Map<String, Map<String, String>> config = new HashMap<String, Map<String,String>>();

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
     * @return the ifaceAliasProtocolMap
     */
    public Map<String, Set<String>> getIfaceAliasProtocolMap() {
        return ifaceAliasProtocolMap;
    }

    /**
     * @param ifaceAliasProtocolMap the ifaceAliasProtocolMap to set
     */
    public void setIfaceAliasProtocolMap(
            Map<String, Set<String>> ifaceAliasProtocolMap) {
        this.ifaceAliasProtocolMap = ifaceAliasProtocolMap;
    }

    /**
     * @return the providers
     */
    public List<Provider> getProviders() {
        return providers;
    }

    /**
     * @param providers the providers to set
     */
    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }

    /**
     * @return the config
     */
    public Map<String, Map<String, String>> getConfig() {
        return config;
    }

    /**
     * @param config the config to set
     */
    public void setConfig(Map<String, Map<String, String>> config) {
        this.config = config;
    }

}
