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
package com.ipd.jsf.registry.recoder;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IpRequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(IpRequestHandler.class);
    //超过interval间隔未更新，就从map中删掉
    private static final int interval = 3 * 60 * 1000;
    public static ConcurrentHashMap<String, IpRequestRecorder> ipRecorderMap = new ConcurrentHashMap<String, IpRequestRecorder>(64, 0.75f, 64);

    /**
     * 心跳数加一
     * @param ip
     */
    public static void hbRecord(String ip) {
        try {
            put(ip);
            ipRecorderMap.get(ip).hbCount.increment();
            //只有当心跳时，记录最后一次更新时间
            ipRecorderMap.get(ip).lastUpdateTime = System.currentTimeMillis();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 注册数加一
     * @param ip
     */
    public static void registryRecord(String ip) {
        try {
            put(ip);
            ipRecorderMap.get(ip).registryCount.increment();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 订阅数加一
     * @param ip
     */
    public static void subscribeRecord(String ip) {
        try {
            if (ip == null || ip.isEmpty()) {
                return;
            }
            put(ip);
            ipRecorderMap.get(ip).subscribeCount.increment();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 每隔2分钟就计算一次
     */
    public static void calAllCount() {
        try {
            Iterator<Entry<String, IpRequestRecorder>> iterator = ipRecorderMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, IpRequestRecorder> entry = iterator.next();
                //如果超过一段时间没更新，就删除掉
                if (System.currentTimeMillis() - entry.getValue().lastUpdateTime > interval) {
                    iterator.remove();
                    continue;
                }
                entry.getValue().calCount();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private static void put(String ip) {
        if (!ipRecorderMap.containsKey(ip)) {
            ipRecorderMap.put(ip, new IpRequestRecorder());
        }
    }
}
