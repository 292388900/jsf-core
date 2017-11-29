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

import com.ipd.jsf.worker.domain.Department;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JsfDepartmentUpdateDao {

    Department getDepartmentByCode(String departmentCode);

    List<Department> getDepartmentByName(String departmentName);

    //获得一级部门
    List<Department> getFirstLevelDepartment();

    //根据部门级别获取部门
    List<Department> getDepartmentByLevel(int departmentLevel);


    int insert(Department department);

    //没有就插入，有就更新
    int insertOrUpdate(Department obj) throws Exception;

    int update(Department department) throws Exception;

    int isExists(String departmentCode);

    int delete(String departmentCode);
}
