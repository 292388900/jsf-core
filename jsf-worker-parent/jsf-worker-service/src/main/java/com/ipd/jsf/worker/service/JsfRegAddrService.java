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

import java.util.List;

import com.ipd.jsf.worker.domain.JsfRegAddr;

public interface JsfRegAddrService {
	int updateByWorker(JsfRegAddr safRegAddr);
	List<JsfRegAddr> listAll();
	int updateStatus(JsfRegAddr addr);
	/**
	 * @return
	 */
	String getAllUrl();

	/**
	 * 获取上线的注册中心地址
	 * @return
	 */
	List<JsfRegAddr> getValidList();
}
