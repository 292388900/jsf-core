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

public class DeployApp
  implements Serializable
{
  private static final long serialVersionUID = -7727349994515585830L;
  private String alias;
  private String appDevelopGroupName;
  private Integer appId;
  private String appType;
  private Date createTime;
  private String developer;
  private String developerLeader;
  private String domain;
  private int isDel;
  private String name;
  private String orderFieldNextType;
  private DeploySys sys;
  private String deptName;

  public String getAlias()
  {
    return this.alias;
  }
  public void setAlias(String alias) {
    this.alias = alias;
  }
  public String getAppDevelopGroupName() {
    return this.appDevelopGroupName;
  }
  public void setAppDevelopGroupName(String appDevelopGroupName) {
    this.appDevelopGroupName = appDevelopGroupName;
  }
  public Integer getAppId() {
    return this.appId;
  }
  public void setAppId(Integer appId) {
    this.appId = appId;
  }
  public String getAppType() {
    return this.appType;
  }
  public void setAppType(String appType) {
    this.appType = appType;
  }
  public Date getCreateTime() {
    return this.createTime;
  }
  public void setCreateTime(Date createTime) {
    this.createTime = createTime;
  }
  public String getDeveloper() {
    return this.developer;
  }
  public void setDeveloper(String developer) {
    this.developer = developer;
  }
  public String getDeveloperLeader() {
    return this.developerLeader;
  }
  public void setDeveloperLeader(String developerLeader) {
    this.developerLeader = developerLeader;
  }
  public String getDomain() {
    return this.domain;
  }
  public void setDomain(String domain) {
    this.domain = domain;
  }
  public int getIsDel() {
    return this.isDel;
  }
  public void setIsDel(int isDel) {
    this.isDel = isDel;
  }
  public String getName() {
    return this.name;
  }
  public void setName(String name) {
    this.name = name;
  }
  public String getOrderFieldNextType() {
    return this.orderFieldNextType;
  }
  public void setOrderFieldNextType(String orderFieldNextType) {
    this.orderFieldNextType = orderFieldNextType;
  }
  public DeploySys getSys() {
    return this.sys;
  }
  public void setSys(DeploySys sys) {
    this.sys = sys;
  }

  public String getDeptName() {
    return deptName;
  }

  public void setDeptName(String deptName) {
    this.deptName = deptName;
  }

}