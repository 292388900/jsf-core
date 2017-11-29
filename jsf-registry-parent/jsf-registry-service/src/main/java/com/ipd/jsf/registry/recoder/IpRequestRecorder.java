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


/**
 * 统计IPtopN
 */
public class IpRequestRecorder {

    //心跳次数
    public Recorder hbCount = new Recorder();
    //注册次数
    public Recorder registryCount = new Recorder();
    //订阅次数
    public Recorder subscribeCount = new Recorder();
    //最后一次计数时间
    public long lastUpdateTime = System.currentTimeMillis();

    /**
     * 计算上个统计周期的数值
     */
    public void calCount() {
        hbCount.calCount();
        registryCount.calCount();
        subscribeCount.calCount();
    }
}
