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

import java.util.Date;
import java.util.List;
import java.util.Random;

import com.ipd.jsf.worker.log.dao.JsfAlarmSettingDao;
import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ipd.jsf.worker.domain.JsfAlarmSetting;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:/spring-database.xml" })
public class SafAlarmSettingDaoTest {

	@Autowired
	private JsfAlarmSettingDao sasDao;
	
	@Test
	public void testInsert() {
		JsfAlarmSetting sas = new JsfAlarmSetting();
		sas.setAlarmDesc("");
		sas.setAlarmInterval(1800);
		Random rand = new Random();
		sas.setAlarmKey("test.alarmKey" + rand.nextInt(1000));
		sas.setAlarmType(Byte.valueOf("0"));
		sas.setCreateTime(new Date());
		sas.setIsValid(Byte.valueOf("1"));
		sas.setUserErp("zhangjunfeng7");
		sasDao.insert(sas);
		Integer id = sas.getId();
		JsfAlarmSetting other = sasDao.selectByPk(id);
		Assert.assertEquals(sas.getAlarmKey(), other.getAlarmKey());
	}
	
	@Test
	public void testSelectByTime(){
		Date updateTime = new Date(0);
		List<JsfAlarmSetting> list = sasDao.selectByTime(updateTime);
		Assert.assertNotNull(list);
	}

	@Test
	public void testUpdate(){
		JsfAlarmSetting sas = new JsfAlarmSetting();
		sas.setAlarmDesc("");
		sas.setAlarmInterval(1800);
		Random rand = new Random();
		sas.setAlarmKey("test.alarmKey" + rand.nextInt(1000));
		sas.setAlarmType(Byte.valueOf("1"));
		sas.setCreateTime(new Date());
		sas.setIsValid(Byte.valueOf("1"));
		sas.setUserErp("zhangjunfeng7");
		sasDao.insert(sas);
		Integer id = sas.getId();
		sas.setAlarmDesc("test alarm desc");
		sasDao.updateByPrimaryKey(sas);
		
		JsfAlarmSetting other =  sasDao.selectByPk(id);
		Assert.assertEquals(sas.getAlarmDesc(), other.getAlarmDesc());
	}
}
