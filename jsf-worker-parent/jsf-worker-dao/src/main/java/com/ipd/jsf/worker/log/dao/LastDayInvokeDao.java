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
package com.ipd.jsf.worker.log.dao;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.worker.domain.LastDayInvoke;

@Repository
public interface LastDayInvokeDao {

	Integer insert(LastDayInvoke invoke);

	Integer getByDate(String invokeDate);

	/**
	 * @param ifaceInvoke
	 */
	void insertIface(LastDayInvoke ifaceInvoke);

	/**
	 * @param ifaceInvoke
	 * @return
	 */
	Integer getIfaceByDate(LastDayInvoke ifaceInvoke);

	Integer deleteIfaceInvoke(LastDayInvoke ifaceInvoke);

	Integer deleteCalltimeByDate(String invokeDate);

	Integer updateCallTimesLastDay(LastDayInvoke ifaceInvoke);

	Integer updateCallTimesIface(LastDayInvoke ifaceInvoke);

	Integer insertIfaceHistoHour(LastDayInvoke ifaceInvoke);

	Integer updateIfaceHistoHour(LastDayInvoke ifaceInvoke);

	Long getIfaceHistoHourSumByDate(LastDayInvoke ifaceInvoke);

	Integer getIfaceHistoHourCountByDateTime(LastDayInvoke ifaceInvoke);

	Long getIfaceHistoSumByDate(LastDayInvoke ifaceInvoke);

	int deleteIfaceHistoByDate(@Param(value = "invokeDate") String invokeDate);

	int deleteIfaceHistoHourByDate(@Param(value = "invokeDate") String invokeDate);

}
