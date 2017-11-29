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
package com.ipd.jsf.worker.saf1dao;

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.Saf1InterfaceInfo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository(value="saf1InterfaceInfoDao")
public interface Saf1InterfaceInfoDao {
    /**
     * 获取所有接口信息
     * @return
     * @throws Exception
     */
    public Saf1InterfaceInfo getByName(@Param("interfaceName") String interfaceName) throws Exception;

    /**
     * 获取在time以后发生变化的接口信息
     * @return
     * @throws Exception
     */
    public List<Saf1InterfaceInfo> getAfterTime(@Param("time") Date time) throws Exception;
}
