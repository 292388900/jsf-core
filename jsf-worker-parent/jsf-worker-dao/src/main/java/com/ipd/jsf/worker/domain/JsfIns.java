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

public class JsfIns implements Serializable {
    private static final long serialVersionUID = 1L;
    private String insKey;  // 实例key
    private String ip = "";   //实例ip
    private int pid;    //实例pid
    private int status;
    private long startTime;   //实例启动时间
    private int safVer;   //saf版本号
    private Date hb;    //心跳时间
    private String regIp;  //注册中心
    private String language;   //语言
    /**
     * @return the insKey
     */
    public String getInsKey() {
        return insKey;
    }
    /**
     * @param insKey the insKey to set
     */
    public void setInsKey(String insKey) {
        this.insKey = insKey;
    }
    /**
     * @return the ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }
    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
    }
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
     * @return the safVer
     */
    public int getSafVer() {
        return safVer;
    }
    /**
     * @param safVer the safVer to set
     */
    public void setSafVer(int safVer) {
        this.safVer = safVer;
    }
    /**
     * @return the hb
     */
    public Date getHb() {
        return hb;
    }
    /**
     * @param hb the hb to set
     */
    public void setHb(Date hb) {
        this.hb = hb;
    }
    /**
     * @return the regIp
     */
    public String getRegIp() {
        return regIp;
    }
    /**
     * @param regIp the regIp to set
     */
    public void setRegIp(String regIp) {
        this.regIp = regIp;
    }
    /**
     * @return the language
     */
    public String getLanguage() {
        return language;
    }
    /**
     * @param language the language to set
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JsfInstance(");
        sb.append("insKey:").append(this.insKey);
        sb.append(",ip:").append(this.ip);
        sb.append(",pid:").append(this.pid);
        sb.append(",status:").append(this.status);
        sb.append(",startTime:").append(this.startTime);
        sb.append(",safVer:").append(this.safVer);
        sb.append(",hb:").append(this.hb);
        sb.append(",regIp:").append(this.regIp);
        sb.append(",language:").append(this.language);
        sb.append(")");
        return sb.toString();
    }
}
