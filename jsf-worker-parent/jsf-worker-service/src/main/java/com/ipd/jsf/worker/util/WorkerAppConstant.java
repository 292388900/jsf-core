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
package com.ipd.jsf.worker.util;

public class WorkerAppConstant {

	/**
	 * db与ZK节点同步worker开关：局部的方法，单向同步zk-provider节点到db的开关
	 */
	public static final String FUN_WORKER_SYN_DB_PROVIDER = "fun.worker.syn2db.provider";

	/**
	 * db与ZK节点同步worker开关：局部的方法，单向同步zk-Consumer节点到db的开关
	 */
	public static final String FUN_WORKER_SYN_DB_CONSUMER = "fun.worker.syn2db.consumer";

	/**
	 * db与ZK节点同步worker开关：局部的方法，单向同步db-provider节点到Zookeeper的开关
	 */
	public static final String FUN_WORKER_SYN_ZK_PROVIDER = "fun.worker.syn2zk.provider";
	
	/**
	 * 实例上下线次数key
	 */
	public final static String SAF_TRACELOG_THRESHOLD_KEY = "saf_tracelog_threshold";
	
	/**
	 * 接口上下线次数key
	 */
	public final static String SAF_TRACELOG_THRESHOLD_IFKEY = "saf_tracelog_threshold_if";
	
	/**
	 * 管理员erp
	 */
	public final static String ALARM_ADMIN_ADDRESS = "alarm.admin.erp";

	/**
	 * 上下线报警统计ump_key
	 */
	
	public final static String SAF_ONOFF_STAT_ALARMKEY = "saf_tracelog_alarm";
	
	/**
	 * 注册中心存活报警ump_key
	 */
	
	public final static String REGISTRY_STSTUS_ALARMKEY = "registry_status_alarm";
	
	/**
	 * 阀值报警
	 */
	
	public final static String JSF_INS_THRESHOLD_ALARMKEY = "service_threshold_value";
	
	/**
	 * 关于报警worker的报警状检查
	 */
	public final static String ALARM_WORKER_CHECKI_ALARMKEY = "jsf_alarmservice_check";

	//telnet连接超时时间
	public final static String TELNET_CONN_TIMEOUT = "telnet.conn.timeout";

	//telnet读取超时时间
	public final static String TELNET_READ_TIMEOUT = "telnet.read.timeout";

	//telnet线程池数量
	public final static String TELNET_THREAD_CNT = "telnet.thread.cnt";
	
	//es的ip地址
	public final static String SCAN_ES_IPS = "scan.es.ips";
	
	//es的port
	public final static String SCAN_ES_PORT = "scan.es.port";
	
	//es的集群名
	public final static String SCAN_ES_CLUSTERNAME = "scan.es.clustername";
	
	//es的索引名
	public final static String SCAN_ES_INDEXNAME = "scan.es.indexname";
	
	//es的类型名
	public final static String SCAN_ES_TYPENAME = "scan.es.typename";

	//es的用户名
	public final static String SCAN_ES_ACCESS_KEY = "scan.es.accessKey";

	//es的密码
	public final static String SCAN_ES_ACCESS_PWD = "scan.es.accessPwd";

	public final static String SCAN_ES_INDEX_PORT = "scan.es.indexPort";
}
