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

import java.util.concurrent.atomic.AtomicLong;

public class CallbackRecoder {
    //回调数
    private static Recorder callbackRecorder = new Recorder();
    //回调失败总数
    private static AtomicLong callbackFailTotalCount = new AtomicLong(0);
    //计算时间，返回给管理端. 间隔是两分钟
    private static long recordTime = 0L;

    /**
     * 获取回调总数
     * @return
     */
    public static long getCallbackTotalCount() {
        return callbackRecorder.getTotalCount();
    }

    /**
     * 获取上一个时间段的回调数
     * @return
     */
    public static long getCallbackCount() {
        return callbackRecorder.getLastCount();
    }

    /**
     * 记录回调数
     * @return
     */
    public static void increaseCallbackCount() {
        callbackRecorder.increment();
    }

    /**
     * 计算上一个时间段的回调数
     */
    public static void calCallbackCount() {
        callbackRecorder.calCount();
    }

    /**
     * 获取回调失败总数
     * @return
     */
    public static long getCallbackFailTotalCount() {
        return callbackFailTotalCount.get();
    }

    /**
     * 记录回调失败总数
     * @return
     */
    public static long increaseCallbackFailTotalCount() {
        return callbackFailTotalCount.incrementAndGet();
    }

    /**
     * 记录统计的时间戳
     */
    public static void recordTime() {
        recordTime = System.currentTimeMillis() / 1000 * 1000;
    }

    /**
     * 获取统计的时间戳
     * @return
     */
    public static long getRecordTime() {
        return recordTime;
    }
}
