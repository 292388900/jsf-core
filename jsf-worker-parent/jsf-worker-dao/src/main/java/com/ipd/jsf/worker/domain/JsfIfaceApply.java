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

import java.sql.Timestamp;

public class JsfIfaceApply{
	private Integer id;
	
	private String interfaceName;
	
	private String department;

	private String departmentCode;
	
	private String ownerUser;
	
	private String remark;
	
	private Integer status = 2;  //是否有效【1:有效; 0:无效；2:新建, 3: 审核通过；4: 已驳回】
	//修改时间
	private Timestamp auditTime;
	//创建者
	private String creator;
	
	private String auditor;
	
	private String uid;
	//创建时间'
	private Timestamp applyTime = new Timestamp(System.currentTimeMillis());
	
	public String getInterfaceName() {
		return interfaceName;
	}
	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}
	public String getDepartment() {
		return department;
	}
	public void setDepartment(String department) {
		this.department = department;
	}

	public String getDepartmentCode() {
		return departmentCode;
	}

	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	public String getOwnerUser() {
		return ownerUser;
	}
	public void setOwnerUser(String ownerUser) {
		this.ownerUser = ownerUser;
	}
	public String getRemark() {
		return remark;
	}
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Timestamp getAuditTime() {
		return auditTime;
	}
	public void setAuditTime(Timestamp auditTime) {
		this.auditTime = auditTime;
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	public String getAuditor() {
		return auditor;
	}
	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}
	public Timestamp getApplyTime() {
		return applyTime;
	}
	public void setApplyTime(Timestamp applyTime) {
		this.applyTime = applyTime;
	}
	
	@Override
	public String toString() {
		return "SafIfaceInfo [interfaceName=" + interfaceName + ", department="
				+ department + ", ownerUser=" + ownerUser +  ", remark=" + remark + ", id=" + id + ", status="
				+ status + ", auditTime="
				+ auditTime + ", creator=" + creator + ", createdTime="
				+ applyTime + "]";
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((interfaceName == null) ? 0 : interfaceName.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		JsfIfaceApply other = (JsfIfaceApply) obj;
		if (interfaceName == null) {
			if (other.interfaceName != null) {
				return false;
			}
		} else if (!interfaceName.equals(other.interfaceName)) {
			return false;
		}
		return true;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
}