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
import java.util.Map;

import com.ipd.jsf.worker.domain.Instance;
import com.ipd.jsf.worker.domain.JsfApp;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.LogicDelAlarmInfo;

public interface InstanceManager {
    /**
     * 更新实例为上线状态
     * @param list
     * @return
     * @throws Exception
     */
    public int batchUpdateStatusOnline(List<JsfIns> list) throws Exception;

    /**
     * 更新实例为死亡状态
     * @param clientId
     * @return
     * @throws Exception
     */
    public int batchUpdateStatusOffline(List<JsfIns> list) throws Exception;

    /**
     * 批量删除节点
     * @param insList
     * @return
     * @throws Exception
     */
    public int batchUpdateDelYn(List<JsfIns> insList) throws Exception;
    
    /**
     * 批量删除节点
     * @param insList
     * @return
     * @throws Exception
     */
    public int batchDeleteByInsKey(List<JsfIns> insList) throws Exception;

    /**
     * 获取状态为存活，但是心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOnlineInsBeforeTime(Date time, List<String> registryList) throws Exception;
    
    /**
     * 获取状态为死亡的节点，但是心跳时间大于time的节点
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsAfterTime(Date time) throws Exception;
    
    /**
     * 获取状态为死亡的节点，心跳时间小于time的节点
     * @param time
     * @param registryList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getOfflineInsBeforeTime(Date time, List<String> registryList) throws Exception;

    /**
     * 获取time时间以前且标识为删除的实例, 每次只取500条
     * @param time
     * @return
     * @throws Exception
     */
    public List<JsfIns> getDelYnInsBeforeTime(Date time) throws Exception;

    /**
     * 获取实例信息
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<JsfIns> getInsListByKey(List<String> insKeyList) throws Exception;

    /**
     * 获取实例与接口列表
     * @return
     * @throws Exception
     */
    public Map<String, Instance> getInstanceInterfaceMap() throws Exception;
    
    JsfIns getJsfInsByInskey(String insKey);

	/**
	 * @param insKey
	 * @return
	 */
	public Integer getAppIdByInsKey(String insKey);

	/**
	 * 
	 * @param insKey
	 * @return
	 */
	public JsfApp getAppByInsKey(String insKey);

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

}
