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

public class InterfaceInfo implements Serializable {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = -4631669696380221240L;

    private Integer interfaceId;

    private String interfaceName;

    private byte important;

    private String remark;

    private ZkNodeInfo zkNodeInfo;

    private String department;

    private String departmentCode;
    
    //erp帐号
    private String ownerUser;

    private String creator;

    private String modifier;
    
    private Date createTime;

    private Date updateTime;

    private byte source;
    
    private Integer valid = 1; //是否有效【1:有效; 0:无效；2:新建, 3: 审核通过；4: 已驳回】
    
    private int hasJsfClient;
    
    /**
     * @return the interfaceId
     */
    public Integer getInterfaceId() {
        return interfaceId;
    }

    /**
     * @param interfaceId the interfaceId to set
     */
    public void setInterfaceId(Integer interfaceId) {
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
     * @return the zkNodeInfo
     */
    public ZkNodeInfo getZkNodeInfo() {
        return zkNodeInfo;
    }

    /**
     * @param zkNodeInfo the zkNodeInfo to set
     */
    public void setZkNodeInfo(ZkNodeInfo zkNodeInfo) {
        this.zkNodeInfo = zkNodeInfo;
    }

    /**
     * @return the ownerUser
     */
    public String getOwnerUser() {
        return ownerUser;
    }

    /**
     * @param ownerUser the ownerUser to set
     */
    public void setOwnerUser(String ownerUser) {
        this.ownerUser = ownerUser;
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


    public String getDepartmentCode() {
        return departmentCode;
    }

    public void setDepartmentCode(String departmentCode) {
        this.departmentCode = departmentCode;
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
     * @return the modifier
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * @param modifier the modifier to set
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
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

    /**
     * @return the source
     */
    public byte getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(byte source) {
        this.source = source;
    }

	public Integer getValid() {
		return valid;
	}

	public void setValid(Integer valid) {
		this.valid = valid;
	}

	public int getHasJsfClient() {
		return hasJsfClient;
	}

	public void setHasJsfClient(int hasJsfClient) {
		this.hasJsfClient = hasJsfClient;
	}

    public String toString(){
        return "interfaceId:" + interfaceId
                + " interfaceName:" + interfaceName
                + " department:" + department
                + " createTime:" + createTime
                + " valid:" + valid
                + " creator:" + creator;
    }

}
