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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.registry.domain.JsfIns;

/**
 * 获取当前连接数
 */
public class ConnectionRecoder {
    //连接数计数总器
    private static AtomicInteger connectionTotalCount = new AtomicInteger(0);
    //客户端连接地址map, key:远程ip, value: 远程ip调用本地的连接信息
    private static ConcurrentHashMap<String, String> connMap = new ConcurrentHashMap<String, String>(64, 0.75f, 64);
    //客户端连接地址map, key:远程ip, value: insKey
    private static ConcurrentHashMap<String, JsfIns> connInsMap = new ConcurrentHashMap<String, JsfIns>(64, 0.75f, 64);

    /**
     * 获取连接总数
     * @return
     */
    public static int getConnectionTotalCount() {
        return connectionTotalCount.get();
    }

    /**
     * 增加连接总数
     * @return
     */
    public static int increaseConnection() {
        return connectionTotalCount.incrementAndGet();
    }

    /**
     * 减少连接总数
     * @return
     */
    public static int decreaseConnection() {
        return connectionTotalCount.decrementAndGet();
    }

    /**
     * 记录长连接信息
     * @param key
     * @param value
     */
    public static void putConnContext(String key, String value) {
        if (key != null && !key.isEmpty()) {
            connMap.put(key, value);
        }
    }

    /**
     * 删除长连接信息
     * @param key
     */
    public static void removeConnContext(String key) {
        connMap.remove(key);
    }

    /**
     * 获取所有长连接信息
     * @return
     */
    public static List<String> getAllConnContext() {
        return new ArrayList<String>(connMap.values());
    }

    /**
     * @param connInsMap the connInsMap to set
     */
    public static void setConnInsMap(String key, JsfIns ins) {
        if (key != null && !key.isEmpty()) {
            connInsMap.put(key, ins);
        }
    }

    /**
     * 删除长连接信息
     * @param key
     */
    public static JsfIns removeConnIns(String key) {
        return connInsMap.remove(key);
    }

}
