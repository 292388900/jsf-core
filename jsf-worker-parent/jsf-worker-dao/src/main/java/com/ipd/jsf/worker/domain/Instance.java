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

import java.util.Set;

public class Instance {
    private Set<Integer> interfaceIdSet;  //实例下的接口列表
	/**
     * @return the interfaceIdSet
     */
    public Set<Integer> getInterfaceIdSet() {
        return interfaceIdSet;
    }
    /**
     * @param interfaceIdSet the interfaceIdSet to set
     */
    public void setInterfaceIdSet(Set<Integer> interfaceIdSet) {
        this.interfaceIdSet = interfaceIdSet;
    }
}
