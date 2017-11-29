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
package com.ipd.jsf.worker.thread.alarm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import com.ipd.jsf.worker.log.dao.JsfAlarmHistoryDao;
import com.ipd.jsf.worker.manager.AlarmManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import com.ipd.jsf.worker.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.AlarmType;
import com.ipd.jsf.gd.util.DateUtils;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.util.WorkerAppConstant;

public class AlarmServiceCheckWorker extends SingleWorker{
	
	private final Logger logger = LoggerFactory.getLogger(getClass());
	
	@Autowired
	private JsfAlarmHistoryDao alarmHistoryDao;
	
	@Autowired
    AlarmManager alarmManager;
	
	@Autowired
    SysParamManager sysParamManager;
	
	private final String paramKey= "alarmworker.alarmnum";
	
	private static final long step = DateUtils.MILLISECONDS_PER_MINUTE * 15;
	private static final int addPercent = 30;

	@Override
	public boolean run() {
		try {
			
			long e = System.currentTimeMillis();
			long s = e - DateUtils.MILLISECONDS_PER_HOUR;
			
			Map<Integer, List<Integer>> alarmMap = new HashMap<Integer, List<Integer>>();
			AlarmType[] alarmTypes = AlarmType.values();
			long tmpTime = s;
			for(AlarmType type : alarmTypes){
				List<Integer> continueCnt = new ArrayList<Integer>();
				int alarmType = type.getValue();
				for(; s <= e; s += step){
					Date startTime = new Date(tmpTime);
					Date endTime = new Date(s);
					int cnt = alarmHistoryDao.getListByTime(startTime, endTime, alarmType);
					continueCnt.add(cnt);
					tmpTime = s;
				}
				s = e - DateUtils.MILLISECONDS_PER_HOUR;
				alarmMap.put(alarmType, continueCnt);
				logger.info("报警worker检查区间内的报警数量: {}, 类型: {}", continueCnt, type.getName());
			}
			
			String erps = PropertyUtil.getProperties("admin.erp.address");
			int addThreshold = Integer.parseInt(sysParamManager.findValueBykey(paramKey));
			for(Entry<Integer, List<Integer>> entry : alarmMap.entrySet()){
				int alarmType = entry.getKey();
				List<Integer> continueCnt = entry.getValue();
				int firstAlarmCnt = continueCnt.get(0);
				int tmp = firstAlarmCnt;
				boolean continueAdd = false; // 是否持续增长
				boolean addOverExpect = false; // 在检查间隔内, 是否存在某个区间的报警次数较第一个区间增加了30%
				for(int i = 1; i < continueCnt.size(); i++){
					int thisCnt = continueCnt.get(i);
					if(thisCnt > tmp){
						continueAdd = true;
					}else {
						continueAdd = false;
					}
					tmp = thisCnt;
					
					if(firstAlarmCnt != 0){
						if((thisCnt - firstAlarmCnt)/firstAlarmCnt * 100 > addPercent && thisCnt >= addThreshold){
							addOverExpect = true;
						}
					}else {
						if(thisCnt > addThreshold){
							addOverExpect = true;
						}
					}
				}
				
				AlarmType alarm = AlarmType.fromValue(alarmType);
				if(continueAdd || addOverExpect){
					logger.info("{}报警数量持续增加, 请关注.", alarm.getName());
					String text = "报警类型:"+alarm.getName()+"在最近一小时内持续增加, 请关注.";
					JsfAlarmHistory alarmHistory = new JsfAlarmHistory();
					alarmHistory.setAlarmKey(WorkerAppConstant.ALARM_WORKER_CHECKI_ALARMKEY);
					alarmHistory.setErps(erps);
					alarmHistory.setIsAlarmed((byte)0);
					alarmHistory.setCreateTime(new Date());
					alarmHistory.setContent(text);
					alarmHistory.setAlarmType((byte)AlarmType.ALARMWORKERCHECK.getValue());
					
					alarmManager.insert(alarmHistory);
					
					logger.info("插入报警列表：{}, alarmType:{}", text, alarm.getName());
				}else {
					logger.info("本次checkworker报警数量正常, alarmType: {}", alarm.getName());
				}
				
			}
			
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public String getWorkerType() {
		return "alarmServiceCheckWorker";
	}

}
