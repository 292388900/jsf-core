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
package com.ipd.jsf.worker.domain;

import java.io.Serializable;

public class InstanceInterface implements Serializable {
    private static final long serialVersionUID = 1L;
    private String insKey;
    private int interfaceId;
    private int serverStatus;
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
	 * @return the serverStatus
	 */
	public int getServerStatus() {
		return serverStatus;
	}
	/**
	 * @param serverStatus the serverStatus to set
	 */
	public void setServerStatus(int serverStatus) {
		this.serverStatus = serverStatus;
	}
    
}
