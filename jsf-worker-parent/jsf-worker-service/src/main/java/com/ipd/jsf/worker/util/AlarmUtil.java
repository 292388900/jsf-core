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
package com.ipd.jsf.worker.util;

import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlarmUtil {
	private final static Logger logger = LoggerFactory.getLogger(AlarmUtil.class);


	/**
	 * 将本地history类转换为远端的history类
	 * @param history
	 * @return
	 */
    public static com.ipd.jsf.alarm.domain.JsfAlarmHistory convertToRemoteHistory(JsfAlarmHistory history) {
    	com.ipd.jsf.alarm.domain.JsfAlarmHistory remote = new com.ipd.jsf.alarm.domain.JsfAlarmHistory();
    	remote.setAlarmKey(history.getAlarmKey());
    	remote.setAlarmTime(history.getAlarmTime());
    	remote.setAlarmType(history.getAlarmType());
    	remote.setContent(history.getContent());
    	remote.setCreateTime(history.getCreateTime());
    	remote.setErps(history.getErps());
    	remote.setExtendKey1(history.getExtendKey1());
    	remote.setExtendKey2(history.getExtendKey2());
    	remote.setInterfaceName(history.getInterfaceName());
    	remote.setAlarmIp(history.getAlarmIp());
    	remote.setIsAlarmed(history.getIsAlarmed());
    	remote.setMethodName(history.getMethodName());
    	return remote;
    }
}
