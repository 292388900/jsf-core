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
import java.util.Date;

public class InterfaceProperty implements Serializable {

    private static final long serialVersionUID = 9158419052197817051L;

    private String interfaceName;

    private String paramKey;

    private String paramValue;

    private Date updateTime;

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
     * @return the paramKey
     */
    public String getParamKey() {
        return paramKey;
    }

    /**
     * @param paramKey the paramKey to set
     */
    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    /**
     * @return the paramValue
     */
    public String getParamValue() {
        return paramValue;
    }

    /**
     * @param paramValue the paramValue to set
     */
    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
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

}
