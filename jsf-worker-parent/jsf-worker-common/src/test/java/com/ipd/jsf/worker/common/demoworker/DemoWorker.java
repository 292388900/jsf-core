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
import com.ipd.jsf.worker.common.ServerInfoAware;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.common.ScheduleServerInfo;

import java.util.Date;

public class DemoWorker extends SingleWorker implements ServerInfoAware {

    private volatile ScheduleServerInfo runningServerInfo;




    @Override
    public boolean run()  {
        if ( runningServerInfo == null ){
            System.out.println(getWorkerType()+"----"+new Date()+"----------master worker----running alone------");
        }
        if ( runningServerInfo.isMaster() ){
            System.out.println(getWorkerType()+"----"+new Date()+"----------master worker----running on master------");
        } else {
            System.out.println(getWorkerType()+"----"+new Date()+"----------master worker----running on slave------");
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
        }
        return true;
    }


    @Override
    public void setRunningServerInfo(ScheduleServerInfo serverInfo) {
        runningServerInfo = serverInfo;
    }

    @Override
    public ScheduleServerInfo getRunningServerInfo() {
        return runningServerInfo;
    }



}
