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

package com.ipd.jsf.version.common.service;

import java.util.Date;
import java.util.List;

import com.ipd.jsf.version.common.domain.IfaceAlias;
import com.ipd.jsf.version.common.domain.IfaceAliasVersion;
import com.ipd.jsf.version.common.domain.IfaceServer;

public interface AliasVersionService {
	public List<IfaceAlias> updateVersionAndConfVersion(int interfaceId, Date time) throws Exception;

	public List<IfaceAlias> updateByServerList(List<IfaceServer> serverList, Date time) throws Exception;
	
	public void updateByServerListForCancelAlias(List<IfaceServer> serverList, String cancelAlias, Date time) throws Exception;

	public List<IfaceAlias> updateByInterfaceIdList(List<Integer> interfaceIdList, Date time) throws Exception;

	public List<IfaceAlias> updateByInterfaceId(int interfaceId, Date time) throws Exception;

	public List<IfaceServer> getRelaIfaceServerList(List<IfaceServer> serverList) throws Exception;

	//获取一个接口的所有alias的版本号
	public List<IfaceAliasVersion> getAliasVersionByIfaceId(int interfaceId) throws Exception;
}
