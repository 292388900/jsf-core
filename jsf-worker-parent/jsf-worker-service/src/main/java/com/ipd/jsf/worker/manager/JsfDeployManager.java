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
package com.ipd.jsf.worker.manager;

import java.util.Date;
import java.util.List;

import com.ipd.jsf.worker.domain.Server;

public interface JsfDeployManager {

	/**
	 * 自动部署上线操作
	 * @param appId
	 * @param appInsId
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public List<Server> onlineByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception;

	/**
	 * 自动部署下线操作
	 * @param appId
	 * @param appInsId
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public List<Server> offlineByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception;

	/**
	 * 自动部署删除操作
	 * @param appId
	 * @param appInsId
	 * @param pid
	 * @return
	 * @throws Exception
	 */
	public List<Server> delByDeploy(Integer appId, String appInsId, int pid, Date date) throws Exception;
}
