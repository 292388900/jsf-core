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
package com.ipd.jsf.registry.domain;

import java.io.Serializable;
import java.util.Date;

/**
 * 实例对象。saf rpc客户端的实例信息
 */
public class JsfIns implements Serializable {
    private static final long serialVersionUID = 1L;
    private String insKey;     // 实例key
    private String ip = "";    //实例ip
    private int pid;           //实例pid
    private int port;          //实例端口
    private long startTime;    //实例启动时间
    private int safVer;        //jsf版本号
    private String language;   //jsf客户端语言
    private Date hb;           //心跳时间
    private Date createTime;   //创建时间
    private String regIp;

    //-------start----保存app和ip的信息-------
    private int jsfAppInsId;
    private int appId;
    private String appName;
    private String appInsId;
    //-------end------保存app和ip的信息-------

    private int insRoom;       //实例所在机房

    //callgraph
    private int cgOpen = 0;
    private int cgEnhance = 0;

    public String getRegIp() {
		return regIp;
	}
	public void setRegIp(String regIp) {
		this.regIp = regIp;
	}
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
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
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
    /**
	 * @return the insRoom
	 */
	public int getInsRoom() {
		return insRoom;
	}
	/**
	 * @param insRoom the insRoom to set
	 */
	public void setInsRoom(int insRoom) {
		this.insRoom = insRoom;
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
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    /**
     * @return the jsfAppInsId
     */
    public int getJsfAppInsId() {
        return jsfAppInsId;
    }
    /**
     * @param jsfAppInsId the jsfAppInsId to set
     */
    public void setJsfAppInsId(int jsfAppInsId) {
        this.jsfAppInsId = jsfAppInsId;
    }
    /**
     * @return the appId
     */
    public int getAppId() {
        return appId;
    }
    /**
     * @param appId the appId to set
     */
    public void setAppId(int appId) {
        this.appId = appId;
    }
    /**
     * @return the appName
     */
    public String getAppName() {
        return appName;
    }
    /**
     * @param appName the appName to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }
    /**
     * @return the appInsId
     */
    public String getAppInsId() {
        return appInsId;
    }
    /**
     * @param appInsId the appInsId to set
     */
    public void setAppInsId(String appInsId) {
        this.appInsId = appInsId;
    }

    public int getCgOpen() {
        return cgOpen;
    }

    public void setCgOpen(int cgOpen) {
        this.cgOpen = cgOpen;
    }

    public int getCgEnhance() {
        return cgEnhance;
    }

    public void setCgEnhance(int cgEnhance) {
        this.cgEnhance = cgEnhance;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Instance(");
        sb.append("insKey:").append(this.insKey);
        sb.append(",ip:").append(this.ip);
        sb.append(",pid:").append(this.pid);
        sb.append(",port:").append(this.port);
        sb.append(",startTime:").append(this.startTime);
        sb.append(",safVer:").append(this.safVer);
        sb.append(",language:").append(this.language);
        sb.append(",room:").append(this.insRoom);
        sb.append(",hb:").append(this.hb);
        sb.append(",createTime:").append(this.createTime);
        sb.append(",regIp:").append(this.regIp);
        if (this.appId > 0) sb.append(",appId:").append(this.appId);
        if (this.appName != null && !this.appName.isEmpty()) sb.append(",appName:").append(this.appName);
        if (this.appInsId != null && !this.appInsId.isEmpty()) sb.append(",appInsId:").append(this.appInsId);
        sb.append(")");
        return sb.toString();
    }
}
