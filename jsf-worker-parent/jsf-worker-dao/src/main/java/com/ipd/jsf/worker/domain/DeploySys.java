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

public class DeploySys
  implements Serializable
{
  private static final long serialVersionUID = 3687651757123777056L;
  private String appDevelopGroupName;
  private String busDomain;
  private String first_branch;
  private String joneAppType;
  private String leader;
  private String leader_name;
  private int level;
  private String level_name;
  private String mainBusDomain;
  private String name;
  private String orderFieldNextType;
  private String second_branch;
  private int sysId;
  private int joneAppLevel;// 应用级别

  public String getAppDevelopGroupName()
  {
    return this.appDevelopGroupName;
  }
  public void setAppDevelopGroupName(String appDevelopGroupName) {
    this.appDevelopGroupName = appDevelopGroupName;
  }
  public String getBusDomain() {
    return this.busDomain;
  }
  public void setBusDomain(String busDomain) {
    this.busDomain = busDomain;
  }
  public String getFirst_branch() {
    return this.first_branch;
  }
  public void setFirst_branch(String first_branch) {
    this.first_branch = first_branch;
  }
  public String getJoneAppType() {
    return this.joneAppType;
  }
  public void setJoneAppType(String joneAppType) {
    this.joneAppType = joneAppType;
  }
  public String getLeader() {
    return this.leader;
  }
  public void setLeader(String leader) {
    this.leader = leader;
  }
  public String getLeader_name() {
    return this.leader_name;
  }
  public void setLeader_name(String leader_name) {
    this.leader_name = leader_name;
  }
  public int getLevel() {
    return this.level;
  }
  public void setLevel(int level) {
    this.level = level;
  }
  public String getLevel_name() {
    return this.level_name;
  }
  public void setLevel_name(String level_name) {
    this.level_name = level_name;
  }
  public String getMainBusDomain() {
    return this.mainBusDomain;
  }
  public void setMainBusDomain(String mainBusDomain) {
    this.mainBusDomain = mainBusDomain;
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
  public String getSecond_branch() {
    return this.second_branch;
  }
  public void setSecond_branch(String second_branch) {
    this.second_branch = second_branch;
  }
  public int getSysId() {
    return this.sysId;
  }
  public void setSysId(int sysId) {
    this.sysId = sysId;
  }

  public int getJoneAppLevel() {
    return joneAppLevel;
  }

  public void setJoneAppLevel(int joneAppLevel) {
    this.joneAppLevel = joneAppLevel;
  }
}