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

import com.ipd.jsf.worker.common.ScheduleServer;

import junit.framework.Assert;
import org.junit.Test;

import java.util.List;

public class WorkerScheduleManagerUtilTest {


    @Test
    public void testStartWorker() throws Exception {
        WorkerScheduleManagerUtil.startWorker("distributeWorkerDemo");
    }

    @Test
    public void testStopWorker() throws Exception {
        WorkerScheduleManagerUtil.stopWorker("distributeWorkerDemo");
    }

    @Test
    public void testStartWorkerForInputServer() throws Exception {
        List<ScheduleServer> servers = WorkerScheduleManagerUtil.listWorkerScheduleServers("distributeWorkerDemo");
        if ( servers != null ){
//            WorkerScheduleManagerUtil.startWorker("distributeWorkerDemo",servers.get(0).getId());
            WorkerScheduleManagerUtil.startWorker("distributeWorkerDemo","8344$127.0.0.1$7456A81923CA46D3A3D10BFF38452FFF");
        }
    }

    @Test
    public void testStopWorkerForInputServer() throws Exception {
        List<ScheduleServer> servers = WorkerScheduleManagerUtil.listWorkerScheduleServers("distributeWorkerDemo");
        if ( servers != null ){
            WorkerScheduleManagerUtil.stopWorker("distributeWorkerDemo","8344$127.0.0.1$7456A81923CA46D3A3D10BFF38452FFF");
//            WorkerScheduleManagerUtil.stopWorker("distributeWorkerDemo",servers.get(0).getId());
        }
    }

    @Test
    public void testIsRunningForFalse() throws Exception {
        WorkerScheduleManagerUtil.stopWorker("demoWorker");
        Assert.assertEquals(false,WorkerScheduleManagerUtil.isRunning("demoWorker","single"));
    }

    @Test
    public void testIsRunningForTrue() throws Exception {
        WorkerScheduleManagerUtil.startWorker("demoWorker");
        Assert.assertEquals(true,WorkerScheduleManagerUtil.isRunning("demoWorker","single"));
    }

    @Test
    public void testListWorkerScheduleServers() throws Exception {
        for (ScheduleServer server : WorkerScheduleManagerUtil.listWorkerScheduleServers("distributeWorkerDemo")){
            System.out.println(server.getId());
        }
    }
}
