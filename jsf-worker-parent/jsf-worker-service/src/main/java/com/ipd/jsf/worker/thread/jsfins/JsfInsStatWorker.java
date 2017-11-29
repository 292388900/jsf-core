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
package com.ipd.jsf.worker.thread.jsfins;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.domain.JsfInsStat;
import com.ipd.jsf.worker.service.JsfInsStatService;

public class JsfInsStatWorker extends SingleWorker {
	
	private final static Logger logger = LoggerFactory.getLogger(JsfInsStatWorker.class);

	@Autowired
	JsfInsStatService jsfInsStatService;

	private int shouldStatWeek(int weekOfYear) {
		JsfInsStat lastWeekRecord = new JsfInsStat();
		int shouldStatWeek = 0;
		try {
			lastWeekRecord = jsfInsStatService.getLastWeekStat();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		if(lastWeekRecord == null || lastWeekRecord.getWeekend() == 0){
			shouldStatWeek = weekOfYear;
		}else {
			shouldStatWeek = lastWeekRecord.getWeekend();
		}
		shouldStatWeek = (shouldStatWeek == weekOfYear) ? shouldStatWeek : shouldStatWeek + 1;
		if(shouldStatWeek >= 53){
			shouldStatWeek = 1;
		}
		return shouldStatWeek;
	}

	@Override
	public boolean run() {
		try {
			logger.info("SAF实例数定时统计worker开始运行。。。");
			Calendar calendar = Calendar.getInstance();
			int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
			int shouldStatWeek = shouldStatWeek(weekOfYear);
			
			SimpleDateFormat format = new SimpleDateFormat("yyyy");
			while(shouldStatWeek <= weekOfYear) {
				Calendar dateCalendar = Calendar.getInstance();
				//dateCalendar.set(Calendar.WEEK_OF_YEAR, shouldStatWeek);

			    Date time = dateCalendar.getTime();
				logger.info("开始统计第{}周实例数", shouldStatWeek);
				
				Map<String, Integer> safinsMap = jsfInsStatService.getInsStatMap(time);
                logger.info("开始检查第{}周数据是否存在", shouldStatWeek);
                Map<String, Object> param = new HashMap<String, Object>();
                param.put("createTime", format.format(time));
                param.put("weekend", shouldStatWeek);
                boolean exists = jsfInsStatService.isExist(param);
                if (!exists) {
    				int pInstanceNum = safinsMap.get(JsfInsStatService.SAF_PROVIDER_INSTANCE_NUM);
    				int cInstanceNum = safinsMap.get(JsfInsStatService.SAF_CONSUMER_INSTANCE_NUM);
    				int pIPs = safinsMap.get(JsfInsStatService.SAF_PROVIDER_IP_NUM);
    				int cIPs = safinsMap.get(JsfInsStatService.SAF_CONSUMER_IP_NUM);
    				int totalIPs = safinsMap.get(JsfInsStatService.SAF_TOTAL_IP_NUM);
    				logger.info("pInstanceNum： {}", pInstanceNum);
    				logger.info("cInstanceNum： {}", cInstanceNum);
    				logger.info("pIPs： {}", pIPs);
    				logger.info("cIPs： {}", cIPs);
    				logger.info("totalIPs： {}", totalIPs);
    				
    				JsfInsStat preWeekRecord = null;
    				try {
    					int preWeek = shouldStatWeek - 1;
						if (preWeek == 0) {
							preWeek = 52;
						}
    					logger.info("获取上周： {}实例数据", (preWeek));
    					preWeekRecord = jsfInsStatService.getByWeek(preWeek);
    					if(preWeekRecord == null){
    						logger.warn("获取第上周{}实例数据时, 得到结果为null", (preWeek));
    						preWeekRecord = new JsfInsStat();
    					}
    				} catch (Exception e) {
    					logger.error(e.getMessage(), e);
    					preWeekRecord = new JsfInsStat();
    				}
    				
                    JsfInsStat instance = new JsfInsStat();
                    instance.setCreateTime(time);
    				instance.setpInstance(pInstanceNum);
    				instance.setcInstance(cInstanceNum);
    				instance.setTotalInstance(pInstanceNum + cInstanceNum);
    				instance.setpIps(pIPs);
    				instance.setcIps(cIPs);
    				instance.setIpNum(totalIPs);
    				instance.setWeekend(shouldStatWeek);
    				instance.setTotalinsAdd(pInstanceNum + cInstanceNum - preWeekRecord.getTotalInstance());
    				instance.setTotalipAdd(totalIPs - preWeekRecord.getIpNum());
    				instance.setPinsAdd(pInstanceNum - preWeekRecord.getpInstance());
    				instance.setCinsAdd(cInstanceNum - preWeekRecord.getcInstance());
    				instance.setCipAdd(cIPs - preWeekRecord.getcIps());
    				instance.setPipAdd(pIPs - preWeekRecord.getpIps());
    				
    				try {
    					JsfInsStat lastWeekRecord = jsfInsStatService.getLastWeekStat();
    					if(lastWeekRecord == null || lastWeekRecord.getWeekend() != weekOfYear) {
    						logger.info("开始插入第{}周数据", shouldStatWeek);
    						jsfInsStatService.insert(instance);
    					}
    				} catch (Exception e) {
    					logger.error(e.getMessage(), e);
    				}
                } else {
                    logger.info("第{}周数据已经存在", shouldStatWeek);
                }
				shouldStatWeek++;
			}
			return true;
		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public String getWorkerType() {
		return "safInsStatWorker";
	}

}
