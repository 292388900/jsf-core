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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.registry.domain.Room;
import com.ipd.jsf.registry.manager.RoomManager;
import com.ipd.jsf.registry.service.ConfigService;

@Service
public class ConfigServiceImpl implements ConfigService {
    private Logger logger = LoggerFactory.getLogger(ConfigServiceImpl.class);
    //机房信息缓存
    private volatile List<Room> roomListCache = new ArrayList<Room>();

    @Autowired
    private RoomManager roomManagerImpl;

    @Override
    public void refreshCache() {
        try {
            loadRoom();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 取出机房信息
     * @throws Exception
     */
    private void loadRoom() throws Exception {
        List<Room> list = roomManagerImpl.getList();
        if (list != null && list.size() > 0) {
            boolean find = false;
            for (Room newRoom : list) {
                find = false;
                if (!roomListCache.isEmpty()) {
                    for (Room room : roomListCache) {
                        if (room.getIpRegular().equals(newRoom.getIpRegular())) {
                            //如果正则表达式相同，用内存里已经创建好的Pattern
                            newRoom.p = room.p;
                            find = true;
                            break;
                        }
                    }
                }
                //如果有新的正则表达式，就在内存创建一个Pattern
                if (!find && newRoom.getIpRegular() != null && !newRoom.getIpRegular().isEmpty()) {
                    newRoom.p = Pattern.compile(newRoom.getIpRegular());
                }
            }
            synchronized (roomListCache) {
                roomListCache = list;
            }
        }
    }

    @Override
    public int getRoomByIp(String ip) {
        return getComputerRoomByIP(roomListCache, ip);
    }

    /**
     * 根据IP获取机房信息
     * @param rooms
     * @param ip
     * @return
     */
    private int getComputerRoomByIP(List<Room> rooms, String ip) {
        try {
            if (rooms != null && rooms.size() > 0) {
                for (Room room : rooms) {
                    if (match(room.p, ip)) {
                        return room.getRoom();
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }

        return ComputerRoom.Default.value();
    }

    private boolean match(Pattern p, String str) {
        if (p != null) {
            Matcher m = p.matcher(str);
            boolean b = m.matches();
            return b;
        }
        return false;
    }
}
