CREATE DATABASE IF NOT EXISTS saf_registry CHARACTER SET utf8;
use saf_registry;

CREATE TABLE `saf_interface_alarm` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`interfaceId` INT(11) NULL DEFAULT NULL COMMENT '接口id',
	`interfaceName` VARCHAR(150) NOT NULL COMMENT '接口名' COLLATE 'utf8_bin',
	`alias` VARCHAR(255) NULL COMMENT '接口别名',
	`method` VARCHAR(255) NULL DEFAULT NULL COMMENT '方法 ',
	`alarmIp` VARCHAR(300) NULL DEFAULT NULL COMMENT '负责ip列表，多个用逗号隔开' COLLATE 'utf8_unicode_ci',
	`alarmType` TINYINT(4) NULL DEFAULT NULL COMMENT '报警类型(0:上下线；1:provider阀值；2:consumer阀值)',
	`threshold` INT(10) NULL DEFAULT NULL COMMENT '报警阀值（alarmType为1和2时有效）',
	`stat_interval` INT(10) NULL DEFAULT NULL COMMENT '统计时间间隔',
	`app_id` INT(10) NULL DEFAULT NULL COMMENT '自动部署中APP的ID',
	`mailAlarm` TINYINT(1) NULL DEFAULT NULL COMMENT '是否邮件报警',
	`mmsAlarm` TINYINT(1) NULL DEFAULT NULL COMMENT '是否短信报警',
	`restartAlarm` TINYINT(1) NULL DEFAULT '0' COMMENT '是否重启报警',
	`createdTime` DATETIME NULL DEFAULT NULL COMMENT '创建时间',
	`updateTime` DATETIME NULL DEFAULT NULL COMMENT '更新时间',
	`alarmUser` VARCHAR(250) NULL DEFAULT NULL COMMENT '该调报警关联的用户，多个用逗号做分隔' COLLATE 'utf8_unicode_ci',
	`pcType` INT(10) NULL DEFAULT NULL COMMENT 'alarmType为上下线时有效。1provider;0consmer',
	PRIMARY KEY (`id`)
)
COMMENT='接口报警表';

/*CREATE TABLE `saf_method_alarm` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，无意义',
  `interfaceId` varchar(150) NOT NULL COMMENT '接口ID',
  `interfaceName` varchar(150) NOT NULL COMMENT '接口名',
  `methodName` varchar(50) NOT NULL COMMENT '方法名',
  `alarmType` int(20) DEFAULT NULL COMMENT '异常类型ID列表',
  `threshold` int(10) DEFAULT NULL COMMENT '每分钟最大调用次数',
  `pins` varchar(30) NOT NULL COMMENT '报警对象,需要发送报警信息的ERP账号列表',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) COMMENT='警报-方法关联表';*/

CREATE TABLE `saf_synzkdb_log` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `node_log` varchar(512) NOT NULL,
  `interface_name` varchar(200) DEFAULT NULL,
  `log_type` varchar(16) NOT NULL,
  `creator` varchar(32) NOT NULL,
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

