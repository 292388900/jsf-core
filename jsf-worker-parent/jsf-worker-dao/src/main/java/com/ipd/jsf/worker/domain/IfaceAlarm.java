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

public class IfaceAlarm implements Serializable {

    private static final long serialVersionUID = 3653400283612610075L;

    private Integer id;
    private Integer interfaceId;
    private String interfaceName;
    private String alias;
    private Integer alarmType;
    private Integer threshold;
    private Integer pcType;
    private String alarmUser;
    private String alarmIp;
    private String ipRegular;
    private boolean mailAlarm;
    private boolean mmsAlarm;
    private Date createdTime = new Date();
    private Date updateTime = new Date();

    public boolean isMailAlarm() {
        return mailAlarm;
    }

    public void setMailAlarm(boolean mailAlarm) {
        this.mailAlarm = mailAlarm;
    }

    public boolean isMmsAlarm() {
        return mmsAlarm;
    }

    public void setMmsAlarm(boolean mmsAlarm) {
        this.mmsAlarm = mmsAlarm;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getAlarmType() {
        return alarmType;
    }

    public void setAlarmType(Integer alarmType) {
        this.alarmType = alarmType;
    }

    public Integer getThreshold() {
        return threshold;
    }

    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }

    public Integer getPcType() {
        return pcType;
    }

    public void setPcType(Integer pcType) {
        this.pcType = pcType;
    }

    public String getAlarmUser() {
        return alarmUser;
    }

    public void setAlarmUser(String alarmUser) {
        this.alarmUser = alarmUser;
    }

    public String getAlarmIp() {
        return alarmIp;
    }

    public void setAlarmIp(String alarmIp) {
        this.alarmIp = alarmIp;
    }

    /**
     * @return the ipRegular
     */
    public String getIpRegular() {
        return ipRegular;
    }

    /**
     * @param ipRegular the ipRegular to set
     */
    public void setIpRegular(String ipRegular) {
        this.ipRegular = ipRegular;
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

}