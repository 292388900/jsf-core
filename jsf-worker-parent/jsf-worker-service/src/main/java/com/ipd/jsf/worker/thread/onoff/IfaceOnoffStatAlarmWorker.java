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
package com.ipd.jsf.worker.thread.onoff;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import com.ipd.jsf.worker.manager.AlarmManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import com.ipd.jsf.worker.service.common.URL;
import com.ipd.jsf.worker.service.common.cache.IfaceOnoffCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.AlarmType;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.util.WorkerAppConstant;

/**
 * Title: <br>
 *
 * Description: 统计接口半小时内的上下线次数, 超过阀值的话，报警
 *
 */
public class IfaceOnoffStatAlarmWorker extends SingleWorker{
	
	private final static Logger logger = LoggerFactory.getLogger(IfaceOnoffStatAlarmWorker.class);
	
	private final int spaceInteval = 30 * 60;
	
	private int SAF_TRACELOG_THRESHOLD_KEY_VALUE;
	
	private int SAF_TRACELOG_THRESHOLD_KEY_IFVALUE;
	
	@Autowired
    SysParamManager sysParamManager;
	
	@Autowired
    AlarmManager alarmManager;
	

	private void checkTraceLogAlarm(){
		try {
			//单个实例的上下线数量
			SAF_TRACELOG_THRESHOLD_KEY_VALUE = Integer.parseInt(sysParamManager.findValueBykey(WorkerAppConstant.SAF_TRACELOG_THRESHOLD_KEY));
			// 整个接口的上下线数量
			SAF_TRACELOG_THRESHOLD_KEY_IFVALUE = Integer.parseInt(sysParamManager.findValueBykey(WorkerAppConstant.SAF_TRACELOG_THRESHOLD_IFKEY));
			
			Map<String, AtomicInteger> map = null;
			synchronized (IfaceOnoffCache.traceLogChangeMap) {
				map = new HashMap<String, AtomicInteger>(IfaceOnoffCache.traceLogChangeMap);
				logger.info("上下线统计: {}", map.toString());
				IfaceOnoffCache.traceLogChangeMap.clear();
			}
			
			checkAlarmByNode(map); //实例上下线
			checkAlarmByInterface(map);  //接口上下线
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	
	private void checkAlarmByNode(Map<String, AtomicInteger> map) {
		for(Entry<String, AtomicInteger> entry : map.entrySet()){
			URL url= URL.valueOf(entry.getKey());
			Object[] objects = new Object[]{entry.getKey(), spaceInteval/60, entry.getValue().get()};
			if(url.getParameter("isonline").equals("1")){
				logger.info("上下线报警统计: {}在{}分钟内上线{}次", objects);
			}else {
				logger.info("上下线报警统计: {}在{}分钟内下线{}次", objects);
			}
			
			if(entry.getValue().get() >= SAF_TRACELOG_THRESHOLD_KEY_VALUE){
				//TODO 功能开关
				saveAlarm(url, entry.getValue().get());
			}
		}
	}
	
	private void checkAlarmByInterface(Map<String, AtomicInteger> map) {
		Map<String, Integer> changeMap = new HashMap<String, Integer>();
		for(Entry<String, AtomicInteger> entry : map.entrySet()){
			URL url = URL.valueOf(entry.getKey());
			Map<String, String> param = new HashMap<String, String>();
			param.put("isprovider", String.valueOf(url.getParameter("isprovider")));
			param.put("isonline", String.valueOf(url.getParameter("isonline")));
			URL tmpUrl = new URL("tmp", "1.1.1.1", 0, url.getServiceInterface(), param);
			String key = tmpUrl.toFullString();
			if(changeMap.get(key) == null){
				changeMap.put(key, 1);
			}else {
				changeMap.put(key, changeMap.get(key) + 1);
			}
		}
		
		for(Entry<String, Integer> entry : changeMap.entrySet()){
			URL url = URL.valueOf(entry.getKey());
			Object[] objects = new Object[]{entry.getKey(), spaceInteval/60/1000, entry.getValue()};
			if(url.getParameter("isonline").equals("1")){
				logger.info("上下线报警统计: {}在{}分钟内上线{}次", objects);
			}else {
				logger.info("上下线报警统计: {}在{}分钟内下线{}次", objects);
			}
			if(entry.getValue() >= SAF_TRACELOG_THRESHOLD_KEY_IFVALUE){
				//TODO 功能开关
				saveAlarm(url, entry.getValue());
			}
		}
		
		
	}
	
	
	private void saveAlarm(URL url, Integer value) {
		boolean isProvider = url.getParameter("isprovider").equals("1") ? true : false;
		boolean isOnline = url.getParameter("isonline").equals("1") ? true : false;
		String text = getAlarmText(url, value, isProvider, isOnline);
		
		try {
			
			JsfAlarmHistory alarmHistory = new JsfAlarmHistory();
			alarmHistory.setAlarmKey(WorkerAppConstant.SAF_ONOFF_STAT_ALARMKEY);
			alarmHistory.setErps(sysParamManager.findValueBykey(WorkerAppConstant.ALARM_ADMIN_ADDRESS));
			alarmHistory.setIsAlarmed((byte)0);
			alarmHistory.setCreateTime(new Date());
			alarmHistory.setContent(text);
			alarmHistory.setAlarmType((byte)AlarmType.IFACEONOFFSTAT.getValue());
			
			alarmManager.insert(alarmHistory);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
	}
	
	
	private String getAlarmText(URL url, long value, boolean isProvider, boolean isOnline) {
		String onlineTag = isOnline ? "上线" : "下线";
		String interfaceName = url.getServiceInterface();
		interfaceName = interfaceName.substring(interfaceName.lastIndexOf(".")+1);
		String protocol = url.getProtocol();
		StringBuilder sb = new StringBuilder("[JSF]");
		String tag = null;
        if (protocol.equals("tmp")) {
            if (isProvider) {
                tag = "提供端";
            } else {
                tag = "调用端";
            }
        } else {
            String host = url.getHost();
            int port = url.getPort();

            if (isProvider) {
                tag = "提供端, 服务器:" + host + ",端口:" + port;
            } else {
                tag = "调用端, 服务器:" + host;
            }
        }
        sb.append(interfaceName).append("在").append(spaceInteval / 60)
                .append("分钟内").append(onlineTag).append("次数为:").append(value)
                .append(",").append(tag);
        return sb.toString();
		
	}


	@Override
	public boolean run() {
		try {
			//TODO 如何控制开关？
			checkTraceLogAlarm();
			return true;
		} catch (Exception e) {
			logger.error("上下线统计worker异常:"+e.getMessage(), e);
			return false;
		}
	}


	@Override
	public String getWorkerType() {
		return "ifaceOnoffStatAlarmWorker";
	}


}
