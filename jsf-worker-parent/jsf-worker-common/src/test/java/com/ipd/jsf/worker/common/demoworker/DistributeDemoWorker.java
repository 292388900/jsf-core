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

import com.ipd.jsf.worker.common.DistributeWorker;
import com.ipd.jsf.worker.common.ServerInfoAware;
import com.ipd.jsf.worker.common.ScheduleServerInfo;

import java.util.Date;

public class DistributeDemoWorker extends DistributeWorker implements ServerInfoAware {

    private volatile ScheduleServerInfo runningServerInfo;

    @Override
    public boolean run(int serverNum,int currentServerIndex)  {
        if ( runningServerInfo == null ){
            System.out.println(new Date()+"----------com.ipd.saf.worker.common.worker.DistributeDemoWorker----running alone------");
        }
        if ( runningServerInfo.isMaster() ){
            System.out.println(new Date()+"---------------com.ipd.saf.worker.common.worker.DistributeDemoWorker-----running on master------------------"+serverNum +"----------"+currentServerIndex);
        } else {
            System.out.println(new Date()+"---------------com.ipd.saf.worker.common.worker.DistributeDemoWorker-----running on slave------------------"+serverNum +"----------"+currentServerIndex);
        }
//        Thread.sleep(2000);
        return true;
    }

    @Override
    public String getWorkerType() {
        return "distributeWorkerDemo";
    }


    @Override
    public void setRunningServerInfo(ScheduleServerInfo serverInfo) {
        this.runningServerInfo = serverInfo;
    }

    @Override
    public ScheduleServerInfo getRunningServerInfo() {
        return runningServerInfo;
    }

}
