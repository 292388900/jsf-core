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


public class IfaceInsStat implements Serializable,Comparable<IfaceInsStat>{

	/**  描述  */  
	private static final long serialVersionUID = 1L;
	
	private long id;
	
	private String startTime;
	
	private String endTime;
	
	public String getStartTime() {
		return startTime;
	}



	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}



	public String getEndTime() {
		return endTime;
	}



	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}



	/**
	 * 接口
	 */
	private String interfaceName;
	
	/**
	 * provider 实例数
	 */
	private long pinsNum;
	
	/**
	 * consumer实例数
	 */
	private long cinsNum;
	
	/**
	 * provider 实例较上周增长数
	 */
	private long pinsAdd;
	
	/**
	 * consumer实例数较上周增长数
	 */
	private long cinsAdd;
	
	/**
	 * 总实例数
	 */
	private long insTotalNum;
	
	/**
	 * 实例数总增加量
	 */
	private long insTotalAdd;
	
	
	/**
	 * provider IP数
	 */
	private long pipNum;
	
	/**
	 * consumerIP数
	 */
	private long cipNum;
	
	/**
	 * provider IP较上周增长数
	 */
	private long pipAdd;
	
	/**
	 * consumerIP数较上周增长数
	 */
	private long cipAdd;
	
	/**
	 * 总IP数
	 */
	private long ipTotalNum;
	
	/**
	 * IP数总增加量
	 */
	private long ipTotalAdd;
	
	/**
	 * 第几周
	 */
	private int week;
	
	private Date createTime;
	
	public String getInterfaceName() {
		return interfaceName;
	}



	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}



	public long getPinsNum() {
		return pinsNum;
	}



	public void setPinsNum(long pinsNum) {
		this.pinsNum = pinsNum;
	}



	public long getCinsNum() {
		return cinsNum;
	}



	public void setCinsNum(long cinsNum) {
		this.cinsNum = cinsNum;
	}



	public long getPinsAdd() {
		return pinsAdd;
	}



	public void setPinsAdd(long pinsAdd) {
		this.pinsAdd = pinsAdd;
	}



	public long getCinsAdd() {
		return cinsAdd;
	}



	public void setCinsAdd(long cinsAdd) {
		this.cinsAdd = cinsAdd;
	}



	public long getInsTotalNum() {
		return insTotalNum;
	}



	public void setInsTotalNum(long insTotalNum) {
		this.insTotalNum = insTotalNum;
	}



	public long getInsTotalAdd() {
		return insTotalAdd;
	}



	public void setInsTotalAdd(long insTotalAdd) {
		this.insTotalAdd = insTotalAdd;
	}



	public long getPipNum() {
		return pipNum;
	}



	public void setPipNum(long pipNum) {
		this.pipNum = pipNum;
	}



	public long getCipNum() {
		return cipNum;
	}



	public void setCipNum(long cipNum) {
		this.cipNum = cipNum;
	}



	public long getPipAdd() {
		return pipAdd;
	}



	public void setPipAdd(long pipAdd) {
		this.pipAdd = pipAdd;
	}



	public long getCipAdd() {
		return cipAdd;
	}



	public void setCipAdd(long cipAdd) {
		this.cipAdd = cipAdd;
	}



	public long getIpTotalNum() {
		return ipTotalNum;
	}



	public void setIpTotalNum(long ipTotalNum) {
		this.ipTotalNum = ipTotalNum;
	}



	public long getIpTotalAdd() {
		return ipTotalAdd;
	}



	public void setIpTotalAdd(long ipTotalAdd) {
		this.ipTotalAdd = ipTotalAdd;
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
	public int compareTo(IfaceInsStat o) {
		Long thisTotalAdd = this.insTotalAdd;
		Long otherTotalAdd = o.insTotalAdd;
		if(this.week > o.week){
			return -1;
		}else {
			if(thisTotalAdd > otherTotalAdd){
				return -1;
			}else if(thisTotalAdd < otherTotalAdd){
				return 1;
			}else {
				return this.pinsAdd > o.pinsAdd ? -1 : 1;
			}
		}
	}



	public int getWeek() {
		return week;
	}



	public void setWeek(int week) {
		this.week = week;
	}



	public long getId() {
		return id;
	}



	public void setId(long id) {
		this.id = id;
	}


}
