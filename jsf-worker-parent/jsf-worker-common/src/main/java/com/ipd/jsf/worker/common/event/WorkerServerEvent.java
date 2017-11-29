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
package com.ipd.jsf.worker.common.event;

import com.ipd.jsf.worker.common.ScheduleManager;
import com.ipd.jsf.worker.common.WorkerManager;

public class WorkerServerEvent extends WorkerEvent {


    protected ScheduleManager scheduleManager;

    public WorkerServerEvent(WorkerManager workerManager, ScheduleManager scheduleManager) {
        super(workerManager);
        this.scheduleManager = scheduleManager;
    }

    public ScheduleManager getScheduleManager() {
        return scheduleManager;
    }
}
