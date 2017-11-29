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

import java.sql.Timestamp;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.JsfIfaceApply;

@Repository
public interface JsfIfaceApplyDao {

    void batchAudit(@Param("ifaces") String[] ifaces, @Param("status") int status, @Param("uid") String uid,
                    @Param("auditor") String auditor, @Param("auditTime") Timestamp auditTime, @Param("rejectReason") String rejectReason);

    /**
     * @param uid
     * @return
     */
    List<JsfIfaceApply> getTobeAudit(String uid);

    List<JsfIfaceApply> batchGetByname(@Param("ifaces") String[] ifaces);

    //根据id更新部门code
    void updateDepartmentCode(@Param(value = "id") int id, @Param(value = "departmentCode") String departmentCode);

    int count();

    List<JsfIfaceApply> list(@Param(value = "offset") int offset, @Param(value = "size") int size);

    List<JsfIfaceApply> listAll();

}