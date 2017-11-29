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

import java.util.List;

import com.ipd.jsf.worker.domain.IfaceAlarm;

public interface IfaceAlarmManager {
    /**
     * 获取provider和consumer阀值报警类型列表
     * @return
     */
	public List<IfaceAlarm> list();

	/**
	 * 根据type获取列表
	 * @param typeList
	 * @return
	 * @throws Exception
	 */
	public List<IfaceAlarm> getListByAlarmType(List<Integer> typeList) throws Exception;
	
	/**
	 * 根据type获取列表
	 * @param typeList
	 * @return
	 * @throws Exception
	 */
	public List<IfaceAlarm> getListByRestartAlarm(List<Integer> typeList, Integer restartAlarm) throws Exception;
}
