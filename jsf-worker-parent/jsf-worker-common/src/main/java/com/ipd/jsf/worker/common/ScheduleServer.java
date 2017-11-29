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
package com.ipd.jsf.worker.common;

import com.alibaba.fastjson.JSONObject;
import com.ipd.jsf.zookeeper.common.NetUtils;
import com.ipd.jsf.zookeeper.common.StringUtils;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.UnknownHostException;
import java.util.UUID;

/**
 * 调度Server信息
 *
 */
public class ScheduleServer {

    /**
     * pid$ip$uuid
     */
    private String id;

    private String ip;

    private boolean isRegister;

    private String workerType;

    /**
     * worker 相关的具体业务参数
     */
    private JSONObject workerParameters;

    /**
     * 调度server是否启动,默认启动
     */
    private boolean start = true;



    /**
     * 创建调度server
     *
     * @param worker
     * @return
     */
    public static ScheduleServer createScheduleServer(Worker worker){
        ScheduleServer scheduleServer = new ScheduleServer();
        scheduleServer.workerType = worker.getWorkerType();
        scheduleServer.workerParameters = worker.getWorkerParameters();
        //0 代表开机启动
        if ( "0".equals(worker.status())){
            scheduleServer.setStart(true);
        } else {
            scheduleServer.setStart(false);
        }
        try {
            scheduleServer.ip = NetUtils.getLocalAddress().getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
        }
        scheduleServer.id = getPid()+"$"+scheduleServer.ip + "$"+ (UUID.randomUUID().toString().replaceAll("-", "")
                .toUpperCase());

        return scheduleServer;

    }

    /**
     *
     * @param id
     * @return 从server id 中解析出来IP地址
     */
    public static String getIpFromID(String id){
        if (StringUtils.isBlank(id)){
            return null;
        }
        if ( id.indexOf("$") > 0 ){
            return id.split("$")[0];
        } else {
            return id;
        }
    }

    public static int getPid() {
        int PID = -1;
        if (PID < 0) {
            try {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                String name = runtime.getName(); // format: "pid@hostname"
                PID = Integer.parseInt(name.substring(0, name.indexOf('@')));
            } catch (Throwable e) {
                PID = 0;
            }
        }
        return PID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isRegister() {
        return isRegister;
    }

    public void setRegister(boolean isRegister) {
        this.isRegister = isRegister;
    }

    public String getWorkerType() {
        return workerType;
    }

    public void setWorkerType(String workerType) {
        this.workerType = workerType;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public JSONObject getWorkerParameters() {
        return workerParameters;
    }

    public void setWorkerParameters(JSONObject workerParameters) {
        this.workerParameters = workerParameters;
    }

    public boolean isStart() {
        return start;
    }

    public void setStart(boolean isWorking) {
        this.start = isWorking;
    }
}
