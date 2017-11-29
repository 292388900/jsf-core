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

public class InterfaceVisit {
    //接口id
    private int interfaceId;

    //接口名
    private String interfaceName;

    //访问者名称
    private String visitorName;

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
     * @return the visitorName
     */
    public String getVisitorName() {
        return visitorName;
    }

    /**
     * @param visitorName the visitorName to set
     */
    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

}
