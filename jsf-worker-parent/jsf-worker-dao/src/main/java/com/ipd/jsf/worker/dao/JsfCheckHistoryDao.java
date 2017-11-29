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

import com.ipd.jsf.worker.domain.JsfCheckHistory;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface JsfCheckHistoryDao {

    public void insert(JsfCheckHistory jsfCheckHistory);

    public void batchInsert(@Param("list") List<JsfCheckHistory> jsfCheckHistories);

    public List<JsfCheckHistory> getList(@Param("time") Date time);

    public void deleteByTime(@Param("time") Date time);
}
