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

import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SimpleWorkerEventMulticaster extends AbstractWorkerEventMulticaster {


    private  static final Executor executor = Executors.newFixedThreadPool(Integer.valueOf(5), new WorkerThreadFactory("simpleWorker"));

    @Override
    public void multicastEvent(final WorkerEvent event) {
        for (final WorkerListener listener : getCacheListeners(event)) {
            executor.execute(new Runnable() {
                public void run() {
                    listener.onWorkerEvent(event);
                }
            });
        }
    }
}
