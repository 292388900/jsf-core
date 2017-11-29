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
import java.text.SimpleDateFormat;
import java.util.Date;

public class IfaceAppDept implements Serializable{
	
	private static final long serialVersionUID = -3087594915742158022L;
	
	private Integer id;
	private Integer appId;
	private Integer interfaceId;
	private String interfaceName;
	private String firstDept;
	private String secondDept;
	private String thirdDept;
	private Date createTime = new Date();
	private String creator = "worker";
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAppId() {
		return appId;
	}
	public void setAppId(Integer appId) {
		this.appId = appId;
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
	public String getFirstDept() {
		return firstDept;
	}
	public void setFirstDept(String firstDept) {
		this.firstDept = firstDept;
	}
	public String getSecondDept() {
		return secondDept;
	}
	public void setSecondDept(String secondDept) {
		this.secondDept = secondDept;
	}
	public String getThirdDept() {
		return thirdDept;
	}
	public void setThirdDept(String thirdDept) {
		this.thirdDept = thirdDept;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	public static void main(String[] args) {
		System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(1430994212770L));
	}
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}

}
