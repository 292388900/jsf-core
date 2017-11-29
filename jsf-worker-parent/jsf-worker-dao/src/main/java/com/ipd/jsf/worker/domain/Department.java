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

public class Department implements Serializable{
	
	/**  描述  */  
	
	private static final long serialVersionUID = 1L;

	private int id;

	private String departmentCode;
	private String departmentName;
	private int departmentLevel;
	private String parentCode;
	private int synchro = 1;//是否同步标志，1表示同步，0表示不同步，不同步的数据不会被worker修改刷新
	private Date createdTime;
	private Date updateTime;

	public Department(){
		this.departmentCode = "";
		this.departmentName = "";
		this.departmentLevel = -1;
	}

	public Department(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	public Department(String departmentCode, String departmentName) {
		this.departmentCode = departmentCode;
		this.departmentName = departmentName;
	}

	public Department(String departmentCode, String departmentName, int departmentLevel) {
		this.departmentCode = departmentCode;
		this.departmentName = departmentName;
		this.departmentLevel = departmentLevel;
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDepartmentCode() {
		return departmentCode;
	}

	public void setDepartmentCode(String departmentCode) {
		this.departmentCode = departmentCode;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public int getDepartmentLevel() {
		return departmentLevel;
	}

	public void setDepartmentLevel(int departmentLevel) {
		this.departmentLevel = departmentLevel;
	}

	public int getSynchro() {
		return synchro;
	}

	public void setSynchro(int synchro) {
		this.synchro = synchro;
	}

	public Date getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}


	public String toString(){
		StringBuilder s = new StringBuilder();
		if(departmentCode != null){
			s.append(departmentCode);
			s.append("  ");
		}
		if(departmentName != null){
			s.append(departmentName);
			s.append("  ");
		}
		s.append(departmentLevel);
		s.append("  ");

		if(parentCode != null){
			s.append(parentCode);
			s.append("  ");
		}
		s.append(synchro);

		return  s.toString();
	}

}