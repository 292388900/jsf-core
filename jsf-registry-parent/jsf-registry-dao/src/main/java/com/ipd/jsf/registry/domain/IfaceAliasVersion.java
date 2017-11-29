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

/**
 * 记录接口+alias的provider列表版本
 */
public class IfaceAliasVersion {
	private int interfaceId;  //接口id

	private String alias;    //别名

	private long dataVersion;  //接口+alias的provider列表版本

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

	public int hashCode() {
		int hc = 13;
		hc = hc*31 + this.interfaceId;
    	hc = hc*31 + this.alias.hashCode();
		return hc;
	}

	public boolean equals(Object obj){
		if(this == obj){
			return true;
		}
		if(!(obj instanceof IfaceAliasVersion)){
			return false;
		}
		IfaceAliasVersion version = (IfaceAliasVersion)obj;
		if(interfaceId != version.interfaceId){
			return false;
		}
		if (alias == null && version.alias == null) {
			return true;
		}
		if((alias == null && version.alias != null) || (alias != null && version.alias == null) ||
    			!alias.equals(version.alias)){
			return false;
		}
		return true;
	}

	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");
        sb.append("ifaceId:").append(this.interfaceId);
        sb.append(",alias:").append(this.alias);
        sb.append(",version:").append(this.dataVersion);
        sb.append(")");
        return sb.toString();
    }
}
