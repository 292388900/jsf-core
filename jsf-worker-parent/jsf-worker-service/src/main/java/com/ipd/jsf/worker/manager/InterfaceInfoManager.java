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

import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.UserResource;

public interface InterfaceInfoManager {
    /**
     * 新建接口
     * @param interfaceInfo
     * @return
     * @throws Exception
     */
    public void newInterfaceInfo(InterfaceInfo interfaceInfo) throws Exception;
    
    public int create(InterfaceInfo interfaceInfo) throws Exception;

    /**
     * 新建接口
     * @param interfaceInfo
     * @return
     * @throws Exception
     */
    public void updateInterfaceInfo(InterfaceInfo interfaceInfo) throws Exception;

    /**
     * 获取跨语言的接口id
     * @return
     * @throws Exception
     */
    public List<Integer> getCrossLangInterfaceIds() throws Exception;

    /**
     * 更新cross标志
     * @param interfaceId
     * @throws Exception
     */
    public void updateCrossLang(int interfaceId) throws Exception;

    /**
     * 获取接口信息
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public InterfaceInfo getByInterfaceName(String interfaceName) throws Exception;

    /**
     * 获取所有接口信息
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getAll() throws Exception;
    
    /**
     * 根据更新时间获取变化的接口
     * @param time
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getInterfaceByCreateTime(Date time) throws Exception;
    
    /**
     * 获取接口版本
     * @param time
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getInterfaceVersionByTime(Date time) throws Exception;

    /**
     * 根据接口获取erp账号
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getErps() throws Exception;
    
    
    /**
     * 获取saf21所有接口
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getAllWithJsfclient(Date date) throws Exception;
    
    public int updateJsfVer(String interfaceName) throws Exception;

	/**
	 * @param info
	 * @return
	 */
	public List<String> getOwnerUsers(InterfaceInfo info);

	/**
	 * @param urs
	 */
	public void batchInsert(List<UserResource> urs);

	/**
	 * @param interfaceId
	 * @param value
	 * @return
	 */
	public List<String> findAuthErps(Integer interfaceId, int value);

	/**
	 * @param info
	 */
	public void deleteToSave(InterfaceInfo info);

	/**
	 * @param isExistInfo
	 */
	public void updateByName(InterfaceInfo isExistInfo);

	/**
	 * 计算saf_interface表privider数(总数\live)和consumer数(总数\live)
	 */
	public void sumProviderAndConsumer();

}
