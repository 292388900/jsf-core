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
package com.ipd.jsf.registry.manager.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.registry.domain.SysParam;
import com.ipd.jsf.registry.manager.SysParamManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;

public class TestSysParamManager extends ServiceBaseTest {

    @Autowired
    SysParamManager sysParamManager;

    @Test
    public void test1() throws Exception {
        List<Integer> typeList = new ArrayList<Integer>();
        typeList.add(DataEnum.SysParamType.CLientConfig.getValue());
        typeList.add(DataEnum.SysParamType.GlobalConfig.getValue());
        List<SysParam> list = sysParamManager.getListByType(typeList);
        Assert.assertNotNull(list);
    }
}
