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
package com.ipd.jsf.worker.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.JsfIns;

@Repository
public interface ScanInsDao {
    /**
     * 将状态为上线的实例更新实例为死亡状态
     * @param insKey
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(@Param("insKey") String insKey, @Param("status") int status) throws Exception;

    /**
     * 更新实例删除标识，逻辑删除
     * @param insKey
     * @param delTime
     * @return
     * @throws Exception
     */
    public int tagInsToDel(@Param("insKey") String insKey, @Param("delTime") long delTime, @Param("status") int status) throws Exception;

    /**
     * 删除实例
     * @param insKey
     * @return
     * @throws Exception
     */
    public int deleteByInsKey(@Param("insKey") String insKey) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTime(@Param("time") Date time, @Param("registryList") List<String> registryList) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTimeByRooms(@Param("time") Date time, @Param("registryList") List<String> registryList, @Param("rooms") List<Integer> rooms) throws Exception;

    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTime(@Param("time") Date time, @Param("registryList") List<String> registryList) throws Exception;

    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTimeByRooms(@Param("time") Date time, @Param("registryList") List<String> registryList, @Param("rooms") List<Integer> rooms) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除反注册的实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelUnregInsBeforeTime(@Param("time") Date time) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除反注册的实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelUnregInsBeforeTimeByRooms(@Param("time") Date time, @Param("rooms") List<Integer> rooms) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelInsBeforeTime(@Param("time") Date time) throws Exception;

    /**
     * 获取time时间以前且标识为逻辑删除实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelInsBeforeTimeByRooms(@Param("time") Date time, @Param("rooms") List<Integer> rooms) throws Exception;

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
    public List<JsfIns> getRevivalInsListByServerAndRooms(@Param("rooms") List<Integer> rooms) throws Exception;

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
    public List<JsfIns> getRevivalInsListByClientAndRooms(@Param("rooms") List<Integer> rooms) throws Exception;

}
