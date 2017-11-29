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
package com.ipd.jsf.registry.manager;

import java.util.List;
import java.util.Map;

import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.Server;

public interface ServerManager {

    /**
     * 注册provider
     * @param serverList
     * @param updateFlagList
     * @throws Exception
     */
    public void registerServer(List<Server> serverList, List<Integer> updateFlagList) throws Exception;

    /**
     * 取消注册provider
     * @param serverList
     * @return
     * @throws Exception
     */
    public boolean unRegisterServer(List<Server> serverList) throws Exception;

    /**
     * 根据接口id+alias，获取provider列表
     * @param ifaceIdAliasList
     * @return
     * @throws Exception
     */
    public Map<Integer, List<Server>> getServersByIfaceIdAliasList(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception;
}
