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

import com.alibaba.fastjson.JSONObject;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.domain.ServiceTraceLog;
import com.ipd.jsf.worker.log.dao.ServiceTraceLogDao;
import com.ipd.jsf.worker.service.common.utils.StringUtils;
import com.ipd.jsf.worker.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Title: OnOffStatisticsAlarmWorker 上下线次数统计报警worker，次数超过阀值发送报警短信
 * Description: OnOffStatisticsAlarmWorker
 */
public class OnOffStatisticsAlarmWorker extends SingleWorker {

    private static Logger logger = LoggerFactory.getLogger(OnOffStatisticsAlarmWorker.class);

    @Autowired
    private ServiceTraceLogDao serviceTraceLogDao;

    private static String alarmLastTime = null; //上次报警时间。用来实现每天只报警一次的逻辑

    private int onOffThreshold = 1000;//Integer.parseInt(PropertyFactory.getProperty("on.off.statistics.alarm.threshold", "1000"));//上下线阈值

    //报警接收人手机
    private final String phones = (PropertyUtil.getProperties("OnOffStatisticsAlarmWorker.alarm.phone") == null) ? "17001122339,18610145326,18911626329,13522657085" : PropertyUtil.getProperties("OnOffStatisticsAlarmWorker.alarm.phone");

    private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public boolean run() {

        try {
            String startTime = sdf.format(new Date());//今天
            String endTime = sdf.format(new Date().getTime() + 24 * 60 * 60 * 1000);//加一天
            logger.info("上下线次数统计报警worker OnOffStatisticsAlarmWorker 执行。 startTime:" + startTime + "endTime:" + endTime);
            if (startTime.equals(alarmLastTime)) {
                logger.info("今天已经报过警了，明天再报警");
                return true;
            }

            JSONObject workParams = getWorkerParameters();//从管理端上配置的worker参数中取值
            if (workParams != null) {
                logger.info("json值：" + workParams.toJSONString());
                String parameterStr = workParams.getString("onOffThreshold");
                if (!StringUtils.isEmpty(parameterStr)) {
                    onOffThreshold = Integer.parseInt(parameterStr);
                }
            }
            logger.info("上下线阈值 onOffThreshold:" + onOffThreshold);

            int maxCount = serviceTraceLogDao.getMaxCount(startTime, endTime);//最大的上下线次数
            logger.info("当前上下线最大值：" + maxCount);
            if (maxCount >= onOffThreshold) {//过了阀值了
                List<ServiceTraceLog> list = (List<ServiceTraceLog>) serviceTraceLogDao.getTopRecords(startTime, endTime, maxCount);
                if (list != null && !list.isEmpty()) {
                    ServiceTraceLog serviceTraceLog = list.get(0);//取一个就行了
                    StringBuilder sb = new StringBuilder();
                    sb.append("[上下线次数报警] 接口：");
                    sb.append(serviceTraceLog.getInterfaceName());
                    sb.append(" IP：");
                    sb.append(serviceTraceLog.getIp());
                    sb.append(" 上下线次数：");
                    sb.append(serviceTraceLog.getCount());
                    sb.append(" 超过报警阀值（今天不会再报警）");
                    logger.info("报警发送短信： " + sb.toString());

                    alarmLastTime = startTime;//记录报警时间
                }
            } else {
                logger.info("最大山线下记录数：" + maxCount + "没有超过报警阀值。");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return true;
    }

    @Override
    public String getWorkerType() {
        return "onOffStatisticsAlarmWorker";
    }
}