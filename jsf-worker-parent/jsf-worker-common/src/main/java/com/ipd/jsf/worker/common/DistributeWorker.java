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
package com.ipd.jsf.worker.common;

/**
 * 分布式的worker
 * <br/>
 * 具体防止worker数据的重复执行，有worker的实现根据传入的serverNum以及currentServerIndex做具体的数据分片处理
 *
 */
public abstract class DistributeWorker extends AbstractWorker {


    /**
     *
     * @param serverNum 处理当前workType的总的结点数目
     * @param currentServerIndex 当前结点在所有结点当中的序号
     * @return
     */
    public abstract boolean run(int serverNum,int currentServerIndex);

    @Override
    public boolean run() {
        return false;
    }
}
