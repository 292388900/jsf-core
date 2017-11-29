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

public class ServerAlias {
    //server id
    private int id;

    //动态分组后指向的别名
    private String targetAlias;

    //接口id
    private int interfaceId;

    //分组类型：1-扩展(copy类型)，2-路由(cut类型)
    private byte aliasType;

    //原有的alias
    private String srcAlias;

    private int optType;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
	 * @return the targetAlias
	 */
	public String getTargetAlias() {
		return targetAlias;
	}

	/**
	 * @param targetAlias the targetAlias to set
	 */
	public void setTargetAlias(String targetAlias) {
		this.targetAlias = targetAlias;
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
     * @return the aliasType
     */
    public byte getAliasType() {
        return aliasType;
    }

    /**
     * @param aliasType the aliasType to set
     */
    public void setAliasType(byte aliasType) {
        this.aliasType = aliasType;
    }

	/**
	 * @return the srcAlias
	 */
	public String getSrcAlias() {
		return srcAlias;
	}

	/**
	 * @param srcAlias the srcAlias to set
	 */
	public void setSrcAlias(String srcAlias) {
		this.srcAlias = srcAlias;
	}

	/**
	 * @return the optType
	 */
	public int getOptType() {
		return optType;
	}

	/**
	 * @param optType the optType to set
	 */
	public void setOptType(int optType) {
		this.optType = optType;
	}


	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServerAlias(");
        sb.append("interfaceId:").append(this.interfaceId);
        sb.append(",aliasType:").append(this.aliasType);
        sb.append(",srcAlias:").append(this.srcAlias);
        sb.append(",targetAlias:").append(this.targetAlias);
        sb.append(",optType:").append(this.optType);
        sb.append(")");
        return sb.toString();
    }
	
}
