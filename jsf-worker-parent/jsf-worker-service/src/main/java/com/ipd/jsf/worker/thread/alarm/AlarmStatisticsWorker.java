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

 import com.ipd.jsf.worker.common.SingleWorker;
 import com.ipd.jsf.worker.manager.AlarmStatisticsManager;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;

 import com.ipd.jsf.worker.domain.JsfAlarmStatistics;

 import java.util.*;
 import java.text.SimpleDateFormat ;

 public class AlarmStatisticsWorker extends SingleWorker{

     private final Logger logger = LoggerFactory.getLogger(getClass());

     static boolean firstRun = true;//该worker第一次执行标志

     @Autowired
     AlarmStatisticsManager alarmStatisticsManager;


     @Override
     public boolean run() {
         try {
             if (firstRun){
                 statisticsHistoryAlarm(10);//统计过去10天的数据,如果数据库中已经存在当天的统计数据，就不会再插入或者更新
                 firstRun = false;
             }

             String today = "";//今天
             String yesterday = "";//昨天

			 SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
             Calendar cal = Calendar.getInstance();// 取当前日期。
             today = format.format(cal.getTime());
             cal.add(Calendar.DAY_OF_MONTH, -1);// 取当前日期的前一天.
             yesterday = format.format(cal.getTime());

             //数据库中不存在今天的数据，说明过来凌晨12点，现在是新的一天了，最后一次更新昨天的数据，然后写今天的数据
             if(alarmStatisticsManager.isExists(today) == 0) {

                 alarmStatisticsManager.deleteOneDay(yesterday);
                 logger.info("删除据库表saf_alarm_statistics中 日期：{} 的报警统计数据", yesterday);
                 //查询昨天的统计数据
                 List<JsfAlarmStatistics> list_JsfAlarmStatistics =  alarmStatisticsManager.selectOneDayStatistics(yesterday);
                 //从数据库中查询出来的时候是不带时间的，所以在这里赋值
                 for(int i = 0; i < list_JsfAlarmStatistics.size(); i++) {
                     list_JsfAlarmStatistics.get(i).setAlarmDate(yesterday);
                 }
                 alarmStatisticsManager.batchInsert(list_JsfAlarmStatistics);
                 logger.info("向数据库表saf_alarm_statistics中批量插入 {} 条 JsfAlarmStatistics 报警统计数据", list_JsfAlarmStatistics.size());

             }else{//先删除今天的旧数据
                 alarmStatisticsManager.deleteOneDay(today);
                 logger.info("删除据库表saf_alarm_statistics中 日期：{} 的报警统计数据", today);
             }

             //向数据库中插入今天的最新的统计数据
             List<JsfAlarmStatistics> list_JsfAlarmStatistics =  alarmStatisticsManager.selectOneDayStatistics(today);

             if(list_JsfAlarmStatistics == null || list_JsfAlarmStatistics.isEmpty()){
                 logger.info("今天还没有报警数据");
                 return true;
             }

             //从数据库中查询出来的时候是不带时间的，所以在这里赋值
             for(int i = 0; i < list_JsfAlarmStatistics.size(); i++) {
                 list_JsfAlarmStatistics.get(i).setAlarmDate(today);
             }
             alarmStatisticsManager.batchInsert(list_JsfAlarmStatistics);
             logger.info("向数据库表saf_alarm_statistics中批量插入 {} 条 JsfAlarmStatistics 报警统计数据", list_JsfAlarmStatistics.size());

         } catch (Exception e) {
             logger.error(e.getMessage(), e);
             return false;
         }
         return true;
     }

     //统计以前的报警数据，插入到数据库中去。只临时执行一次就够了
     //dayNum表示天数，即统计从现在到dayNum天前的数据
     private void statisticsHistoryAlarm(int dayNum){

         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
         Calendar longlongAgo = Calendar.getInstance();
         longlongAgo.add(Calendar.DAY_OF_MONTH, -dayNum);//统计过去dayNum天前的数据
         Calendar now = Calendar.getInstance();
         while(now.after(longlongAgo)){

             now.add(Calendar.DAY_OF_MONTH, -1);//往前推一天
             String time = format.format(now.getTime());
             if(alarmStatisticsManager.isExists(time)!=0) {
                 continue;
             }

             List<JsfAlarmStatistics> list_JsfAlarmStatistics = alarmStatisticsManager.selectOneDayStatistics(time);
             //从数据库中查询出来的时候是不带时间的，所以在这里赋值
             for(int i = 0; i < list_JsfAlarmStatistics.size(); i++) {
                 list_JsfAlarmStatistics.get(i).setAlarmDate(time);
             }

             try {
                 alarmStatisticsManager.batchInsert(list_JsfAlarmStatistics);
             } catch (Exception e) {
                 logger.info("向数据库表saf_alarm_statistics中批量插入{}天的报警统计数据出错。{}",time, e);
                 e.printStackTrace();
             }
         }
     }

     @Override
     public String getWorkerType() {
         return "alarmStatisticsWorker";
     }
 }
