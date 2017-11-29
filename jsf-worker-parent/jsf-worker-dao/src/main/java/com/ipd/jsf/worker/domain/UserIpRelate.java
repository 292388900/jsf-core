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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.springframework.util.StringUtils;

public class UserIpRelate implements Serializable{
	
	private final static String PROVIDER = "provider";
	
	private static final long serialVersionUID = -5622765337420223872L;
	private int id;
	private String erp;
	private List<String> ips = new ArrayList<String>();
	private String ipList;
	private String pcType = PROVIDER;
	private int src = 1; // 1 worker; 2 管理端
	private Date createTime;
	private Date updateTime;
	
	public String getErp() {
		return erp;
	}
	public void setErp(String erp) {
		this.erp = erp;
	}
	public List<String> getIps() {
		return ips;
	}
	public void setIps(String ips) {
		if(StringUtils.hasText(ips)){
			this.ips = Arrays.asList(ips.split(","));
		}
		this.ips = new ArrayList<String>();
	}
	public String getPcType() {
		return pcType;
	}
	public void setPcType(String pcType) {
		this.pcType = pcType;
	}
	public int getSrc() {
		return src;
	}
	public void setSrc(int src) {
		this.src = src;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getIpList() {
		return ipList;
	}
	public void setIpList(String ipList) {
		if(StringUtils.hasText(ipList)){
			this.ips = Arrays.asList(ipList.split(","));
		}
		this.ipList = ipList;
	}
}
