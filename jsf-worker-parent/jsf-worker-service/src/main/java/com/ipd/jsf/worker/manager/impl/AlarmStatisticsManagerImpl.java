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
package com.ipd.jsf.worker.manager.impl;

import com.ipd.jsf.worker.domain.JsfAlarmStatistics;
import com.ipd.jsf.worker.log.dao.JsfAlarmStatisticsDao;
import com.ipd.jsf.worker.manager.AlarmStatisticsManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class AlarmStatisticsManagerImpl implements AlarmStatisticsManager {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private JsfAlarmStatisticsDao jsfAlarmStatisticsDao;


    @Override
    public int insert(JsfAlarmStatistics record) {
        return  jsfAlarmStatisticsDao.insert(record);
    }


    /**
     * 批量插入
     * @param list
     * @throws Exception
     */
    @Override
    public void batchInsert(List<JsfAlarmStatistics> list) throws Exception {

        jsfAlarmStatisticsDao.batchInsert(list);
    }


    /*
    @Override
    public List<JsfAlarmStatisticsItem> getOneDayStatistics(String date){

        String theNextDay = date;//下一天的00:00:00

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();// 取当前日期。
        try {
            Date time = format.parse(date);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(time);
            calendar.add(calendar.DATE, 1);//把日期往后推一天
            theNextDay = format.format(calendar.getTime());

        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return jsfAlarmStatisticsDao.selectOneDay(date,theNextDay);
    }
*/


    @Override
    public List<JsfAlarmStatistics> selectOneDayStatistics(String date){

        String theNextDay = date;//下一天的00:00:00

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();// 取当前日期。
        try {
            Date time = format.parse(date);
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(time);
            calendar.add(calendar.DATE, 1);//把日期往后推一天
            theNextDay = format.format(calendar.getTime());

        } catch (ParseException e) {
            logger.error(e.getMessage(), e);
            e.printStackTrace();
        }
        return jsfAlarmStatisticsDao.selectOneDayStatistics(date, theNextDay);
    }



    @Override
    public int isExists(String date){
        return jsfAlarmStatisticsDao.isExists(date);
    }

    //删除某一天的记录
    @Override
    public void deleteOneDay(String date){

        try {
            jsfAlarmStatisticsDao.deleteOneDay(date);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        }
    }
}
