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

import java.util.List;

import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.Server;

public interface ScanServerManager {

    /**
     * 更新节点为死亡状态
     * @param serverList
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(List<Server> serverList) throws Exception;

    /**
     * 批量逻辑删除节点
     * @param serverList
     * @return
     * @throws Exception
     */
    public int tagServerToDel(List<Server> serverList) throws Exception;

    /**
     * 批量物理删除节点
     * @param serverList
     * @return
     * @throws Exception
     */
    public int deleteServerByUniqkey(List<Server> serverList) throws Exception;

    /**
     * 批量物理删除节点
     * @param serverList
     * @return
     * @throws Exception
     */
    public int deleteServerById(List<Server> serverList) throws Exception;

    /**
     * 获取状态为存活的节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Server> getOnlineServersByIns(String insKey) throws Exception;
    
    /**
     * 获取无实例server列表
     * @param optTime
     * @return
     * @throws Exception
     */
    public List<Server> getNoInsServers(long optTime) throws Exception;

    /**
     * 根据实例获取server列表
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<Server> getServersByInsKeyList(List<String> insKeyList) throws Exception;

    /**
     * 复活已经逻辑删除的provider
     * @param insList
     * @return
     * @throws Exception
     */
    public int updateServerToRevival(List<JsfIns> insList) throws Exception;

    /**
     * 记录逻辑删除日志
     * @param serverList
     * @return
     * @throws Exception
     */
    public int recordLog_Tagdel(List<Server> serverList, String logCreator) throws Exception;

}
