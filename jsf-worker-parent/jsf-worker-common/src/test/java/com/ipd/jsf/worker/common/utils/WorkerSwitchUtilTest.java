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
package com.ipd.jsf.worker.common.utils;

import com.ipd.jsf.worker.common.ScheduleServerInfo;

import org.junit.Test;

public class WorkerSwitchUtilTest {


    @Test
    public void testGetWorkerMasterServerInfo() throws Exception {
        ScheduleServerInfo serverInfo = WorkerSwitchUtil.getWorkerMasterServerInfo("demoWorker",new WorkerSwitchUtil.MasterSwitchCallback() {
            @Override
            public void execute(ScheduleServerInfo newestServerInfo) {
                System.out.println("-----------new master-------"+newestServerInfo.getWorkerType()+"-----id--"+newestServerInfo.getId()+newestServerInfo.getWorkerParameters());

            }
        });
        System.out.println("-----------current master server-------"+serverInfo.getWorkerType()+"-----id--"+serverInfo.getId());
        Thread.sleep(100000);
    }
}
