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
package com.ipd.jsf.worker.common.domain;

public class WorkerInfo {

    private String workerName;

    private String workerType;

    private String workerDesc;

    private String cronExpression;

    private String workerParameters;

    private boolean immediate;

    private boolean errorAlert;

    private String workerManager;

    private String active;

    public String getWorkerName() {
        return workerName;
    }

    public void setWorkerName(String workerName) {
        this.workerName = workerName;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public String getWorkerDesc() {
        return workerDesc;
    }

    public void setWorkerDesc(String workerDesc) {
        this.workerDesc = workerDesc;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getWorkerParameters() {
        return workerParameters;
    }

    public void setWorkerParameters(String workerParameters) {
        this.workerParameters = workerParameters;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public boolean isErrorAlert() {
        return errorAlert;
    }

    public void setErrorAlert(boolean errorAlert) {
        this.errorAlert = errorAlert;
    }

    public String getWorkerManager() {
        return workerManager;
    }

    public void setWorkerManager(String workerManager) {
        this.workerManager = workerManager;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }
}
