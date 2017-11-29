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
package com.ipd.jsf.worker.common.demoworker;

import com.ipd.jsf.worker.common.event.WorkerServerEvent;
import com.ipd.jsf.worker.common.EventWorker;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.common.ZKClientAware;
import com.ipd.jsf.worker.common.event.WorkerListener;
import com.ipd.jsf.zookeeper.ZkClient;

public class LoadBalanceWorker extends  SingleWorker  implements EventWorker,ZKClientAware,WorkerListener<WorkerServerEvent> {

    private ZkClient zkClient;

    private volatile boolean active = false;

    @Override
    public boolean activeEvent() {
        return active;
    }



    @Override
    public void offActive() {
        this.active = false;
    }

    @Override
    public boolean run()  {
        System.out.println("--------=========================-----------loadbalance worker----============================-----");
        return true;
    }

    @Override
    public String getWorkerType() {
        return "loadBalance";
    }


    @Override
    public void setZKClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public void onWorkerEvent(WorkerServerEvent event) {
        active = true;
        event.getScheduleManager().refresh();
    }

}
