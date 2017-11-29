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
package com.ipd.jsf.worker.thread.scanstatus;

import com.alibaba.fastjson.JSONObject;
import com.ipd.jsf.worker.service.ScanNodeStatusService;
import com.ipd.jsf.worker.vo.ByRoomResult;

import org.apache.commons.lang.time.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.thread.checkdb.CheckDBWorker;

import java.util.*;

/**
 * 实例、server、client状态扫描worker
 */
public class ScanNodeStatusWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(ScanNodeStatusWorker.class);

    @Autowired
    private ScanNodeStatusService scanNodeStatusService;

    private volatile boolean isRunning = false;

    @Override
    public boolean run() {
        if (isRunning) {
            logger.info("ScanInsStatusWorker is running now, the task is over...");
            return true;
        }
        if (!CheckDBWorker.isDBOK) {
            logger.info("can not connet to mysql, the task is over...");
            return true;
        }
        logger.info("Scanning status of servers and clients...");
        StopWatch clock = new StopWatch();
        try {
            clock.start(); // 计时开始
            isRunning = true;

            ByRoomResult result = isByRoom();

            if (result.isByRoom() && !checkRoomsConfig()) {
                logger.info("by room is true, config room info invalid, the task is over...");
                //TODO send alarm
                return true;
            } else if (!result.isByRoom() && !result.isMatchWorkerType()) {
                logger.info("by room is false and worker's alias no match with workerType, the task is over...");
                //TODO send alarm
                return true;
            }

            scanNodeStatusService.scanStatus();
        } catch (Exception e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行", e);
        } catch (Throwable e) {
            logger.error("异常终止。执行心跳时间扫描定时器出现异常，等待下次执行(Throwable)", e);
        } finally {
            isRunning = false;
            clock.stop(); // 计时结束
        }
        logger.info("The end of scanning status of servers and clients... it elapse time: " + clock.getTime() + " ms");
        return true;
    }

    /**
     * 是否按机房分任务,忽略的化必须配置有哪个别名的workerType执行任务
     * @return
     */
    private ByRoomResult isByRoom() throws Exception {
        return scanNodeStatusService.isByRoom(getWorkerType());
    }

    /**
     * 验证机房参数有效性
     * @return
     */
    private boolean checkRoomsConfig() throws Exception {
        JSONObject params = getWorkerParameters();
        if (params == null) {
            logger.info("no config room info or config error, the task is over...");
            return false;
        }
        String rooms = params.getString("rooms");
        boolean valid = true;
        if (rooms == null || rooms.isEmpty()) {
            valid = false;
        }

        String[] roomArray = rooms.split(",");

        if (roomArray == null || roomArray.length == 0) {
            valid = false;
        }

        Set<Integer> roomSet = new HashSet<Integer>();

        for (String room : roomArray) {
            try {
                Integer roomNum = Integer.parseInt(room);
                if (ComputerRoom.of(roomNum) == null) {
                    logger.info("no config room info or config error, the task is over...");
                    valid = false;
                    break;
                } else {
                    valid = true;
                    roomSet.add(roomNum);
                }
            } catch (Exception e) {
                logger.info("no config room info or config error, the task is over...", e);
                valid = false;
                break;
            }
        }

        if (valid) {
            scanNodeStatusService.setCurrTaskRooms(new ArrayList<Integer>(roomSet));
            logger.info("by room config rooms for : {}", rooms);
        } else {
            scanNodeStatusService.setCurrTaskRooms(null);
            roomSet.clear();
            roomSet = null;
        }

        return valid;
    }

    /**
    @Override
    public String getWorkerType() {
        return "scanNodeStatusWorker";
    }
    */
}

