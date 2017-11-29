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

import java.util.List;
import java.util.Map;

public class InterfaceInfoVo {
    //provider 列表
    private List<Provider> providers;
    //provider总记录数
    private int providerTotalRecord;
    //接口配置信息
    private Map<String, String> configs;
    //动态分组 key:alias, value: serverId list
    private Map<String, List<Integer>> aliasServers;
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
     * @return the providerTotalRecord
     */
    public int getProviderTotalRecord() {
        return providerTotalRecord;
    }
    /**
     * @param providerTotalRecord the providerTotalRecord to set
     */
    public void setProviderTotalRecord(int providerTotalRecord) {
        this.providerTotalRecord = providerTotalRecord;
    }
    /**
     * @return the configs
     */
    public Map<String, String> getConfigs() {
        return configs;
    }
    /**
     * @param configs the configs to set
     */
    public void setConfigs(Map<String, String> configs) {
        this.configs = configs;
    }
    /**
     * @return the aliasServers
     */
    public Map<String, List<Integer>> getAliasServers() {
        return aliasServers;
    }
    /**
     * @param aliasServers the aliasServers to set
     */
    public void setAliasServers(Map<String, List<Integer>> aliasServers) {
        this.aliasServers = aliasServers;
    }

    
}
