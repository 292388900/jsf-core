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

import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.JsfIns;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ScanClientDao {

    /**
     * 更新节点为死亡状态
     * @param idList
     * @param status
     * @return
     * @throws Exception
     */
    public int updateStatusOffline(@Param("idList") List<Integer> idList, @Param("status") int status) throws Exception;

    /**
     * 逻辑删除
     * @param idList
     * @param delTime
     * @return
     * @throws Exception
     */
    public int tagClientToDel(@Param("idList") List<Integer> idList, @Param("delTime") long delTime, @Param("status") int status) throws Exception;

    /**
     * 根据uniqkey删除节点
     * @param uniqkeyList
     * @return
     * @throws Exception
     */
    public int deleteByUniqkey(@Param("uniqkeyList") List<String> uniqkeyList) throws Exception;

    /**
     * 根据id删除节点
     * @param idList
     * @return
     * @throws Exception
     */
    public int deleteById(@Param("idList") List<Integer> idList) throws Exception;

    /**
     * 根据insKey删除节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public int deleteByInsKey(@Param("insKey") String insKey) throws Exception;

    /**
     * 获取状态为存活的节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Client> getOnlineClientsByIns(@Param("insKey") String insKey) throws Exception;

    /**
     * 获取状态为死亡的节点
     * @param list
     * @return
     * @throws Exception
     */
    public List<Client> getOfflineClientsByIns(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 获取节点
     * @param insKey
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByIns(@Param("insKey") String insKey) throws Exception;

    /**
     * 根据inskeylist获取client
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<Client> getClientsByInsKeyList(@Param("insKeyList") List<String> insKeyList) throws Exception;

    /**
     * 获取无实例client列表，并获取小于date时间的数据
     * @return
     * @throws Exception
     */
    public List<Client> getNoInsClients(@Param("time") long time) throws Exception;

    /**
     * 对心跳正常的但已经逻辑删除的consumer做恢复操作
     *
     * @param clientList
     * @param status
     * @return
     */
    public int updateClientToRevival(@Param("clientList") List<Client> clientList, @Param("status") int status);

}