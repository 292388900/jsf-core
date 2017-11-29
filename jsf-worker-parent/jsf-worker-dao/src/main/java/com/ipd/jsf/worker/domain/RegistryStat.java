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

public class RegistryStat implements Serializable, Comparable<RegistryStat>{
	private static final long serialVersionUID = 1L;
	
	private Integer id;
	private Integer safIndexId = 0;
	private String ip;
	private int port;
	private int conns;
	private int callbacks;
	private Date createdTime = new Date();
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getSafIndexId() {
		return safIndexId;
	}
	public void setSafIndexId(Integer safIndexId) {
		this.safIndexId = safIndexId;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public int getConns() {
		return conns;
	}
	public void setConns(int conns) {
		this.conns = conns;
	}
	public int getCallbacks() {
		return callbacks;
	}
	public void setCallbacks(int callbacks) {
		this.callbacks = callbacks;
	}
	public Date getCreatedTime() {
		return createdTime;
	}
	
	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}
	@Override
	public int compareTo(RegistryStat o) {
		if(this.createdTime.after(o.createdTime)){
			return 1;
		}else {
			return -1;
		}
	}
}
