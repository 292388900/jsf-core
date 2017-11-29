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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.UserResource;

@Repository
public interface InterfaceInfoDao {
    /**
     * 创建接口
     * @param interfaceInfo
     * @return
     * @throws Exception
     */
    public int create(InterfaceInfo interfaceInfo) throws Exception;

    /**
     * 修改接口
     * @param interfaceInfo
     * @return
     * @throws Exception
     */
    public int update(InterfaceInfo interfaceInfo) throws Exception;

    /**
     * 获取跨语言的接口id
     * @return
     * @throws Exception
     */
    public List<Integer> getCrossLangInterfaceIds() throws Exception;

    /**
     * 更新接口的crossLang
     * @param interfaceId
     * @return
     * @throws Exception
     */
    public int updateCrossLang(@Param("interfaceId") int interfaceId) throws Exception;
    
    /**
     * 获取接口信息
     * @param interfaceName
     * @return
     * @throws Exception
     */
    public InterfaceInfo getByInterfaceName(@Param("interfaceName") String interfaceName) throws Exception;

    /**
     * 获取所有接口信息
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getAll() throws Exception;

    /**
     * 根据更新时间获取变化的接口
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getInterfaceByCreateTime(@Param("time") Date time) throws Exception;

    /**
     * 获取接口版本
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getInterfaceVersionByTime(@Param("time") Date time) throws Exception;

    /**
     * 根据接口，获取erp帐号
     * @return
     * @throws Exception
     */
    public List<InterfaceInfo> getErps() throws Exception;

	/**
	 * 
	 */
	public List<InterfaceInfo> getAllWithJsfclient(@Param("date")Date date) throws Exception;
	
	public int updateJsfVer(String interfaceName) throws Exception;

	/**
	 * @param info
	 * @return
	 */
	public List<String> getOwnerUsers(InterfaceInfo info);
	
	public void batchInsertResource(@Param("list")List<UserResource> res);

	/**
	 * @param resId
	 * @param resType
	 * @return
	 */
	List<String> findAuthErps(@Param("resId")Integer resId, @Param("resType")Integer resType);


	public List<String> getAlarmErps(@Param("interfaceName")String interfaceName);

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

    //根据id更新部门code
    void updateDepartmentCode(@Param(value="id")int id, @Param(value="departmentCode")String departmentCode);

    int count();

    Collection list(@Param(value="offset")int offset, @Param(value="size")int size);

    Collection listAll();
    
    /**
	 * 分页查询
	 * 
	 * @param start
	 * @param size
	 * @return
	 */
	List<Map<String,Object>> listByPage(@Param(value = "start") int start, @Param(value = "size") int size);
}
