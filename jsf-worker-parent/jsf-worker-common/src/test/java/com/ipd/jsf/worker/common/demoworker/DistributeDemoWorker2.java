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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class DistributeDemoWorker2 extends DistributeWorker {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean run(int serverNum,int currentServerIndex) {
        logger.info(new Date()+" "+Thread.currentThread().getName()+"---------------com.ipd.saf.worker.common.worker.DistributeDemoWorker2-----running ----------------"+serverNum +"----------"+currentServerIndex);
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public String getWorkerType() {
        return "distributeWorkerDemo2";
    }


}
