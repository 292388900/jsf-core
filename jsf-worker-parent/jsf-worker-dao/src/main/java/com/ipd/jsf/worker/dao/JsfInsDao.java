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
import java.util.Map;

import com.ipd.jsf.worker.domain.InstanceInterface;
import com.ipd.jsf.worker.domain.JsfApp;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.LogicDelAlarmInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JsfInsDao {
    /**
     * 将状态为死亡的实例更新为上线状态
     * @param clientId
     * @return
     * @throws Exception
     */
    public int batchUpdateStatusOnline(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 将状态为上线的实例更新实例为死亡状态
     * @param clientId
     * @return
     * @throws Exception
     */
    public int batchUpdateStatusOffline(@Param("list") List<JsfIns> list) throws Exception;

    /**
     * 批量更新实例删除标识，逻辑删除
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public int batchUpdateDelYn(@Param("insKeyList") List<String> insKeyList) throws Exception;
    
    /**
     * 批量删除实例
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public int batchDeleteByInsKey(@Param("insKeyList") List<String> insKeyList) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTime(@Param("time") Date time, @Param("registryList") List<String> registryList) throws Exception;

    /**
     * 获取状态为死亡的节点，但是心跳时间大于time的节点
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsAfterTime(@Param("time") Date time) throws Exception;

    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTime(@Param("time") Date time, @Param("registryList") List<String> registryList) throws Exception;

    /**
     * 获取time时间以前且标识为删除的实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelYnInsBeforeTime(@Param("time") Date time) throws Exception;

    /**
     * 根据实例key获取实例信息
     * @param ip
     * @param pid
     * @return
     * @throws Exception
     */
    public List<JsfIns> getInsListByKey(@Param("list") List<String> insKeyList) throws Exception;

    /**
     * 获取实例对应的接口
     * @return
     * @throws Exception
     */
    public List<InstanceInterface> getInsIfaceList(@Param("start") int start, @Param("limit") int limit) throws Exception;
    
    /**
     * 获取实例对应的接口
     * @return
     * @throws Exception
     */
    public int getInsIfaceListCount() throws Exception;

	/**
	 * @param insKey
	 * @return
	 */
	public JsfIns getJsfInsByInsKey(String insKey);

	/**
	 * @param insKey
	 * @return
	 */
	public Integer getAppIdByInsKey(String insKey);
	
	/**
	 * @param insKey
	 * @return
	 */
	public JsfApp getAppByInsKey(String insKey);

	/**
	 * @param appId
	 * @param appInsId
	 * @param pid
	 * @return
	 */
	public List<String> getInsKeys(@Param("appId")Integer appId, @Param("appInsId")String appInsId, @Param("pid")int pid);

    /**
     * 按照机房 应用分组汇总8小时前标记为逻辑删除的实例数
     * @param params
     * @return
     */
    public List<LogicDelAlarmInfo> countLogicDelInsNodes(Map<String, Object> params);

    /**
     * 按照机房汇总8小时前实例数
     * @param params
     * @return
     */
    public List<LogicDelAlarmInfo> countInsNodes(Map<String, Object> params);


    int updInsAppIdAndAppInsId(@Param("insKey")String insKey, @Param("appId")Integer appId, @Param("appInsId")String appInsId);

}
