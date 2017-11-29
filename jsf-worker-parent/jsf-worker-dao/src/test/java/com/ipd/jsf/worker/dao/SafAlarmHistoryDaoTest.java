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
package com.ipd.jsf.worker.dao;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ipd.jsf.common.enumtype.AlarmType;
import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import com.ipd.jsf.worker.log.dao.JsfAlarmHistoryDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring-database.xml" })
public class SafAlarmHistoryDaoTest {
	
	@Autowired
	private JsfAlarmHistoryDao sahDao;

	Integer id;
	
	@Test
	public void testSelectTime() {
		Date createTime = new Date(1000000);
		List<JsfAlarmHistory> resultList = this.sahDao.selectByTime(createTime);
		
		Assert.assertNotNull(resultList);
	}

	@Test
	public void testInsert(){
		JsfAlarmHistory sah = new JsfAlarmHistory();
		sah.setIsAlarmed((byte)0);
		sah.setAlarmKey("test.alarm");
		sah.setAlarmType((byte) AlarmType.SERVERONOFF.getValue());
		sah.setContent("alarm is hapened!");
		sah.setCreateTime(new Date());
		sah.setErps("xxx");
		sah.setInterfaceName("com.ipd.saf.worker.dao.SafAlarmHistoryDao");
		sah.setMethodName("");
		int result = sahDao.insert(sah);
		Assert.assertEquals(1, result);
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, 2014);
		cal.set(Calendar.MONTH, 5);
		cal.set(Calendar.DAY_OF_MONTH, 15);
		Date alarmTime = cal.getTime();
		
//		sahDao.updateByPrimaryKey(sah.getId(), alarmTime, "13810538161");
		
//		SafAlarmHistory other = sahDao.selectById(sah.getId());
//		Assert.assertNotNull(other);
//		Assert.assertEquals(sah.getInterfaceName(), other.getInterfaceName());
//		Assert.assertEquals(alarmTime.toString(), other.getAlarmTime().toString());
//		Assert.assertEquals("13810538161", other.getRemarks());
//		Assert.assertEquals(Byte.valueOf("1"), other.getAlarmed());
	}
	
	@Test
	public void testCountByTime(){
		Date createTime = new Date(10000000);
		
		List<JsfAlarmHistory> alarmList = sahDao.countByTime(createTime, 0);
		Assert.assertNotNull(alarmList);
	}
	
	@Test
	public void testGetNewLastTime(){
//		SafAlarmHistory sah = sahDao.getNewLastTime();
//		Assert.assertNotNull(sah);
	}
}
