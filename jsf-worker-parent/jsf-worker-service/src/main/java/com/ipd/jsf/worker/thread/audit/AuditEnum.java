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
package com.ipd.jsf.worker.thread.audit;


public enum AuditEnum {
	INTERFACE_APPLY("新建接口", 0),
	GATWAY_INVOKE_APPLY("网关调用申请", 1);
	
	private String name;
	private int value;
	
	private AuditEnum(String name, int value) {
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public int getValue() {
		return value;
	}
	
	public static AuditEnum fromName(String name) {
		for (AuditEnum type : AuditEnum.values()) {
			if (type.name().equals(name))
				return type;
		}
		return null;
	}
	
	public static AuditEnum fromValue(int value) {
		for (AuditEnum type : AuditEnum.values()) {
			if (type.value == value)
				return type;
		}
		return null;
	}
}
