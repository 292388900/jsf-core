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

public class JsfAlarmStatisticsItem implements Serializable {

	private int alarmType;//报警类型
	private int count;//报警数量


	public int getAlarmType() {
		return alarmType;
	}
	public void setAlarmType(int alarmType) {
		this.alarmType = alarmType;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}

	public int hashCode(){
		int hc = 13;
		hc = hc*31 + this.alarmType;
		hc = hc*31 + this.count;
		return hc;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof JsfAlarmStatisticsItem)) {
			return false;
		}
		if (this == obj) {
			return true;
		}

		JsfAlarmStatisticsItem other = (JsfAlarmStatisticsItem)obj;

		if(this.alarmType == other.alarmType && this.count == other.count ){
			return true;
		}
		return false;
	}



	/*
    private Integer alarmKey;
    private Integer count;


    public Integer getAlarmKey() {
        return alarmKey;
    }
    public void setAlarmKey(Integer alarmKey) {
        this.alarmKey = alarmKey;
    }
    public Integer getCount() {
        return count;
    }
    public void setCount(Integer count) {
        this.count = count;
    }

*/



	/*
	public int hashCode(){
    	int hc = 13;
    	hc = hc*31 + (this.alarmKey==null?0 : this.alarmKey.hashCode());
    	hc = hc*31 + (this.count==null?0 : this.count.hashCode());
    	return hc;
    }
    */

	/*
    public boolean equals(Object obj){
    	if(!(obj instanceof JsfAlarmStatisticsItem)){
    		return false;
    	}
    	if(this == obj){
    		return true;
    	}
    	JsfAlarmStatisticsItem other = (JsfAlarmStatisticsItem)obj;
    	if(!this.alarmKey.equals(other.getAlarmKey())){
    		return false;
    	}

    	if(this.count == null && other.count != null ||
    			(this.count != null && other.count == null) ||
    			(this.count != other.count && !this.count.equals(other.count))){
    		return false;
    	}

    	return true;
    }
    */
    

}