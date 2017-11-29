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

package com.ipd.jsf.common.enumtype;

public enum AlarmType {

    SERVERONOFF("服务上下线", 0),
    PROVIDERTHRESHOLD("provider阀值", 1),
    CONSUMERTHRESHOLD("consumer阀值", 2),
    INVOKETHRESHOLD("方法调用次数阀值", 3),
    IFACEONOFFSTAT("服务上下线统计", 4),
    REGISTRYALIVE("注册中心存活", 5),
    INSTANCEONOFF("实例上下线", 6),
    WORKEREXECUTEFAILED("worker执行失败", 7),
    MONITORSTATUS("监控服务端状态", 8),
    NOINSPROVIDERALIVE("provider无实例但存活", 9),
    MORELOSSCONSUMER("consumer下线过多", 10),
    SERVICEPCALLTIMIEALARM("PROVIDER 调用次数报警", 11),
    SERVICEERRORALARM("PROVIDER 异常次数报警", 12),
    SERVICETPSALARM("PROVIDER 调用耗时报警", 13),
    JSFHTTPGWALARM("JSF Http GW 异常次数报警", 14),
    ALARMWORKERCHECK("报警WORKER状态", 15),
    //Deprecated
    CONSUMERERRORALARM("CONSUMER 异常次数报警", 16),
    CONSUMERTPSALARM("CONSUMER 调用耗时报警", 17),
    LDSALARM("LDS与DB数据不一致报警", 18),
    JIESIALARM("杰思ES服务报警", 19),
    //zk报警
    ZKALIVE("zk节点存活 异常次数报警", 21),
    ZKOUTSTANDING("zk节点积压 异常次数报警", 22);

    private String name;
    private int value;

    private AlarmType(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public int getValue() {
        return value;
    }

    public static AlarmType fromName(String name) {
        for (AlarmType type : AlarmType.values()) {
            if (type.name().equals(name))
                return type;
        }
        return null;
    }

    public static AlarmType fromValue(int value) {
        for (AlarmType type : AlarmType.values()) {
            if (type.value == value)
                return type;
        }
        return null;
    }

}