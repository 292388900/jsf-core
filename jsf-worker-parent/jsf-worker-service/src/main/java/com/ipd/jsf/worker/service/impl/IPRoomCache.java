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
package com.ipd.jsf.worker.service.impl;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.ipd.jsf.worker.domain.JsfRoom;
import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.worker.dao.JsfRoomDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class IPRoomCache {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(IPRoomCache.class);

    private IPRoomCache() {

    }

    @Autowired
    private JsfRoomDAO jsfRoomDAO;

    private LoadingCache<String, List<JsfRoom>> ROOMS_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES) // 10分钟加载一次
            .maximumSize(5)
            .build(new CacheLoader<String, List<JsfRoom>>() {
                public List<JsfRoom> load(String key) throws Exception {
                    return key.equals("rooms") ? jsfRoomDAO.listAll() : null;
                }
            });

    private LoadingCache<String, String> IP_ROOM_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(15, TimeUnit.MINUTES)  // 15分钟重新计算加载一次
            .maximumSize(1024)
            .build(new CacheLoader<String, String>() {
                public String load(String ip) throws Exception {
                    return matchRoom(ip);
                }
            });

    /**
     * 查询ip
     * @param ip
     * @return
     */
    public String getRoomByIp(String ip) {
        try {
            if (ip == null) {
                return ComputerRoom.Default.name();
            }
            return IP_ROOM_CACHE.get(ip);
        } catch (ExecutionException e) {
            LOGGER.error("Read room from ip_room_cache error", e);
            return matchRoom(ip);
        }
    }

    /**
     *
     * @param ip
     * @return
     */
    private String matchRoom(String ip) {
        try {
            List<JsfRoom> rooms = ROOMS_CACHE.get("rooms");
            if (rooms != null && rooms.size() > 0) {
                for (JsfRoom room : rooms) {
                    if (match(room.getIpRegular(), ip)) {
                        return ComputerRoom.of(room.getRoom()).name();
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return ComputerRoom.Default.name();
    }

    private boolean match(String expression, String str){
        Pattern p = Pattern.compile(expression);
        Matcher m = p.matcher(str);
        boolean b = m.matches();
        return b;
    }
}
