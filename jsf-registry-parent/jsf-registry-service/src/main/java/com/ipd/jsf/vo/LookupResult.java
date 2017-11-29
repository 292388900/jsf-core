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

import java.util.List;

public class LookupResult {
    //版本号
    private long dataVer;

    //provider列表
    private List<ProviderUrl> list;

    private String iface;

    private String alias;

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
     * @return the list
     */
    public List<ProviderUrl> getList() {
        return list;
    }

    /**
     * @param list the list to set
     */
    public void setList(List<ProviderUrl> list) {
        this.list = list;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getIface() {
        return iface;
    }

    public void setIface(String iface) {
        this.iface = iface;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("LookupResult(");
        sb.append("iface:").append(this.iface);
        sb.append(",alias:").append(this.alias);
        sb.append(",dataVer:").append(this.dataVer);
        sb.append(",providerList:");
        if (list == null) {
            sb.append("null");
        } else {
            sb.append("{");
            for (ProviderUrl url : list) {
                sb.append(url.toString());
            }
            sb.append("}");
        }
        sb.append(")");
        return sb.toString();
    }
}
