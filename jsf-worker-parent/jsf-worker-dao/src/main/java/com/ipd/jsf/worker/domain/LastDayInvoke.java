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

public class LastDayInvoke implements Serializable{
	
	private static final long serialVersionUID = -7429575155416168524L;
	private int id;
	private String interfaceName;
	private String method;
	private String callTimes;
	private String invokeDate;
	private String invokeDateTime;
	private Date createTime = new Date();
	
	public LastDayInvoke(){};
	public LastDayInvoke(String iface, String method, String invokeDate){
		this.interfaceName = iface;
		this.method = method;
		this.invokeDate = invokeDate;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getCallTimes() {
		return callTimes;
	}
	public void setCallTimes(String callTimes) {
		this.callTimes = callTimes;
	}
	public String getInvokeDate() {
		return invokeDate;
	}
	public void setInvokeDate(String invokeDate) {
		this.invokeDate = invokeDate;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
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

	public String getInvokeDateTime() {
		return invokeDateTime;
	}

	public void setInvokeDateTime(String invokeDateTime) {
		this.invokeDateTime = invokeDateTime;
	}
}
