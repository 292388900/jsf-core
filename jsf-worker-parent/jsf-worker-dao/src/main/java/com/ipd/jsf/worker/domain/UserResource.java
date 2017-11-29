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

public class UserResource implements Serializable{
	
	private static final long serialVersionUID = 7150039892525183236L;
	
	private Integer id;
	private String pin;
	private Integer resId;
	private Integer roleId;
	private Integer resType;
	private Integer pcType;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	public Integer getResId() {
		return resId;
	}
	public void setResId(Integer resId) {
		this.resId = resId;
	}
	public Integer getRoleId() {
		return roleId;
	}
	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}
	public Integer getResType() {
		return resType;
	}
	public void setResType(Integer resType) {
		this.resType = resType;
	}
	
	@Override
	public String toString() {
		return "UserResource [id=" + id + ", pin=" + pin + ", resId=" + resId
				+ ", roleId=" + roleId + ", resType=" + resType + "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pin == null) ? 0 : pin.hashCode());
		result = prime * result + ((resId == null) ? 0 : resId.hashCode());
		result = prime * result + ((resType == null) ? 0 : resType.hashCode());
		result = prime * result + ((roleId == null) ? 0 : roleId.hashCode());
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
		UserResource other = (UserResource) obj;
		if (pin == null) {
			if (other.pin != null) {
				return false;
			}
		} else if (!pin.equals(other.pin)) {
			return false;
		}
		if (resId == null) {
			if (other.resId != null) {
				return false;
			}
		} else if (!resId.equals(other.resId)) {
			return false;
		}
		if (resType == null) {
			if (other.resType != null) {
				return false;
			}
		} else if (!resType.equals(other.resType)) {
			return false;
		}
		if (roleId == null) {
			if (other.roleId != null) {
				return false;
			}
		} else if (!roleId.equals(other.roleId)) {
			return false;
		}
		return true;
	}
	public Integer getPcType() {
		return pcType;
	}
	public void setPcType(Integer pcType) {
		this.pcType = pcType;
	}
}
	