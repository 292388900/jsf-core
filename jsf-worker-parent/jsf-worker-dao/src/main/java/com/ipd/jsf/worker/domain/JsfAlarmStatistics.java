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

public class JsfAlarmStatistics implements Serializable {

    private Integer id;//数据库中的自增ID，无用
    private String alarmDate;//报警时间
    private int alarmType;//报警类型
    private String alarmIp;//报警的ip地址
    private String alarmInterface;//报警的接口名字
    private int alarmCount;//报警数量





    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }

    //public String getStatistics() {return statistics;}

    //public void setStatistics(String statistics) {this.statistics = statistics;}



    public int getAlarmType() {
        return alarmType;
    }
    public void setAlarmType(int alarmType) {
        this.alarmType = alarmType;
    }
    public int getAlarmCount() {
        return alarmCount;
    }
    public void setAlarmCount(int alarmCount) {
        this.alarmCount = alarmCount;
    }
    public String getAlarmIp() {
        return alarmIp;
    }

    public void setAlarmIp(String alarmIp) {
        this.alarmIp = alarmIp;
    }

    public String getAlarmInterface() {
        return alarmInterface;
    }

    public void setAlarmInterface(String alarmInterface) {
        this.alarmInterface = alarmInterface;
    }

    public int hashCode() {
        int hc = 13;
        hc = hc * 31 + (this.id == null ? 0 : this.id.hashCode());
        hc = hc * 31 + (this.alarmDate == null ? 0 : this.alarmDate.hashCode());
        //hc = hc * 31 + (this.statistics == null ? 0 : this.statistics.hashCode());
        hc = hc * 31 + (this.alarmType*31);
        hc = hc * 31 + (this.alarmCount*31);
        return hc;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof JsfAlarmStatistics)) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        JsfAlarmStatistics other = (JsfAlarmStatistics) obj;
        if (!this.id.equals(other.getId())) {
            return false;
        }

        if (this.alarmDate == null && other.alarmDate != null ||
                (this.alarmDate != null && other.alarmDate == null) ||
                (this.alarmDate != other.alarmDate && !this.alarmDate.equals(other.alarmDate))) {
            return false;
        }
        /*
        if (this.statistics == null && other.statistics != null ||
                (this.statistics != null && other.statistics == null) ||
                (this.statistics != other.statistics && !this.statistics.equals(other.statistics))) {
            return false;
        }
*/
        if (this.alarmType != other.alarmType){
            return false;
        }

        if (this.alarmCount != other.alarmCount) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return "JsfAlarmStatistics{" +
                "id=" + id +
                ", alarmDate='" + alarmDate + '\'' +
                ", alarmType=" + alarmType +
                ", alarmIp=" + alarmIp +
                ", alarmInterface=" + alarmInterface +
                ", alarmCount=" + alarmCount +
                '}';
    }
}