CREATE TABLE `saf_service_tracelog` (
`id` int(10) NOT NULL AUTO_INCREMENT,
`interfaceName` varchar(200) Binary DEFAULT NULL COMMENT '接口名字',
`alias` varchar(64) Binary DEFAULT NULL COMMENT '别名',
`ip` varchar(32) DEFAULT NULL COMMENT 'ip地址',
`port` int(6) DEFAULT NULL COMMENT '端口',
`protocol` varchar(16) DEFAULT NULL COMMENT '协议',
`pid` int(6) DEFAULT NULL COMMENT '进程id',
`safVer` int(10) DEFAULT NULL COMMENT 'saf版本',
`pcType` tinyint(1) DEFAULT NULL COMMENT '是否服务端 1服务端0客户端',
`onoffType` tinyint(1) DEFAULT NULL COMMENT 'saf上下线 0下线 1上线',
`creator` varchar(32) DEFAULT NULL COMMENT '创建者',
`createdTime` datetime DEFAULT NULL COMMENT '创建时间',
`eventTime` datetime NOT NULL COMMENT '上下线时间',
PRIMARY KEY (`id`),
KEY `iface_idx` (`interfaceName`),
KEY `iface_ip_idx` (`interfaceName`,`ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口上下线记录表';

CREATE TABLE `saf_registry_request` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `cluster_id` int(11) NOT NULL COMMENT '集群id',
  `ip` varchar(100) NOT NULL COMMENT 'ip',
  `port` int(11) NOT NULL COMMENT 'port',
  `request_type` varchar(100) NOT NULL COMMENT 'method',
  `request` int(11) NOT NULL COMMENT '请求数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) COMMENT='注册中心请求数';

CREATE TABLE `saf_registry_stat_history` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `cluster_id` int(11) NOT NULL COMMENT '所属集群id',
  `ip` varchar(100) NOT NULL COMMENT 'ip',
  `port` int(11) NOT NULL COMMENT 'port',
  `connections` int(11) NOT NULL COMMENT '链接数',
  `callbacks` int(11) NOT NULL COMMENT '监控数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) comment '注册中心监控数';

/*CREATE TABLE `saf_tracelog_stat` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `interface_name` varchar(255) NOT NULL COMMENT '接口名',
  `host` varchar(100)  NOT NULL COMMENT 'IP',
  `port` int(11) DEFAULT NULL COMMENT '端口',
  `is_provider` int(11) NOT NULL COMMENT '是否为提供端',
  `is_online` int(11) NOT NULL COMMENT '是否为上线',
  `stat_count` int(11) NOT NULL DEFAULT '0' COMMENT '数量',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx` (`interface_name`)
) comment '服务上下线统计表';*/

CREATE TABLE `saf_registry_addr` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`ip` VARCHAR(20) NOT NULL COMMENT 'ip',
	`port` INT(5) NOT NULL COMMENT '端口',
	`protocol` VARCHAR(20) NOT NULL COMMENT '协议',
	`last_check_time` DATETIME NOT NULL COMMENT '启动时间',
	`state` TINYINT(11) NOT NULL COMMENT '注册中心实例状态',
	`note` VARCHAR(200) NULL DEFAULT NULL COMMENT '备注',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	`saf_index_id` INT(11) UNSIGNED ZEROFILL NOT NULL COMMENT 'saf_index id',
	`room` VARCHAR(20) NOT NULL DEFAULT '亦庄',
	`conns` INT(11) NULL DEFAULT NULL COMMENT '长连接数',
	`requests` INT(11) NULL DEFAULT NULL COMMENT '请求数',
	`callbacks` INT(11) NULL DEFAULT NULL COMMENT '回调数',
	`isValid` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '是否有效。0:无效,1:有效',
  `env` ENUM('test','online','release') NOT NULL COMMENT '应用环境',
  `logic_del_flag` TINYINT(2) NOT NULL DEFAULT 1 COMMENT '是否忽略逻辑删除, 1:不忽略,0:忽略',
	PRIMARY KEY (`id`)
)ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

