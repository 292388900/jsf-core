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

public abstract class AbstractWorker implements Worker {

    protected String cronExpression;

    protected JSONObject workerParameters;

    protected boolean immediate;

    protected String erps;

    protected boolean errorAlert;

    /* worker 的状态表示 0 代表启动 1 代表停止*/
    protected String active;

    protected String workerType;


    @Override
    public String cronExpression() {
        return cronExpression;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public JSONObject getWorkerParameters() {
        return workerParameters;
    }

    public void setWorkerParameters(JSONObject workerParameters) {
        this.workerParameters = workerParameters;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }


    public String getErps() {
        return erps;
    }

    public void setErps(String erps) {
        this.erps = erps;
    }

    public boolean isErrorAlert() {
        return errorAlert;
    }

    public void setErrorAlert(boolean errorAlert) {
        this.errorAlert = errorAlert;
    }


    public void setStatus(String active){
        this.active = active;
    }

    @Override
    public String status() {
        return this.active;
    }

    @Override
    public void destroy() {

    }

    @Override
    public void init() {

    }

    @Override
    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

}
