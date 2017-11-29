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

package com.ipd.jsf.alarm.domain;

import java.io.Serializable;
import java.util.Date;

public class JsfAlarmHistory implements Serializable {
	private static final long serialVersionUID = -8230264653671728682L;

	/**
     * saf_alarm_history.id (自增id)
     */
    private Integer id;

    /**
     * saf_alarm_history.alarm_key (报警约定key)
     */
    private String alarmKey;

    /**
     * saf_alarm_history.interface_name (接口名。为空时'')
     */
    private String interfaceName;

    /**
     * saf_alarm_history.method_name (方法名。为空时'')
     */
    private String methodName;

    /**
     * saf_alarm_history.content (报警内容（以邮件为准）)
     */
    private String content;

    /**
     * saf_alarm_history.erps (收件人erp。分号隔开)
     */
    private String erps;

    /**
     * saf_alarm_history.alarm_type (报警类型（代码中枚举定义）)
     */
    private Byte alarmType;

    /**
     * saf_alarm_history.is_alarmed (是否已报警。0：否；1：是)
     */
    private Byte isAlarmed;

    /**
     * saf_alarm_history.create_time (记录时间)
     */
    private Date createTime;

    /**
     * saf_alarm_history.alarm_time (报警时间)
     */
    private Date alarmTime;

    /**
     * saf_alarm_history.remarks (备注。短信发送成功后记录短信接收号码)
     */
    private String remarks;
    
    private String extendKey1;
    
    private String extendKey2;
    
    private String regIp;

    /**
     * saf_alarm_history.alarm_ip (报警ip)
     */
    private String alarmIp;

	public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlarmKey() {
        return alarmKey;
    }

    public void setAlarmKey(String alarmKey) {
        this.alarmKey = alarmKey;
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getErps() {
        return erps;
    }

    public void setErps(String erps) {
        this.erps = erps;
    }

    public Byte getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Byte alarmType) {
        this.alarmType = alarmType;
    }

    public Byte getIsAlarmed() {
		return isAlarmed;
	}

	public void setIsAlarmed(Byte isAlarmed) {
		this.isAlarmed = isAlarmed;
	}

	public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getAlarmTime() {
        return alarmTime;
    }

    public void setAlarmTime(Date alarmTime) {
        this.alarmTime = alarmTime;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
    
    public String getExtendKey1() {
		return extendKey1;
	}

	public void setExtendKey1(String extendKey1) {
		this.extendKey1 = extendKey1;
	}

	public String getExtendKey2() {
		return extendKey2;
	}

	public void setExtendKey2(String extendKey2) {
		this.extendKey2 = extendKey2;
	}

    public String getAlarmIp() {return alarmIp;}

    public void setAlarmIp(String alarmIp) {this.alarmIp = alarmIp;}


    public int hashCode(){
    	int hc = 13;
    	hc = hc*31 + this.alarmKey.hashCode();
    	hc = hc*31 + (this.interfaceName==null?0 : this.interfaceName.hashCode());
    	hc = hc*31 + (this.methodName==null?0 : this.methodName.hashCode());
    	hc = hc*31 + this.alarmType.hashCode();
    	hc = hc*31 + (this.extendKey1==null?0:this.extendKey1.hashCode());
    	hc = hc*31 + (this.extendKey2==null?0: this.extendKey2.hashCode());
    	return hc;
    }
    
    public boolean equals(Object obj){
    	if(!(obj instanceof JsfAlarmHistory)){
    		return false;
    	}
    	if(this == obj){
    		return true;
    	}
    	JsfAlarmHistory other = (JsfAlarmHistory)obj;
    	if(!this.alarmKey.equals(other.getAlarmKey())){
    		return false;
    	}
    	if(this.interfaceName == null && other.interfaceName != null ||
    			(this.interfaceName != null && other.interfaceName == null) ||
    			(this.interfaceName != other.interfaceName && !this.interfaceName.equals(other.interfaceName))){
    		return false;
    	}
    	if(this.methodName == null && other.methodName != null ||
    			(this.methodName != null && other.methodName == null) ||
    			(this.methodName != other.methodName && !this.methodName.equals(other.methodName))){
    		return false;
    	}
    	if(this.extendKey1 == null && other.extendKey1 != null ||
    			(this.extendKey1 != null && other.extendKey1 == null) ||
    			(this.extendKey1 != other.extendKey1 && !this.extendKey1.equals(other.extendKey1))){
    		return false;
    	}

    	if(this.extendKey2 == null && other.extendKey2 != null ||
    			(this.extendKey2 != null && other.extendKey2 == null) ||
    			(this.extendKey2 != other.extendKey2 && !this.extendKey2.equals(other.extendKey2))){
    		return false;
    	}

    	if(alarmType.byteValue() != other.alarmType.byteValue()){
    		return false;
    	}
    	return true;
    }
    
    public String getCombineKey(){
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.alarmKey.trim()).append("_").append(this.interfaceName==null?"":this.interfaceName.trim())
		.append("_").append(this.methodName==null?"":this.methodName.trim()).append("_")
		.append(this.alarmType);
    	return sb.toString();
    }

	public String getRegIp() {
		return regIp;
	}

	public void setRegIp(String regIp) {
		this.regIp = regIp;
	}
}