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

import com.google.common.base.Joiner;
import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.util.FileUtil;
import com.ipd.jsf.worker.domain.LogicDelAlarmInfo;
import com.ipd.jsf.worker.manager.InstanceManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;

public class LogicDelAlarmUtil {

    private final static Logger logger = LoggerFactory.getLogger(LogicDelAlarmUtil.class);

    private final static String logicDelAlarmThreshold = "logicdel.alarm.threshold";

    private final static String logicDelAlarmContacts = "logicdel.alarm.contacts";

    //0=close; 1=open
    private final static String logicDelAlarmSwitch = "logicdel.alarm.switch";

    private final static String logicDelAlarmFrequency = "logicdel.alarm.frequency";

    private static volatile long lastExecuteTime = 0L; //上一次执行时间

    private final static String logicDelAlarmRange = "logicdel.alarm.range";

    public static void checkDeadInsToDel(final InstanceManager instanceManager,
                                         final SysParamManager sysParamManager) {
        long currExecuteTime = System.currentTimeMillis();
        try {
            String isOpen = sysParamManager.findValueBykey(logicDelAlarmSwitch);
            if (!"1".equals(isOpen)) {
                logger.warn("checkDeadInsToDel is Closed!");
                return;
            }

            long frequency = 30L; //30分钟
            String frequencyStr = sysParamManager.findValueBykey(logicDelAlarmFrequency);
            if (frequencyStr != null && frequencyStr.length() > 0) {
                try {
                    frequency = Long.parseLong(frequencyStr);
                    if (frequency <= 0L) {
                        frequency = 30L;
                    }
                } catch (Exception e) {
                    frequency = 30L;
                }
            }


        if (currExecuteTime - lastExecuteTime < (frequency * 60 * 1000)) {
            logger.warn("checkDeadInsToDel no reach next execute time!");
            return;
        }
            lastExecuteTime = currExecuteTime;


            String rangeStr = sysParamManager.findValueBykey(logicDelAlarmRange);
            long range = 7L;
            if (rangeStr != null && rangeStr.length() > 0) {
                try {
                    range = Long.parseLong(rangeStr);
                } catch (Exception e) {
                }
            }

            Date alarmRangeTime = new Date(System.currentTimeMillis() - range * 3600 * 1000);

            Map<String, Object> params = new HashMap<String, Object>();
            params.put("startTime", null);
            params.put("endTime", alarmRangeTime);
            params.put("status", InstanceStatus.offline.value());
            //机房+应用详情 实例状态为 0
            List<LogicDelAlarmInfo> logicDelAlarmInfoList = instanceManager.countLogicDelInsNodes(params);

            //机房+应用汇总 实例状态为 0
            Map<Integer, Map<String, LogicDelAlarmInfo>> logicDelAlarmInfoMap = new HashMap<Integer, Map<String, LogicDelAlarmInfo>>();
            //机房汇总 实例状态为 0
            Map<Integer, Long> logicDelCountMap = new HashMap<Integer, Long>();
            //机房汇总 实例状态为 0,1
            Map<Integer, Long> allInsCountMap = new HashMap<Integer, Long>();
            //需要报警的机房应用信息
            Map<Integer, Double> alarmMap = new HashMap<Integer, Double>();

            if (logicDelAlarmInfoList != null && !logicDelAlarmInfoList.isEmpty()) {
                for (LogicDelAlarmInfo logicDelAlarmInfo : logicDelAlarmInfoList) {
                    if (logicDelAlarmInfo != null && logicDelAlarmInfo.getInsRoom() != null) {
                        //按机房统计汇总逻辑删除实例数
                        Long insCount = logicDelAlarmInfo.getCount();
                        if (insCount != null) {
                            Long insRoomCount = logicDelCountMap.get(logicDelAlarmInfo.getInsRoom());
                            if (insRoomCount == null) {
                                logicDelCountMap.put(logicDelAlarmInfo.getInsRoom(), insCount);
                            } else {
                                logicDelCountMap.put(logicDelAlarmInfo.getInsRoom(), insRoomCount + insCount);
                            }
                        }

                        //按机房和应用统计逻辑删除实例数
                        String appId = logicDelAlarmInfo.getAppId();
                        if (appId == null) {
                            appId = "NoAppId";
                        }
                        Map<String, LogicDelAlarmInfo> appCountInfoMap = logicDelAlarmInfoMap.get(logicDelAlarmInfo.getInsRoom());
                        if (appCountInfoMap == null) {
                            appCountInfoMap = new HashMap<String, LogicDelAlarmInfo>();
                            appCountInfoMap.put(appId, logicDelAlarmInfo);
                            logicDelAlarmInfoMap.put(logicDelAlarmInfo.getInsRoom(), appCountInfoMap);
                        } else {
                            LogicDelAlarmInfo appCountInfo = appCountInfoMap.get(appId);
                            if (appCountInfo != null) {
                                Long mapCount = appCountInfo.getCount();
                                Long currCount = logicDelAlarmInfo.getCount();
                                if (mapCount == null) {
                                    mapCount = 0L;
                                }
                                if (currCount == null) {
                                    currCount = 0L;
                                }
                                appCountInfo.setCount(mapCount + currCount);
                                appCountInfoMap.put(appId, appCountInfo);
                            } else {
                                appCountInfoMap.put(appId, logicDelAlarmInfo);
                            }
                            logicDelAlarmInfoMap.put(logicDelAlarmInfo.getInsRoom(), appCountInfoMap);
                        }
                    }
                }
            } else {
                //没有需要逻辑删除的实例,直接返回
                return;
            }

            params.put("status", new Integer[]{InstanceStatus.offline.value(), InstanceStatus.online.value()});
            params.put("endTime", null);
            List<LogicDelAlarmInfo> allInsInfoList = instanceManager.countInsNodes(params);
            if (allInsInfoList != null && !allInsInfoList.isEmpty()) {
                for (LogicDelAlarmInfo logicDelAlarmInfo : allInsInfoList) {
                    if (logicDelAlarmInfo != null && logicDelAlarmInfo.getInsRoom() != null) {
                        Integer insRoom = logicDelAlarmInfo.getInsRoom();
                        Long insCount = logicDelAlarmInfo.getCount();
                        if (insRoom != null) {
                            if (insCount == null) {
                                insCount = 0L;
                            }
                            allInsCountMap.put(insRoom, insCount);
                        }
                    }
                }
            } else {
                //TODO
                logger.warn("Instance is Emptyl! really?");
            }

            Double threshold = 5D;
            String thresholdStr = sysParamManager.findValueBykey(logicDelAlarmThreshold);
            if (thresholdStr != null) {
                try {
                    threshold = Double.parseDouble(thresholdStr);
                } catch (Exception e) {
                    //TODO
                }
            }

            if (!logicDelCountMap.isEmpty()) {
                for (Integer insRoom : logicDelCountMap.keySet()) {
                    Long logicDelCount = logicDelCountMap.get(insRoom);
                    Long insRoomCount = allInsCountMap.get(insRoom);
                    if (logicDelCount != null && insRoomCount != null && insRoomCount.intValue() > 0) {
                        BigDecimal logicDelCountDecimal = BigDecimal.valueOf(logicDelCount);
                        BigDecimal insRoomCountDecimal = BigDecimal.valueOf(insRoomCount);
                        double percent = logicDelCountDecimal.divide(insRoomCountDecimal, 2, BigDecimal.ROUND_HALF_UP).doubleValue();
                        if (percent * 100 >= threshold) {
                            alarmMap.put(insRoom, percent);
                        }
                    }
                }
            }
            if (!alarmMap.isEmpty()) {
                logger.info("checkDeadInsToDel alarm start!");
                StringBuilder tableContent = new StringBuilder();
                for (Integer insRoom : alarmMap.keySet()) {
                    logger.info("checkDeadInsToDel alarm : insRoom = {}, over threshold = {}", insRoom, alarmMap.get(insRoom));
                    //机房信息
                    tableContent.append("<DIV class=leftArea>");
                    tableContent.append("<TABLE>")
                            .append("<TBODY>")
                            .append("<TR>")
                            .append("<TH class=textL>机房名</TH>")
                            .append("<TH class=textL>实例总数</TH>")
                            .append("<TH class=textL>预逻辑删除实例总数</TH>")
                            .append("<TH class=textL>预逻辑删除实例比率</TH>")
                            .append("</TR>")
                            .append("<TR>")
                            .append("<TD width=\"100\">")
                            .append(ComputerRoom.of(insRoom).name())
                            .append("</TD>")
                            .append("<TD width=\"100\">")
                            .append(allInsCountMap.get(insRoom))
                            .append("</TD>")
                            .append("<TD width=\"200\">")
                            .append(logicDelCountMap.get(insRoom))
                            .append("</TD>")
                            .append("<TD width=\"200\">")
                            .append(alarmMap.get(insRoom))
                            .append("</TD>")
                            .append("</TR>")
                            .append("</TBODY>")
                            .append("</TABLE>");
                    tableContent.append("</DIV>");
                    //应用信息
                    Map<String, LogicDelAlarmInfo> appCountInfoMap = logicDelAlarmInfoMap.get(insRoom);
                    if (appCountInfoMap != null && !appCountInfoMap.isEmpty()) {
                        tableContent.append("<DIV class=leftArea>");
                        tableContent.append("<TABLE>")
                                .append("<TBODY>");
                        tableContent.append("<TR>")
                                .append("<TD colspan=\"4\"L>应用信息如下:</TD>")
                                .append("<TR>");
                        tableContent.append("<TR>")
                                .append("<TH class=textL>应用ID</TH>")
                                .append("<TH class=textL>应用名</TH>")
                                .append("<TH class=textL>预逻辑删除实例总数</TH>")
                                .append("<TH class=textL>扩展信息</TH>")
                                .append("<TR>");

                        for (String appId : appCountInfoMap.keySet()) {
                            tableContent.append("<TR>")
                                    .append("<TD width=\"100\">")
                                    .append(appId)
                                    .append("</TD>")
                                    .append("<TD width=\"100\">")
                                    .append(appCountInfoMap.get(appId).getAppName())
                                    .append("</TD>")
                                    .append("<TD width=\"200\">")
                                    .append(appCountInfoMap.get(appId).getCount())
                                    .append("</TD>")
                                    .append("<TD width=\"200\">")
                                    .append("NONE")
                                    .append("</TD>")
                                    .append("</TR>");
                        }
                        tableContent.append("</TBODY>")
                                .append("</TABLE>")
                                .append("</DIV>");
                    }
                }
                String logicDelAlarmMailTemplate = FileUtil.file2String(FileUtil.getFileByClasspath("/logicDelAlarmMail.html"));
                logicDelAlarmMailTemplate = logicDelAlarmMailTemplate.replace("#{tableContent}", tableContent.toString());

                String contactsStr = sysParamManager.findValueBykey(logicDelAlarmContacts);
                String contactArray[] = null;
                if (contactsStr != null) {
                    contactArray = contactsStr.split(";");
                }
                List<String> toMailList = new ArrayList<String>();
                List<String> toSmsList = new ArrayList<String>();
                if (contactArray != null && contactArray.length > 0) {
                    for (String mailAndTelStr : contactArray) {
                        if (mailAndTelStr != null && mailAndTelStr.length() > 0) {
                            String [] mailAndTelArray = mailAndTelStr.split(":");
                            if (mailAndTelArray != null && mailAndTelArray.length >= 1) {
                                String mail = mailAndTelArray[0];
                                String tel = "";
                                if (mailAndTelArray.length >= 2) {
                                    tel = mailAndTelArray[1];
                                }
                                if (mail != null && mail.length() > 0) {
                                    toMailList.add(mail);
                                }
                                if (tel != null && tel.length() > 0) {
                                    toSmsList.add(tel);
                                }
                            }
                        }
                    }
                }
                if (!toMailList.isEmpty()) {
                    String subject = "逻辑删除前告警, 请紧急处理!";
                    String to[] = new String[toMailList.size()];
                    toMailList.toArray(to);
                    try {
                        logger.info("checkDeadInsToDel alarm sendEmail content is : {}!", logicDelAlarmMailTemplate);
                        EmailUtil.sendEmail(logicDelAlarmMailTemplate, to, null, null, subject);
                    } catch (Exception e) {
                        logger.info("checkDeadInsToDel alarm sendEmail Exception : {}!", e.getMessage(), e);
                        EmailUtil.sendEmail(logicDelAlarmMailTemplate, to, null, null, subject);
                    }
                }

                if (!toSmsList.isEmpty()) {
                    String phoneList = Joiner.on(",").skipNulls().join(toSmsList);
                    StringBuilder content = new StringBuilder("逻辑删除前告警, 请紧急处理, 详情请参考邮件信息! ");
                    for (Integer insRoom : alarmMap.keySet()) {
                        content.append("[机房: " + ComputerRoom.of(insRoom).name()).append(",");
                        content.append("预逻辑删除实例比率: " + alarmMap.get(insRoom)).append("];");
                    }
                }

                logger.info("checkDeadInsToDel alarm end!");
            } else {
                return;
            }
        } catch (Exception e) {
            logger.error("checkDeadInsToDel error:" + e.getMessage(), e);
        } finally {
        }
    }

}