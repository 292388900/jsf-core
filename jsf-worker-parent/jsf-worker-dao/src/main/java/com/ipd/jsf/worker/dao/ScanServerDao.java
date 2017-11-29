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

import com.ipd.jsf.worker.domain.Server;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanServerDao {

    /**
     * 更新节点为死亡状态
     * @param serverList
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(@Param("serverList") List<Server> serverList, @Param("status") int status) throws Exception;

    /**
     * 逻辑删除
     * @param uniqKeyList
     * @param delTime
     * @return
     * @throws Exception
     */
    public int batchTagServerToDel(@Param("uniqKeyList") List<String> uniqKeyList, @Param("delTime") long delTime, @Param("status") int status) throws Exception;

    /**
     * 逻辑删除
     * @param serverIds
     * @param delTime
     * @param status
     * @return
     * @throws Exception
     */
    public int batchTagServerToDelByIds(@Param("serverIds") List<Integer> serverIds, @Param("delTime") long delTime, @Param("status") int status) throws Exception;

    /**
     * 根据uniqkeyList列表，删除server
     * @param uniqkeyList
     * @return
     * @throws Exception
     */
    public int deleteServerByUniqkey(@Param("uniqkeyList") List<String> uniqkeyList) throws Exception;

    /**
     * 根据idList列表，删除server
     * @param idList
     * @return
     * @throws Exception
     */
    public int deleteServerById(@Param("idList") List<Integer> idList) throws Exception;

    /**
     * 根据insKey，删除server
     * @param insKey
     * @return
     * @throws Exception
     */
    public int deleteByInsKey(@Param("insKey") String insKey) throws Exception;

    /**
     * 根据实例key查找节点
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<Server> getServersByInsKeyList(@Param("insKeyList") List<String> insKeyList) throws Exception;

    /**
     * 获取状态为存活的节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Server> getOnlineServersByIns(@Param("insKey") String insKey) throws Exception;

    /**
     * 获取无实例server列表
     * @param time
     * @return
     * @throws Exception
     */
    public List<Server> getNoInsServers(@Param("time") long time) throws Exception;

    /**
     * 获取无实例server列表
     * @param time
     * @return
     * @throws Exception
     */
    public List<Server> getNoInsServersByRooms(@Param("time") long time, @Param("rooms") List<Integer> rooms) throws Exception;

    /**
     * 将心跳正常但被逻辑删除的provider改为上线状态
     * @param serverList
     * @param status
     * @return
     * @throws Exception
     */
    public int updateServerToRevival(@Param("serverList") List<Server> serverList, @Param("status") int status) throws Exception;
}
