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
import java.util.HashMap;
import java.util.Map;

public class IfaceGateway implements Serializable{
	
	private static final long serialVersionUID = 7552662645468178906L;
	
	private Integer id;
	
	private Integer interfaceId;
	
	private String interfaceName;
	
	private Integer appId;
	
	private String alias;
	
	private String method;
	
	private Integer invokeTime;
	
	private Map<String, Integer> methodMap = new HashMap<String, Integer>();
	
	private Integer status = 0; // 0新建，1审核通过，2驳回
	
	private String applyer;
	
	private String auditor;
	
	private Date createTime = new Date();
	
	private Date auditTime;

	private String remark;
	
	private String uid;
	
	public Integer getAppId() {
		return appId;
	}

	public void setAppId(Integer appId) {
		this.appId = appId;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getApplyer() {
		return applyer;
	}

	public void setApplyer(String applyer) {
		this.applyer = applyer;
	}

	public String getAuditor() {
		return auditor;
	}

	public void setAuditor(String auditor) {
		this.auditor = auditor;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getAuditTime() {
		return auditTime;
	}

	public void setAuditTime(Date auditTime) {
		this.auditTime = auditTime;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getInterfaceId() {
		return interfaceId;
	}

	public void setInterfaceId(Integer interfaceId) {
		this.interfaceId = interfaceId;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Map<String, Integer> getMethodMap() {
		return methodMap;
	}

	public void setMethodMap(Map<String, Integer> methodMap) {
		this.methodMap = methodMap;
	}

	public Integer getInvokeTime() {
		return invokeTime;
	}

	public void setInvokeTime(Integer invokeTime) {
		this.invokeTime = invokeTime;
	}
	
	@Override
	public String toString() {
		return "{interfaceName: "+interfaceName+", appId: "+appId+", alias: "+alias+", method: "+method+"}";
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
