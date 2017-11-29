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

public class JsfAlarmSetting implements Serializable {
    /**
     * saf_alarm_setting.id
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private Integer id;

    /**
     * saf_alarm_setting.alarm_key (报警key)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private String alarmKey;
    
    private String interfaceName;
    
    private String methodName;
    
    private Byte alarmType;
    
    private String extendKey1;
    
    private String extendKey2;

    /**
     * saf_alarm_setting.alarm_desc (报警描述)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private String alarmDesc;

    /**
     * saf_alarm_setting.interval (报警间隔（秒）)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private Integer alarmInterval;//报警间隔: 报警时间距当前时间小于等于interval的时候才报警

    public Integer getNotifyType() {
		return notifyType;
	}

	public void setNotifyType(Integer notifyType) {
		this.notifyType = notifyType;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public Integer getThreshold() {
		return threshold;
	}

	public void setThreshold(Integer threshold) {
		this.threshold = threshold;
	}

	/**
     * saf_alarm_setting.is_valid (是否有效：0：无效；1：有效)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private Byte isValid;

    /**
     * saf_alarm_setting.user_erp (添加、更新人erp)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private String userErp;

    /**
     * saf_alarm_setting.create_time (添加时间)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private Date createTime;

    /**
     * saf_alarm_setting.update_time (更新时间)
     * @ibatorgenerated 2014-08-20 11:19:21
     */
    private Date updateTime;
    
	private Integer notifyType; //0邮件和短信报警，1只短信，2只邮件
	private String level; // info\warn\error\fatal
	private Integer threshold; // 同类型的报警阀值, 即相同类型最多报警多少次，0表示不限制

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

    public String getAlarmDesc() {
        return alarmDesc;
    }

    public void setAlarmDesc(String alarmDesc) {
        this.alarmDesc = alarmDesc;
    }

    public Integer getAlarmInterval() {
        return alarmInterval;
    }

    public void setAlarmInterval(Integer alarmInterval) {
        this.alarmInterval = alarmInterval;
    }

    public Byte getIsValid() {
        return isValid;
    }

    public void setIsValid(Byte isValid) {
        this.isValid = isValid;
    }

    public String getUserErp() {
        return userErp;
    }

    public void setUserErp(String userErp) {
        this.userErp = userErp;
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

	public Byte getAlarmType() {
		return alarmType;
	}

	public void setAlarmType(Byte alarmType) {
		this.alarmType = alarmType;
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
	
	public int hashCode(){
    	int hc = 13;
    	hc = hc*31 + this.alarmKey.hashCode();
    	hc = hc*31 + (this.interfaceName==null?0 : this.interfaceName.hashCode());
    	hc = hc*31 + (this.methodName==null?0 : this.methodName.hashCode());
    	hc = hc*31 + this.alarmType.hashCode();
    	hc = hc*31 + (this.extendKey1==null?0 : this.extendKey1.hashCode());
    	hc = hc*31 + (this.extendKey2==null?0 : this.extendKey2.hashCode());
    	return hc;
    }
    
    public boolean equals(Object obj){
    	if(!(obj instanceof JsfAlarmHistory)){
    		return false;
    	}
    	if(this == obj){
    		return true;
    	}
    	JsfAlarmSetting other = (JsfAlarmSetting)obj;
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
}