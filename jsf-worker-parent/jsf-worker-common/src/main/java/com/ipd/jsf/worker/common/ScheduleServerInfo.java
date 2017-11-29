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
package com.ipd.jsf.worker.common;

import com.alibaba.fastjson.JSONObject;

/**
 * ScheduleServer 相关信息
 *
 */
public class ScheduleServerInfo {

    /**
     * 当前workerType下总的结点数目
     */
    private int serverNum;

    /**
     * 当前结点在所有（指定workerType）下结点序号
     */
    private int index;

    private String workerType;

    private boolean isMaster;

    private String id;

    private String ip;

    private JSONObject workerParameters;

    public ScheduleServerInfo(int serverNum, int index, String workerType) {
        this.serverNum = serverNum;
        this.index = index;
        this.workerType = workerType;
    }

    public ScheduleServerInfo() {
    }

    public int getServerNum() {
        return serverNum;
    }

    public void setServerNum(int serverNum) {
        this.serverNum = serverNum;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public JSONObject getWorkerParameters() {
        return workerParameters;
    }

    public void setWorkerParameters(JSONObject workerParameters) {
        this.workerParameters = workerParameters;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}
