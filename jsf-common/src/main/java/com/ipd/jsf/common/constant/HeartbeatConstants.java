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

package com.ipd.jsf.common.constant;

public class HeartbeatConstants {

    /**
     * 注册中心相关-注册中心心跳<br>
     * 默认30秒<br>
     */
    public final static int              REGISTRY_HEARTBEAT_PERIOD = 30000;

    /**
     * 判断实例死亡时间（以注册中心心跳的倍数进行判断）<br>
     * 默认120秒<br>
     */
    public final static long              JUDGE_DEAD_INSTANCE_TIME = REGISTRY_HEARTBEAT_PERIOD * 2 * 2;

    /**
     * 实例死亡后，从数据库逻辑删除数据时间（以注册中心心跳的倍数进行判断）<br>
     * 默认5分钟<br>
     */
    public final static long              DELETE_DEAD_INSTANCE_FROM_DB_TIME_DEFAULT = REGISTRY_HEARTBEAT_PERIOD * 2 * 60 * 8;
    
    /**
     * 实例死亡后，从数据库逻辑删除数据时间（以注册中心心跳的倍数进行判断）<br>
     * 默认5分钟<br>
     */
    public static long              DELETE_DEAD_INSTANCE_FROM_DB_TIME = DELETE_DEAD_INSTANCE_FROM_DB_TIME_DEFAULT;

    /**
     * provider/consumer死亡后，从数据库删除数据时间（以注册中心心跳的倍数进行判断）<br>
     * 6分钟<br>
     */
//    public static long              DELETE_DEAD_NODE_FROM_DB_TIME = REGISTRY_HEARTBEAT_PERIOD * 4 * 6;

    /** 注册中心心跳间隔时间 */
    public final static long REG_HB_INTERVAL = 30 * 1000L;

    /** 删除注册中心心跳历史数据时间 */
    public final static long DEL_REG_HB_HISTORY_PERIOD = 1 * 24 * 3600 * 1000L;

}
