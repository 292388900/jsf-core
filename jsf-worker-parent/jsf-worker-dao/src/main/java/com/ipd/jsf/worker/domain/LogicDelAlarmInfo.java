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

public class LogicDelAlarmInfo implements Serializable {

    /**
     * 实例所属机房
     */
    private Integer insRoom;

    /**
     * 实例所属应用
     */
    private String appId;

    /**
     * 实例所属应用名
     */
    private String appName;

    /**
     * 应用即将被逻辑删除的实例数
     */
    private Long count;

    public LogicDelAlarmInfo() {
    }

    public Integer getInsRoom() {
        return insRoom;
    }

    public void setInsRoom(Integer insRoom) {
        this.insRoom = insRoom;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

}