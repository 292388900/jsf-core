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

import java.util.Date;
import java.util.Map;

import com.ipd.jsf.worker.domain.JsfInsStat;

public interface JsfInsStatService {
	public final String SAF_PROVIDER_INSTANCE_NUM = "SAF_PROVIDER_INSTANCE_NUM";
	public final String SAF_CONSUMER_INSTANCE_NUM = "SAF_CONSUMER_INSTANCE_NUM";
	public final String SAF_PROVIDER_IP_NUM = "SAF_PROVIDER_IP_NUM";
	public final String SAF_CONSUMER_IP_NUM = "SAF_CONSUMER_IP_NUM";
	public final String SAF_TOTAL_IP_NUM = "SAF_TOTAL_IP_NUM";
	
	
	/**
	 * 获取provider实例总数，consumer实例总数，provider ip总数， consumer Ip总数
	 * @return
	 */
	public Map<String, Integer> getInsStatMap(Date date) throws Exception;
	
	int insert(JsfInsStat safInsStat);
	boolean isExist(Map<String, Object> param);
	JsfInsStat getLastWeekStat();
	JsfInsStat getByWeek(int week);
}
