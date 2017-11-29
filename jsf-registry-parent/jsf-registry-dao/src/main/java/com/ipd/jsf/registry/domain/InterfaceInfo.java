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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InterfaceInfo implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4631669696380221240L;

    private int interfaceId;

    private String interfaceName;

    /** 创建时间 **/
    private Date createTime;

    /**  provider 更新版本号     */
    private Date updateTime;

    /**  配置更新版本号     */
    private Date configUpdateTime;

    /** aliasVersionList */
    private List<IfaceAliasVersion> versionList = new ArrayList<IfaceAliasVersion>();

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
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the updateTime
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return the configUpdateTime
     */
    public Date getConfigUpdateTime() {
        return configUpdateTime;
    }

    /**
     * @param configUpdateTime the configUpdateTime to set
     */
    public void setConfigUpdateTime(Date configUpdateTime) {
        this.configUpdateTime = configUpdateTime;
    }

	/**
	 * @return the versionList
	 */
	public List<IfaceAliasVersion> getVersionList() {
		return versionList;
	}

	/**
	 * @param versionList the versionList to set
	 */
	public void setVersionList(List<IfaceAliasVersion> versionList) {
		this.versionList = versionList;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("interfaceInfo(");
        sb.append("interfaceId:").append(this.interfaceId);
        sb.append(",interfaceName:").append(this.interfaceName);
        sb.append(",updateTime:").append(this.updateTime);
        sb.append(",configUpdateTime:").append(this.configUpdateTime);
        sb.append(",versionList:").append(this.versionList);
        sb.append(")");
        return sb.toString();
    }
}
