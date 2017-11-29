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

import java.util.Date;

public class InsServer {
	private String insKey;
	private String serverUniqkey;
	private Date createTime;
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
	 * @return the serverUniqkey
	 */
	public String getServerUniqkey() {
		return serverUniqkey;
	}
	/**
	 * @param serverUniqkey the serverUniqkey to set
	 */
	public void setServerUniqkey(String serverUniqkey) {
		this.serverUniqkey = serverUniqkey;
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

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("insServer:{insKey:").append(insKey).append(",server uniqkey:").append(serverUniqkey).append("}");
		return sb.toString();
	}
}
