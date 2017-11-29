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
package com.ipd.jsf.worker.common.dao;

import com.ipd.jsf.worker.common.domain.WorkerInfo;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * worker定义查询接口
 *
 */
@Repository
public interface WorkerDefineDAO {

    /**
     * 加载所有worker定义信息
     *
     * @return
     */
    List<WorkerInfo> findWorkerInfos();

    WorkerInfo findWorkerInfo(@Param("id")int id);

    WorkerInfo findWorkerInfoByName(@Param("name")String name);

    List<WorkerInfo> findWorkerInfosByWorkerType(@Param("workerTypes") List<String> workerTypes);

}
