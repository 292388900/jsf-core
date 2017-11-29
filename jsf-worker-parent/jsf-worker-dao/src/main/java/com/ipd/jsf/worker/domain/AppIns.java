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

import java.util.Date;

public class AppIns {
    //序号
    private int JsfAppInsId;

    //应用表的id
    private int jsfAppId;

    //注册中心的实例key
    private String insKey;
    
    //app端的ip
    private String ip;

    //app的id号
    private int appId;

    //app的实例id号
    private String appInsId;

    //创建时间
    private Date createTime;

    //创建者
    private String creator;

    //修改时间
    private Date updateTime;

    //修改者
    private String modifier;


    /**
     * @return the jsfAppInsId
     */
    public int getJsfAppInsId() {
        return JsfAppInsId;
    }

    /**
     * @param jsfAppInsId the jsfAppInsId to set
     */
    public void setJsfAppInsId(int jsfAppInsId) {
        JsfAppInsId = jsfAppInsId;
    }

    /**
     * @return the jsfAppId
     */
    public int getJsfAppId() {
        return jsfAppId;
    }

    /**
     * @param jsfAppId the jsfAppId to set
     */
    public void setJsfAppId(int jsfAppId) {
        this.jsfAppId = jsfAppId;
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
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the updateTime
     */
    public Date getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return the modifier
     */
    public String getModifier() {
        return modifier;
    }

    /**
     * @param modifier the modifier to set
     */
    public void setModifier(String modifier) {
        this.modifier = modifier;
    }
}