CREATE TABLE `saf_instance_stat` (
  `id` bigint(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `weekend` int(5) NOT NULL COMMENT '第几周',
  `p_instance` int(11) NOT NULL COMMENT 'provider实例数',
  `c_instance` int(11) NOT NULL COMMENT 'consumer实例数',
  `total_instance` int(11) NOT NULL COMMENT '实例总数',
  `ips` int(5) NOT NULL COMMENT 'IP数',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `p_ips` int(5) NOT NULL,
  `c_ips` int(5) NOT NULL,
  `total_ins_add` int(20) NOT NULL,
  `total_ip_add` int(11) NOT NULL,
  `p_ins_add` int(20) NOT NULL,
  `c_ins_add` int(20) NOT NULL,
  `p_ip_add` int(11) NOT NULL,
  `c_ip_add` int(11) NOT NULL,
  PRIMARY KEY (`id`)
) COMMENT 'saf实例总数表';

CREATE TABLE `saf_alarm_history` (
	`id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`alarm_key` VARCHAR(255) NOT NULL COMMENT '报警约定key',
	`interface_name` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '接口名。为空时\'\'',
	`alarm_ip` VARCHAR(50) NULL DEFAULT '\'\'' COMMENT '报警ip',
	`method_name` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '方法名。为空时\'\'',
	`content` VARCHAR(1024) NOT NULL COMMENT '报警内容（以邮件为准）' COLLATE 'utf8_unicode_ci',
	`erps` VARCHAR(255) NOT NULL COMMENT '收件人erp。分号隔开' COLLATE 'utf8_unicode_ci',
	`alarm_type` TINYINT(3) UNSIGNED NOT NULL COMMENT '报警类型（代码中枚举定义）',
	`is_alarmed` TINYINT(4) UNSIGNED NOT NULL DEFAULT '0' COMMENT '是否已报警。0：否；1：是',
	`create_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
	`alarm_time` TIMESTAMP NULL DEFAULT NULL COMMENT '报警时间',
	`remarks` VARCHAR(1024) NULL DEFAULT '\'\'' COMMENT '备注。短信发送成功后记录短信接收号码',
	`extend_key1` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '扩展排重字段1',
	`extend_key2` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '扩展排重字段2',
	`reg_ip` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '注册中心ip(可选)',
	PRIMARY KEY (`id`),
	INDEX `idx_create_time` (`create_time`, `is_alarmed`),
	INDEX `idx_alarm_key` (`alarm_key`)
)
 COLLATE='utf8_general_ci' ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE `saf_alarm_setting` (
	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`alarm_key` VARCHAR(255) NOT NULL COMMENT '报警key',
	`interface_name` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '接口名',
	`method_name` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '方法名',
	`alarm_type` TINYINT(3) UNSIGNED NOT NULL COMMENT '报警类型',
	`extend_key1` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '扩展排重字段1',
	`extend_key2` VARCHAR(255) NULL DEFAULT '\'\'' COMMENT '扩展排重字段2',
	`alarm_desc` VARCHAR(60) NULL DEFAULT '\'\'' COMMENT '报警描述',
	`alarm_interval` MEDIUMINT(8) UNSIGNED NOT NULL DEFAULT '0' COMMENT '报警间隔（秒）',
	`is_valid` TINYINT(3) UNSIGNED NOT NULL DEFAULT '1' COMMENT '是否有效：0：无效；1：有效',
	`user_erp` VARCHAR(50) NOT NULL COMMENT '创建者\\\\更新者',
	`notify_type` tinyint(2) NOT NULL,
	`level` VARCHAR(50) NOT NULL DEFAULT 'warn' COMMENT '报警级别',
	`threshold` INT(11) NOT NULL DEFAULT '0' COMMENT '每个报警类型一次报警阀值',
	`create_time` DATETIME NOT NULL COMMENT '添加时间',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	INDEX `idx_update` (`is_valid`, `update_time`)
)
COMMENT='报警配置';

CREATE TABLE `saf_interface_week_stat` (
  `id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
  `total_ins` BIGINT(20) NOT NULL COMMENT '总实例数',
  `p_ins` BIGINT(20) NOT NULL COMMENT 'provider实例数',
  `c_ins` BIGINT(20) NOT NULL COMMENT 'consumer实例数',
  `p_ins_add` INT(11) NOT NULL COMMENT 'provider实例增加数',
  `c_ins_add` INT(11) NOT NULL COMMENT 'consumer实例增加数',
  `total_ip` INT(20) NOT NULL COMMENT '总IP数',
  `p_ips` INT(11) NOT NULL COMMENT 'provider IP数',
  `c_ips` INT(11) NOT NULL COMMENT 'consumerIP数',
  `p_ip_add` INT(11) NOT NULL COMMENT 'providerIP增加数',
  `c_ip_add` INT(11) NOT NULL COMMENT 'consumerIP增加数',
  `create_time` DATETIME NOT NULL COMMENT '创建时间',
  `week` INT(11) NOT NULL COMMENT '第几周',
  `total_ip_add` BIGINT(20) NOT NULL,
  `total_ins_add` BIGINT(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='节点实例变化量 周统计';


CREATE TABLE `saf_monitor_interface` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `interface_name` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT '接口名',
  `method` varchar(4000) NOT NULL COMMENT '方法名',
  `create_date` datetime NOT NULL,
  `update_date` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `saf_monitor_record` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `rowkey` varchar(200) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'rowkey',
  `time` bigint(20) NOT NULL COMMENT '时间',
  `time_type` int(2) NOT NULL COMMENT '事件类型, 2 天；1小时',
  `type` int(2) NOT NULL COMMENT '所属端 1provider；0consumer',
  `create_date` datetime NOT NULL,
  `update_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `ind_rowkey` (`rowkey`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `saf_scanstatus_hislog` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(32) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `ins_key` varchar(63) DEFAULT NULL COMMENT '实例key',
  `interface_name` varchar(255) DEFAULT NULL,
  `scan_type` tinyint(4) DEFAULT NULL,
  `detail_info` varchar(512) DEFAULT NULL,
  `creator` varchar(45) DEFAULT NULL,
  `creator_ip` varchar(32) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `sync_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--zzc
CREATE TABLE `saf_alarm_statistics` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'id自增',
	`alarm_date` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '时间',
	`alarm_type` TINYINT(3) NOT NULL,
	`alarm_ip` VARCHAR(15) NULL DEFAULT NULL COMMENT '报警IP',
	`alarm_interface` VARCHAR(255) NULL DEFAULT NULL COMMENT '报警接口',
	`alarm_count` INT(11) NOT NULL,
	PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='用于统计每一天中各个种类的报警信息的个数';

CREATE TABLE `saf_iface_invokehisto` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '表主键',
  `interface_name` varchar(100) NOT NULL COMMENT '接口名',
  `method` varchar(100) NOT NULL COMMENT '方法名',
  `calltimes` bigint(15) NOT NULL COMMENT '访问量',
  `invoke_date` varchar(100) NOT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_saf_calltimes` (`interface_name`,`invoke_date`)
) ENGINE=InnoDB AUTO_INCREMENT=1331583 DEFAULT CHARSET=utf8 COMMENT='接口历史调用量';
CREATE TABLE `saf_iface_invokehisto_hour` (
  `id` int(11) unsigned NOT NULL AUTO_INCREMENT COMMENT '表主键',
  `interface_name` varchar(100) NOT NULL COMMENT '接口名',
  `method` varchar(100) NOT NULL COMMENT '方法名',
  `calltimes` int(11) NOT NULL COMMENT '访问量',
  `invoke_date` varchar(30) NOT NULL COMMENT '统计时间',
  `invoke_date_time` varchar(30) NOT NULL COMMENT '统计时间（小时）',
  `create_time` datetime NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_saf_calltimes` (`interface_name`,`invoke_date`,`invoke_date_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1717029 DEFAULT CHARSET=utf8 COMMENT='接口历史调用量';
CREATE TABLE `saf_lastday_calltime` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '表主键',
  `calltimes` varchar(100) NOT NULL COMMENT '访问量',
  `invoke_date` varchar(100) NOT NULL COMMENT '统计时间',
  `create_time` datetime NOT NULL COMMENT '记录时间',
  PRIMARY KEY (`id`),
  KEY `idx_saf_calltimes` (`invoke_date`)
) ENGINE=InnoDB AUTO_INCREMENT=544 DEFAULT CHARSET=utf8 COMMENT='昨日调用量';

CREATE TABLE `saf_operate_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(32) DEFAULT NULL,
  `pin` varchar(32) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `ip` varchar(50) DEFAULT NULL,
  `clazz` varchar(255) DEFAULT NULL,
  `method` varchar(255) DEFAULT NULL,
  `action_type` varchar(32) DEFAULT NULL,
  `detail` varchar(5000) DEFAULT NULL,
  `is_success` tinyint(4) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8;
