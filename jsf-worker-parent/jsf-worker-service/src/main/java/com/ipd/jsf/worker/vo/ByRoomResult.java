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
package com.ipd.jsf.worker.vo;

public class ByRoomResult {

    private boolean byRoom;

    /** 如果byRoom=fasle,需指定哪个worker执行任务 */
    private String workerAlias;

    private boolean matchWorkerType;

    public ByRoomResult(boolean byRoom, String workerAlias, boolean matchWorkerType) {
        this.byRoom = byRoom;
        this.workerAlias = workerAlias;
        this.matchWorkerType = matchWorkerType;
    }

    public boolean isByRoom() {
        return byRoom;
    }

    public void setByRoom(boolean byRoom) {
        this.byRoom = byRoom;
    }

    public String getWorkerAlias() {
        return workerAlias;
    }

    public void setWorkerAlias(String workerAlias) {
        this.workerAlias = workerAlias;
    }

    public boolean isMatchWorkerType() {
        return matchWorkerType;
    }

    public void setMatchWorkerType(boolean matchWorkerType) {
        this.matchWorkerType = matchWorkerType;
    }

}