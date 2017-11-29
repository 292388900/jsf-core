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
package com.ipd.jsf.worker.thread.onlyone;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.service.UserResourceService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserResourceCheckWorker extends SingleWorker {

    @Autowired
    private UserResourceService userResourceService;

    /**
     * worker 具体业务逻辑操作执行
     * <p/>
     * worker common 会根据返回的值去做一些事情，比如报警等
     * <p/>
     * 因此需要正确的返回运行结果
     *
     * @return true 如果执行成功，否则返回false
     */
    @Override
    public boolean run() {
        userResourceService.check();
        return true;
    }

    /**
     * worker type与worker一一对应
     * <p/>
     * worker type （用在结点路径当中）
     *
     * @return
     */
    @Override
    public String getWorkerType() {
        return "userResourceCheckWorker";
    }

}