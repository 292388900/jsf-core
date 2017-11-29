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
package com.ipd.jsf.worker.manager;

import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.worker.domain.ServerAlias;
import com.ipd.jsf.worker.domain.UserAction;

import java.util.List;

public interface ServerAliasManager {

    /**
     *
     * @param serverAliases
     * @throws Exception
     */
    public void batchInsert(List<ServerAlias> serverAliases) throws Exception;

    /**
     *
     * @param ids
     * @throws Exception
     */
    public void batchCancel(List<Integer> ids) throws Exception;

    /**
     *
     * @param serverAlias
     * @return
     */
    public List<ServerAlias> exists(ServerAlias serverAlias);

    /**
     *
     * @param newServerAliasList
     * @param cancelServerAliasList
     * @param updateServerVersionList
     * @param action
     * @throws Exception
     */
    public void dynamicGrouping(List<ServerAlias> newServerAliasList,
                                List<Integer> cancelServerAliasList,
                                List<IfaceServer> updateServerVersionList,
                                UserAction action) throws Exception;

}