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
package com.ipd.jsf.registry.recoder;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Recorder {
    //总数
    private AtomicLong totalCount = new AtomicLong(0);
    //单位时间的数量
    private AtomicInteger count = new AtomicInteger(0);
    //上一个时间单位内，统计值
    private int lastCount = 0;

    /**
     * 获取总数值
     * @return
     */
    public long getTotalCount() {
        return totalCount.get();
    }

    /**
     * 获取上一次时间的统计值
     * @return
     */
    public int getLastCount() {
        return lastCount;
    }

    /**
     * 计数
     */
    public void increment() {
        totalCount.incrementAndGet();
        count.incrementAndGet();
    }

    /**
     * 计算上一次时间段的计数，并清零
     */
    public void calCount() {
        lastCount = count.get();
        if (lastCount > 0) {
            count.getAndAdd(-lastCount);
        }
    }

    /**
     * 获取当前值
     * @return
     */
    public int getCount() {
        return count.get();
    }
}
