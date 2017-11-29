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

import java.util.Calendar;
import java.util.Date;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.gd.util.NetUtils;

public class WorkerUtil {
    private static Logger logger = LoggerFactory.getLogger(WorkerUtil.class);

    //本地ip
    private static String workerIP = null;

    /**
     * 返回复活时间：当前时间减去心跳间隔
     * @return
     */
    public static long getRevivalTime() {
        return getSystemCurrentTime() - HeartbeatConstants.REGISTRY_HEARTBEAT_PERIOD;
    }

    /**
     * 返回死亡的心跳时间点：当前时间减去死亡心跳间隔
     * @return
     */
    public static long getDeadTime() {
        return getSystemCurrentTime() - HeartbeatConstants.JUDGE_DEAD_INSTANCE_TIME;
    }

    /**
     * 返回逻辑删除实例的心跳时间点：当前时间减去删除心跳间隔
     * @return
     */
    public static long getInstanceDeleteTime() {
        return getSystemCurrentTime() - HeartbeatConstants.DELETE_DEAD_INSTANCE_FROM_DB_TIME;
    }

//    /**
//     * 返回删除provider（随机端口）/consumer的心跳时间点：当前时间减去删除心跳间隔
//     * @return
//     */
//    public static long getNodeDeleteTime() {
//        return getSystemCurrentTime() - HeartbeatConstants.DELETE_DEAD_NODE_FROM_DB_TIME;
//    }

    /**
     * 获取系统时间
     * @return
     */
    public static long getSystemCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 截取字符
     * @param str
     * @param length
     * @return
     */
    public static String limitString(String str, int length) {
        if (str != null && str.length() > length) {
            str = str.substring(0, length);
        }
        return str;
    }

    /**
     * 将set转为字符串
     * @param set
     * @return
     */
    public static String getStringFromSet(Set<String> set) {
        StringBuilder result = new StringBuilder();
        if (set != null && !set.isEmpty()) {
            for (String value : set) {
                if (!value.isEmpty()) {
                    result.append(value).append(",");
                }
            }
        }
        return result.toString();
    }

    /**
     * 获取本地机器的IP
     * @return
     */
    public static String getWorkerIP() {
        try {
            if (workerIP == null || workerIP.equals("")) {
                String localIp = NetUtils.getLocalAddress().getHostAddress();
                if (localIp != null && !localIp.equals("")) {
                    workerIP = localIp;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return workerIP;
    }

    /**
     * get Calendar of given year
     * @param year
     * @return
     */
    public static Calendar getCalendarFormYear(int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        return cal;
    }

    /**
     * get start date of given week no of a year
     * @param year
     * @param weekNo
     * @return
     */
    public static Date getStartTimeOfWeekNo(int year, int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);
        return cal.getTime();
    }

    /**
     * get the end day of given week no of a year.
     * @param year
     * @param weekNo
     * @return
     */
    public static Date getEndTimeOfWeekNo(int year,int weekNo) {
        Calendar cal = getCalendarFormYear(year);
        cal.set(Calendar.WEEK_OF_YEAR, weekNo);
        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        return cal.getTime();
    }

    /**
     * 计算两个日期直接的间隔
     * @param endDate
     * @param startDate
     * @return
     */
    public static long getDateUtil(long endDate, long startDate) {

        long nd = 1000 * 24 * 60 * 60;
        long nh = 1000 * 60 * 60;
        long nm = 1000 * 60;

        // 获得两个时间的毫秒时间差异
        long diff = endDate - startDate;
        // 计算差多少天
        // long day = diff / nd;
        // 计算差多少小时
        // long hour = diff % nd / nh;
        // 计算差多少分钟
        long min = diff % nd % nh / nm;
        // 计算差多少秒//输出结果
        // long sec = diff % nd % nh % nm / ns;
        return min;
    }

}
