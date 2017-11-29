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

package com.ipd.jsf.version.common.domain;

public class IfaceServer {
	private String uniqKey;  //server的业务主键

	private int interfaceId;  //接口id

	private String alias;  //alias

	/**
	 * @return the uniqKey
	 */
	public String getUniqKey() {
		return uniqKey;
	}

	/**
	 * @param uniqKey the uniqKey to set
	 */
	public void setUniqKey(String uniqKey) {
		this.uniqKey = uniqKey;
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

	@Override
	public String toString() {
		return "IfaceServer{ uniqkey:" + this.uniqKey + ", interfaceId:" + this.interfaceId + ", alias:" + this.alias;
	}
}
