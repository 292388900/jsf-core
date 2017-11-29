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

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.JsfIns;

public interface ScanInsManager {
    /**
     * 更新实例为死亡状态
     * @param insKey
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(String insKey) throws Exception;

    /**
     * 逻辑删除节点
     * @param ins
     * @return
     * @throws Exception
     */
    public int tagInsToDel(JsfIns ins) throws Exception;
    
    /**
     * 删除节点
     * @return
     * @throws Exception
     */
    public int deleteByInsKey(JsfIns ins) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTime(Date time, List<String> registryList) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTimeByRooms(Date time, List<String> registryList, List<Integer> rooms) throws Exception;

    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTime(Date time, List<String> registryList) throws Exception;

    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTimeByRooms(Date time, List<String> registryList, List<Integer> rooms) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除的实例, 每次只取100条
     * @param time
     * @param type
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelInsBeforeTime(Date time, int type) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除的实例, 每次只取100条
     * @param time
     * @param type
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelInsBeforeTimeByRooms(Date time, int type, List<Integer> rooms) throws Exception;

    /**
     * 获取心跳正常，但是provider被逻辑删除的实例
     * @return
     * @throws Exception
     */
    public List<JsfIns> getRevivalInsListByServer() throws Exception;

    /**
     * 获取心跳正常，但是provider被逻辑删除的实例
     * @return
     * @throws Exception
     */
    public List<JsfIns> getRevivalInsListByServerAndRooms(List<Integer> rooms) throws Exception;

    /**
     * 获取心跳正常，但是consumer被逻辑删除的实例
     * @return
     * @throws Exception
     */
    public List<JsfIns> getRevivalInsListByClient() throws Exception;

    /**
     * 获取心跳正常，但是consumer被逻辑删除的实例
     * @return
     * @throws Exception
     */
    public List<JsfIns> getRevivalInsListByClientAndRooms(List<Integer> rooms) throws Exception;

}
