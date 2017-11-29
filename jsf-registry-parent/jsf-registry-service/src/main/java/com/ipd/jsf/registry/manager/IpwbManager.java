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
package com.ipd.jsf.registry.manager;

import java.util.List;
import java.util.Map;

import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.Ipwblist;

public interface IpwbManager {

    /**
     * 根据接口列表获取接口的黑白名单
     * key: interfaceId, second key: wbtype, value: Ipwblist list
     * @return
     * @throws Exception
     */
    public Map<String, Map<String, List<Ipwblist>>> getListByInterfaceIdList(List<InterfaceInfo> list) throws Exception;
}
