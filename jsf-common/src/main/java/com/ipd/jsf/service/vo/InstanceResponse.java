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

package com.ipd.jsf.service.vo;

import java.util.List;

public class InstanceResponse {
    /*实例列表*/
    private List<Instance> instanceList;

    /*实例总记录数*/
    private int totalRecord;

    /**
     * @return the instanceList
     */
    public List<Instance> getInstanceList() {
        return instanceList;
    }

    /**
     * @param instanceList the instanceList to set
     */
    public void setInstanceList(List<Instance> instanceList) {
        this.instanceList = instanceList;
    }

    /**
     * @return the totalRecord
     */
    public int getTotalRecord() {
        return totalRecord;
    }

    /**
     * @param totalRecord the totalRecord to set
     */
    public void setTotalRecord(int totalRecord) {
        this.totalRecord = totalRecord;
    }
    
    
}
