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
package com.ipd.jsf.registry.dao;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.registry.domain.JsfIns;

@Repository
public interface JsfInsDao {
    /**
     * 创建实例
     * @param ins
     * @param status
     * @return
     * @throws Exception
     */
    public int create(@Param("ins") JsfIns ins, @Param("status") int status) throws Exception;

    /**
     * 更新实例心跳时间
     * @param insKeyList
     * @param hbTime
     * @param regIp
     * @return
     * @throws Exception
     */
    public int batchUpdateHb(@Param("insKeyList") List<String> insKeyList, @Param("hbTime") Date hbTime, @Param("regIp") String regIp) throws Exception;

    /**
     * 根据inskey获取inskey，主要用来判断实例是否存在
     * @param insKeyList
     * @return
     * @throws Exception
     */
    public List<String> getInsKeyListByInsKey(@Param("insKeyList") List<String> insKeyList) throws Exception;

    /**
     * 根据inskey获取inskey，主要用来判断实例是否存在
     * @param insKey
     * @return
     * @throws Exception
     */
    public String getInsKeyByInsKey(@Param("insKey") String insKey) throws Exception;
}
