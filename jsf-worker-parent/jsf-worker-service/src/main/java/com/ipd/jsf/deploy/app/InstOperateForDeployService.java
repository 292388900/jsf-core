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
package com.ipd.jsf.deploy.app;

import com.ipd.jsf.deploy.app.domain.DeployRequest;
import com.ipd.jsf.deploy.app.error.FailInvokeException;

public interface InstOperateForDeployService {

	/**
	 * 根据appId和appInsId上线实例
	 * @param appId
	 * @param appInsId
	 * @return 是否成功
	 */
	public boolean doOnline(Integer appId, Integer appInsId)  throws FailInvokeException; 

	/**
	 * 根据appId和appInsId下线实例
	 * @param appId
	 * @param appInsId
	 * @return 是否成功
	 */
	public boolean doOffline(Integer appId, Integer appInsId)  throws FailInvokeException; 

	/**
	 * 根据appId和appInsId上线实例
	 * @param request
	 * @return 是否成功
	 * @throws FailInvokeException
	 */
	public boolean doInsOnline(DeployRequest request)  throws Exception; 

	/**
	 * 根据appId和appInsId下线实例
	 * @param request
	 * @return 是否成功
	 * @throws FailInvokeException
	 */
	public boolean doInsOffline(DeployRequest request)  throws Exception;
}
