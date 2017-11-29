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


public class JsfInsStat implements Serializable,Cloneable, Comparable<JsfInsStat>{

	/**  描述  */  
	
	private static final long serialVersionUID = 1L;
	
	private int id;

	private int weekend;
	
	private int pInstance;
	
	private int cInstance;
	
	private int totalInstance;
	
	private int pIps;
	
	private int cIps;
	
	private int ipNum;
	
	private Date createTime;
	
	private int totalinsAdd;
	
	private int totalipAdd;
	
	public int getTotalinsAdd() {
		return totalinsAdd;
	}

	public void setTotalinsAdd(int totalinsAdd) {
		this.totalinsAdd = totalinsAdd;
	}

	public int getTotalipAdd() {
		return totalipAdd;
	}

	public void setTotalipAdd(int totalipAdd) {
		this.totalipAdd = totalipAdd;
	}

	public int getPinsAdd() {
		return pinsAdd;
	}

	public void setPinsAdd(int pinsAdd) {
		this.pinsAdd = pinsAdd;
	}

	public int getCinsAdd() {
		return cinsAdd;
	}

	public void setCinsAdd(int cinsAdd) {
		this.cinsAdd = cinsAdd;
	}

	public int getPipAdd() {
		return pipAdd;
	}

	public void setPipAdd(int pipAdd) {
		this.pipAdd = pipAdd;
	}

	public int getCipAdd() {
		return cipAdd;
	}

	public void setCipAdd(int cipAdd) {
		this.cipAdd = cipAdd;
	}

	private int pinsAdd;
	
	private int cinsAdd;
	
	private int pipAdd;
	
	private int cipAdd;
	
	


	public int getpInstance() {
		return pInstance;
	}

	public void setpInstance(int pInstance) {
		this.pInstance = pInstance;
	}

	public int getcInstance() {
		return cInstance;
	}

	public void setcInstance(int cInstance) {
		this.cInstance = cInstance;
	}

	public int getIpNum() {
		return ipNum;
	}

	public void setIpNum(int ipNum) {
		this.ipNum = ipNum;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getWeekend() {
		return weekend;
	}

	public void setWeekend(int weekend) {
		this.weekend = weekend;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
	/**   
	 * @param o
	 * @return  
	 * @see java.lang.Comparable#compareTo(java.lang.Object)  
	 */
	@Override
	public int compareTo(JsfInsStat o) {
		Integer w = this.weekend;
		Integer w1 = o.weekend;
		return w.compareTo(w1);
	}

	public int getTotalInstance() {
		return totalInstance;
	}

	public void setTotalInstance(int totalInstance) {
		this.totalInstance = totalInstance;
	}

	public int getpIps() {
		return pIps;
	}

	public void setpIps(int pIps) {
		this.pIps = pIps;
	}

	public int getcIps() {
		return cIps;
	}

	public void setcIps(int cIps) {
		this.cIps = cIps;
	}

}
