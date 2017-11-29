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
package com.ipd.jsf.worker.service;

import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.JsfIfaceServer;

import java.util.List;

public interface JsfIfaceServerService {

	public String getServiceInfo(String erp, String interfaceName);
	
	public List<String> getInterfacesWithErp(String erp) throws Exception;

	public List<InterfaceInfo> listAllInterface() throws Exception;

	/**
	 * 只取2个server
	 * @param interfaceName
	 * @return
	 * @throws Exception
	 */
	public List<JsfIfaceServer> getOneServer(String interfaceName) throws Exception;

}
