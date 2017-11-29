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

import com.alibaba.fastjson.JSONObject;

public interface Worker {


    /**
     * worker 具体业务逻辑操作执行
     *
     * worker common 会根据返回的值去做一些事情，比如报警等
     *
     * 因此需要正确的返回运行结果
     *
     * @return true 如果执行成功，否则返回false
     */
    boolean run();


    /**
     * worker type与worker一一对应
     *
     * worker type （用在结点路径当中）
     * @return
     */
    String getWorkerType();

    /**
     * worker执行时间的表达式
     *
     * @return
     */
    String cronExpression();

    /**
     *
     * @return 返回worker相关JSON格式参数(worker业务相关参数),如果没有可以返回null
     */
    JSONObject getWorkerParameters();


    /**
     *
     * @return 到达执行立即执行而不受上次worker是否有没有执行完没关系,返回true，否则返回false
     */
    boolean isImmediate();

    /**
     *
     * @return worker的状态
     */
    String status();

    /**
     * 提供给worker具体实现类，销毁资源的方法
     */
    void destroy();


    /**
     * 提供给worker具体实现类，初始化的方法
     *
     * worker执行前的准备工作可以在此方法中完成
     *
     */
    void init();
}
