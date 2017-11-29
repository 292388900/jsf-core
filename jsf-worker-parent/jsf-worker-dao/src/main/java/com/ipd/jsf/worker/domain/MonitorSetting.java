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
import java.util.Map;

public class MonitorSetting implements Serializable, Comparable<MonitorSetting>{
	private static final long serialVersionUID = 8608328733000980208L;
	
	private Integer interfaceId;
	private String interfaceName;
	private String method;
	private String sendInteval;
	private Map<String, String> methodMap;
	private String alias;
	private int pNum;
	private int cNum;
	
	private int cntOneHour = 0;

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

	public String getSendInteval() {
		return sendInteval;
	}

	public void setSendInteval(String sendInteval) {
		this.sendInteval = sendInteval;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getpNum() {
		return pNum;
	}

	public void setpNum(int pNum) {
		this.pNum = pNum;
	}

	public int getcNum() {
		return cNum;
	}

	public void setcNum(int cNum) {
		this.cNum = cNum;
	}

	public int getCntOneHour() {
		return cntOneHour;
	}

	public void setCntOneHour(int cntOneHour) {
		this.cntOneHour = cntOneHour;
	}

	@Override
	public int compareTo(MonitorSetting o) {
		return this.cntOneHour < o.cntOneHour ? 1 : -1;
	}

	public Map<String, String> getMethodMap() {
		return methodMap;
	}

	public void setMethodMap(Map<String, String> methodMap) {
		this.methodMap = methodMap;
	}
	
	@Override
	public String toString() {
		return "[interfaceId: " + interfaceId +", interfaceName: " +interfaceName + ", method: "+ method
				+", alias: " + alias + ", pNum: " + pNum +", cNum: " + cNum + ", cntOfHour: " + cntOneHour +", ,methodMap: " + methodMap +"]";
	}
}
