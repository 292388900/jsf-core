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
package com.ipd.jsf.registry.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class WorkerThreadPool {
	private int initQueueSize = 100000;
    private BlockingQueue<Runnable> queue = null;
    private ThreadPoolExecutor threadPool = null;

    public WorkerThreadPool(int corePoolSize, int maximumPoolSize, String threadName) {
        createThreadPool(corePoolSize, maximumPoolSize, threadName, this.initQueueSize);
    }

    public WorkerThreadPool(int corePoolSize, int maximumPoolSize, String threadName, int queueSize) {
    	createThreadPool(corePoolSize, maximumPoolSize, threadName, queueSize);
    }

    /**
     * 创建线程池
     */
    private void createThreadPool(int corePoolSize, int maximumPoolSize, String threadName, int queueSize) {
    	queue = new LinkedBlockingQueue<Runnable>(queueSize);
        threadPool = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                30L, TimeUnit.SECONDS,
                queue, new NamedThreadFactory(threadName));
    }

    public void execute(Runnable task) {
        threadPool.execute(task);
    }
    
    public Future<Boolean> submit(Callable<Boolean> task) {
        return threadPool.submit(task);
    }

    public int queueSize() {
        return queue.size();
    }

    public int queueRemainingCapacity() {
        return queue.remainingCapacity();
    }

    public void shutdown() {
        threadPool.shutdown();
    }
}
