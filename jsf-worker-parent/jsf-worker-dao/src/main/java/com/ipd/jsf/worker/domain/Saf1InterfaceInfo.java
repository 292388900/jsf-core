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


public class Saf1InterfaceInfo {
    //接口名
    private String interfaceName;
    //创建人
    private String creator;
    //部门
    private String department;
    //erp帐号
    private String viewUsers;
    //描述
    private String remark;
    //是否重要
    private byte important;
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
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }
    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }
    /**
     * @return the department
     */
    public String getDepartment() {
        return department;
    }
    /**
     * @param department the department to set
     */
    public void setDepartment(String department) {
        this.department = department;
    }
    /**
     * @return the viewUsers
     */
    public String getViewUsers() {
        return viewUsers;
    }
    /**
     * @param viewUsers the viewUsers to set
     */
    public void setViewUsers(String viewUsers) {
        this.viewUsers = viewUsers;
    }
    /**
     * @return the remark
     */
    public String getRemark() {
        return remark;
    }
    /**
     * @param remark the remark to set
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }
    /**
     * @return the important
     */
    public byte getImportant() {
        return important;
    }
    /**
     * @param important the important to set
     */
    public void setImportant(byte important) {
        this.important = important;
    }
    
}
