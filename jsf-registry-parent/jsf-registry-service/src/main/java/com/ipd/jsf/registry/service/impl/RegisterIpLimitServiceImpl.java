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
package com.ipd.jsf.registry.service.impl;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.service.RegisterIpLimitService;

@Service
public class RegisterIpLimitServiceImpl implements RegisterIpLimitService {
    private Logger logger = LoggerFactory.getLogger(RegisterIpLimitServiceImpl.class);
    //是否需要ip访问数量检查
    private boolean checkFlag = true;
    //取5分钟内ip注册访问量
    private byte minuteLimit = 5;
    //ip在MINUTELIMIT内的访问次数
    private int visitLimit = 100;
    private float loadFactor = 0.75f;

    /*ip访问数量限制，保存ip近5分钟的数据*/
    private ConcurrentHashMap<String, ConcurrentHashMap<Byte, AtomicInteger>> ipVisitLimitMap = new ConcurrentHashMap<String, ConcurrentHashMap<Byte, AtomicInteger>>();

    private int concurrencyLevel = 1;

    @PostConstruct
    public void init() {
        while (concurrencyLevel < minuteLimit) {
            concurrencyLevel <<= 1;
        }
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.registry.service.RegisterLimitService#checkIpVisitLimit(java.lang.String)
     */
    @Override
    public boolean checkIpVisitLimit(String ip) {
        //如果不需要检查，就返回true
        if (!checkFlag || ip == null || ip.isEmpty()) {
            return true;
        }
        byte currentMinute = 0;
        try {
            currentMinute = (byte) ((System.currentTimeMillis() / (60000)) % minuteLimit);
            int visitCount = 0;
            if (ipVisitLimitMap.get(ip) == null) {
                ipVisitLimitMap.put(ip, new ConcurrentHashMap<Byte, AtomicInteger>(concurrencyLevel, loadFactor, concurrencyLevel));
            }
            if (ipVisitLimitMap.get(ip).get(currentMinute) == null) {
                ipVisitLimitMap.get(ip).put(currentMinute, new AtomicInteger(0));
            }
            //对当前ip，在当前分钟的访问进行计数
            ipVisitLimitMap.get(ip).get(currentMinute).incrementAndGet();

            for (AtomicInteger value : ipVisitLimitMap.get(ip).values()) {
                if (value != null) {
                    visitCount += value.get();
                }
            }
            if (visitCount < visitLimit) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage() + ", ip:" + ip + ", " + visitLimit + "/" + minuteLimit + " per minute, current:" + currentMinute + ", map:" + ipVisitLimitMap.get(ip), e);
            //有异常就返回true, 以免影响注册
            return true;
        }
        logger.info("+++++++++++check ip {} is up to limit {}/{} per minute, current:{} map:{}", ip, visitLimit, minuteLimit, currentMinute, ipVisitLimitMap.get(ip));
        return false;
    }

    /**
     * 清除统计数据，到每分钟的第1秒时，清除上一轮的统计数据
     */
    @Override
    public void eraseVisitCount() {
        //如果不需要检查，就返回
        if (!checkFlag) {
            if (!ipVisitLimitMap.isEmpty()) {
                ipVisitLimitMap.clear();
            }
            return;
        }
        //判断当前分钟是否第0秒
        byte currentSecond = (byte) ((System.currentTimeMillis() / (1000)) % 60);
        if (currentSecond == 0) {
            //获取当前分钟的取余
            byte currentMinute = (byte) ((System.currentTimeMillis() / (60 * 1000)) % minuteLimit);
            for (Map<Byte, AtomicInteger> map : ipVisitLimitMap.values()) {
                if (map.get(currentMinute) != null) {
                    //置为0
                    map.get(currentMinute).set(0);
                }
            }

            boolean hasValue = false;
            Iterator<Entry<String, ConcurrentHashMap<Byte, AtomicInteger>>> iterator = ipVisitLimitMap.entrySet().iterator();
            // 删除ip访问次数都为0的值
            while (iterator.hasNext()) {
                hasValue = false;
                Entry<String, ConcurrentHashMap<Byte, AtomicInteger>> entry = iterator.next();
                for (AtomicInteger value : entry.getValue().values()) {
                    //如果有值，就跳过
                    if (value.get() > 0) {
                        hasValue = true;
                        break;
                    }
                }
                //ip的访问次数都为0，就删除这个ip
                if (!hasValue) {
                    logger.info("+++++++++++erase visit ip:{}", entry.getKey());
                    iterator.remove();
                }
            }
            logger.info("+++++++++++erase visit count map:{}", ipVisitLimitMap.toString());
        }
    }

    /**
     *
     * @param checkFlag
     * @param visitLimit
     * @param minuteLimit
     */
    public void init(boolean checkFlag, int visitLimit, byte minuteLimit) {
        this.checkFlag = checkFlag;
        this.visitLimit = visitLimit;
        this.minuteLimit = minuteLimit;
    }

    public static void main(String[] args) {
        System.out.println((byte) ((System.currentTimeMillis() / (60 * 1000)) % 5));
        Byte value = 127;
        System.out.println(value.intValue());
        Calendar a = Calendar.getInstance();
        System.out.println(a.get(Calendar.MINUTE));
        System.out.println((byte) ((System.currentTimeMillis() / (60 * 1000)) % 5));
        System.out.println(((System.currentTimeMillis() / (1000)) % 60));
    }
}
