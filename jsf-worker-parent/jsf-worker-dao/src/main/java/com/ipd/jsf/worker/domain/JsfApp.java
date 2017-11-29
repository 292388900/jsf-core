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

public class JsfApp implements Serializable {

    private static final long serialVersionUID = -4822399358581267614L;

    private Integer id;
    private Integer appId;
    private String appName;
    private String erps;
    private Integer srcType = 1; //0:自动部署；1：手动录入

    //ext
    private String appType; //例如tomcat之类
    private String deptName; //部门信息
    private String developer; //开发者erp,逗号分割
    private String developerLeader; //负责人
    private String domain; //域名
    private String appDevelopGroupName; //开发组
    private String first_branch; //一级部门
    private String second_branch; //二级部门
    private String joneAppLevel; //JONE系统级别
    private String leader; //技术经理erp
    private String leaderName; //技术经理名字
    private String level; //系统级别
    private String levelName; //系统级别名称
    private String token;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAppId() {
        return appId;
    }

    public void setAppId(Integer appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getSrcType() {
        return srcType;
    }

    public void setSrcType(Integer srcType) {
        this.srcType = srcType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getErps() {
        return erps;
    }

    public void setErps(String erps) {
        this.erps = erps;
    }

    private Date createTime = new Date();
    private String creator;

    public String getAppType() {
        return appType;
    }

    public void setAppType(String appType) {
        this.appType = appType;
    }

    public String getDeptName() {
        return deptName;
    }

    public void setDeptName(String deptName) {
        this.deptName = deptName;
    }

    public String getDeveloper() {
        return developer;
    }

    public void setDeveloper(String developer) {
        this.developer = developer;
    }

    public String getDeveloperLeader() {
        return developerLeader;
    }

    public void setDeveloperLeader(String developerLeader) {
        this.developerLeader = developerLeader;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAppDevelopGroupName() {
        return appDevelopGroupName;
    }

    public void setAppDevelopGroupName(String appDevelopGroupName) {
        this.appDevelopGroupName = appDevelopGroupName;
    }

    public String getFirst_branch() {
        return first_branch;
    }

    public void setFirst_branch(String first_branch) {
        this.first_branch = first_branch;
    }

    public String getSecond_branch() {
        return second_branch;
    }

    public void setSecond_branch(String second_branch) {
        this.second_branch = second_branch;
    }

    public String getJoneAppLevel() {
        return joneAppLevel;
    }

    public void setJoneAppLevel(String joneAppLevel) {
        this.joneAppLevel = joneAppLevel;
    }

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader) {
        this.leader = leader;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevelName() {
        return levelName;
    }

    public void setLevelName(String levelName) {
        this.levelName = levelName;
    }

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

}
