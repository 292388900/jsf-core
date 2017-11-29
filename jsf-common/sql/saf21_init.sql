CREATE DATABASE IF NOT EXISTS saf21 CHARACTER SET utf8;
use saf21;

DROP TABLE IF EXISTS `saf_client`;
CREATE TABLE `saf_client` (
  `client_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `client_uniquekey` varchar(128) Binary NOT NULL COMMENT 'ip;pid;alias(group:version);protocol;interfaceId , 将这5个字段作为consumer的唯一判断和索引。为了降低唯一约束对在此不做唯一约束，通过程序进行唯一判断',
  `interface_id` int(10) unsigned NOT NULL,
  `interface_name` varchar(255) Binary NOT NULL,
  `client_alias` varchar(64) Binary NOT NULL COMMENT '默认alias,group:version',
  `client_ip` varchar(16) NOT NULL,
  `client_pid` int(10) unsigned NOT NULL,
  `protocol` tinyint(2) NOT NULL COMMENT '协议',
  `client_apppath` varchar(128) DEFAULT NULL,
  `client_status` tinyint(2) unsigned NOT NULL,
  `saf_version` int(10) NOT NULL,
  `src_type` tinyint(2) unsigned NOT NULL COMMENT '数据来源：1-proxy, 2-manual, 3-zookeeper',
  `ins_key` varchar(32) DEFAULT NULL COMMENT '实例key, ip_port_starttime(后5位)',
  `safurl_desc` varchar(1023) DEFAULT NULL,
  `start_time` bigint(20) unsigned NOT NULL COMMENT '节点启动时间',
  `del_time` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '删除时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  `c_type` tinyint(2) unsigned NOT NULL DEFAULT 1 COMMENT 'consumer类型，1-单个consumer, 2-consumergroup',
  `c_id` VARCHAR(64) NOT NULL DEFAULT '' COMMENT '数据来源：1-proxy, 2-manual, 3-zookeeper',
  PRIMARY KEY (`client_id`),
  KEY iface_name_idx (interface_name),
  KEY client_inskey (ins_key),
  UNIQUE KEY `client_uniquekey_UNIQUE` (`client_uniquekey`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='consumer信息表';

DROP TABLE IF EXISTS `saf_dept`;
CREATE TABLE `saf_dept` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `deptName` varchar(100) NOT NULL DEFAULT '' COMMENT '名称',
  `refId` int(11) NOT NULL DEFAULT '0' COMMENT '所属一级部门',
  `remark` varchar(120) DEFAULT NULL COMMENT '描述',
  `createdTime` datetime NOT NULL,
  `modifiedTime` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_group`;
CREATE TABLE `saf_group` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，组ID',
  `name` varchar(30) NOT NULL COMMENT '组名称',
  `flag` varchar(20) DEFAULT NULL COMMENT '组访问标识',
  `desc` varchar(80) DEFAULT NULL COMMENT '组描述',
  `valid` tinyint(1) DEFAULT '1' COMMENT '是否有效【1:有效; 0:无效】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_iface_version`;
CREATE TABLE `saf_iface_version` (
  `interface_id` int(11) NOT NULL COMMENT '接口id',
  `update_time` datetime NOT NULL COMMENT '接口下的provider更新时间戳',
  `cfg_update_time` datetime NOT NULL COMMENT '接口下的配置更新时间戳',
  PRIMARY KEY (`interface_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='记录接口下的provider更新时间戳';

DROP TABLE IF EXISTS `saf_ins_hb`;
CREATE TABLE `saf_ins_hb` (
  `ins_key` varchar(32) NOT NULL COMMENT '实例key, ip_port_starttime(后5位)',
  `ins_ip` varchar(16) NOT NULL COMMENT 'jsf实例IP',
  `ins_pid` int(10) unsigned NOT NULL COMMENT 'jsf实例pid',
  `ins_port` int(10) unsigned NOT NULL,
  `hb_time` datetime NOT NULL COMMENT '心跳时间',
  `ins_status` tinyint(3) unsigned NOT NULL COMMENT '实例状态',
  `start_time` bigint(20) unsigned NOT NULL DEFAULT '0' COMMENT 'jsf实例启动时间',
  `reg_ip` varchar(30) NOT NULL COMMENT '注册中心的ip',
  `saf_ver` int(10) DEFAULT NULL COMMENT 'jsf version',
  `language` varchar(16) DEFAULT NULL COMMENT '客户端语言',
  `ins_room` TINYINT(2) unsigned  NOT NULL DEFAULT 0 COMMENT '客户端所在机房，0-Default',
  `del_yn` tinyint(3) unsigned NOT NULL DEFAULT '0' COMMENT '删除标识, 0-未删除，1-删除',
  `del_time` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '删除时间',
  `jsf_appins_id` int(11) DEFAULT 0 COMMENT 'appins表的id',
  `app_id` INT(11) NOT NULL DEFAULT '0' COMMENT '自动部署appId',
  `app_ins_id` VARCHAR(16) NOT NULL DEFAULT '' COMMENT '自动部署app实例id',
  `create_time` datetime NOT NULL COMMENT '记录创建时间',
  `cg_open` TINYINT(1) DEFAULT 0 COMMENT 'callgraph isopen 0-默认值 1-未开启，2-开启 1和2代表使用了callgraph jar',
  `cg_enhance` TINYINT(1) DEFAULT 0 COMMENT 'callgraph isenhance 0-默认值 1-未增强，2-已增强 1和2代表使用了callgraph jar',
  PRIMARY KEY (`ins_key`),
  INDEX `Idx_App` (`ins_key`, `app_id`, `app_ins_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='实例心跳表。记录实例心跳时间';

DROP TABLE IF EXISTS `saf_interface`;
CREATE TABLE `saf_interface` (
	`id` INT(10) NOT NULL AUTO_INCREMENT,
	`interfaceName` VARCHAR(255) NOT NULL COMMENT '接口名字' COLLATE 'utf8_bin',
	`groupId` INT(10) NULL DEFAULT NULL COMMENT '项目ID',
	`department` VARCHAR(32) NULL DEFAULT NULL COMMENT '所属部门：默认创建人部门',
	`departmentCode` VARCHAR(8) NULL DEFAULT NULL COMMENT '记录用户申请时选择的1,2,3级部门中的最低级部门编号',
	`ownerUser` VARCHAR(300) NULL DEFAULT NULL COMMENT '允许查看的erp帐号，多个分号分隔',
	`important` TINYINT(1) NULL DEFAULT NULL COMMENT '是否重要接口',
	`hasJsfClient` TINYINT(1) NULL DEFAULT '0' COMMENT '是否有jsf客户端',
	`cross_lang` TINYINT(1) NULL DEFAULT '0' COMMENT '跨语言标志, 0-没有,1-部分,2-全部跨语言',
	`remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
	`valid` TINYINT(1) NULL DEFAULT '1' COMMENT '是否有效【1:有效; 0:无效；2:新建(待提交)；3:待审核; 4: 已驳回】',
	`syntozk` TINYINT(1) NULL DEFAULT '0' COMMENT '是否同步到zk【1:同步; 0:不同步】',
	`appInvoke` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '允许网关【1允许，0不允许】',
	`modifier` VARCHAR(40) NULL DEFAULT NULL COMMENT '修改者erp',
	`modifiedTime` DATETIME NULL DEFAULT NULL COMMENT '修改时间',
	`creator` VARCHAR(40) NULL DEFAULT NULL COMMENT '创建者erp',
	`createdTime` DATETIME NULL DEFAULT NULL COMMENT '创建时间',
	`src` INT(11) NOT NULL DEFAULT '2' COMMENT '来源',
	`uid` VARCHAR(100) NULL DEFAULT NULL COMMENT '和邮件关联的uid',
  `provider_total` INT(10) NULL DEFAULT '0' COMMENT 'Provider总数目',
  `provider_live` INT(10) NULL DEFAULT '0' COMMENT '存活的Provider数目',
  `consumer_total` INT(10) NULL DEFAULT '0' COMMENT 'Consumer总数目',
  `consumer_live` INT(10) NULL DEFAULT '0' COMMENT '存活的Consumer数目',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `UN_INTERFACENAME` (`interfaceName`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口基本信息表';

DROP TABLE IF EXISTS `saf_interface_display`;
CREATE TABLE `saf_interface_display` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，无意义',
  `pin` varchar(30) NOT NULL COMMENT '登陆账号,一般是用户ERP账号',
  `interfaceId` int(10) NOT NULL COMMENT '接口ID',
  `interfaceName` varchar(255) Binary NOT NULL COMMENT '接口名字',
  `method` VARCHAR(150) NOT NULL COMMENT '方法名',
  `metrics` VARCHAR(20) NOT NULL COMMENT '指标',
  `order` INT NOT NULL COMMENT '序号',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='用户接口关联表';

DROP TABLE IF EXISTS `saf_interface_property`;
CREATE TABLE `saf_interface_property` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`interface_id` INT(11) NOT NULL,
	`interface_name` VARCHAR(255) NOT NULL COLLATE 'utf8_bin',
	`param_key` VARCHAR(32) NULL DEFAULT NULL,
	`param_value` VARCHAR(1000) NOT NULL,
	`param_type` TINYINT(4) NOT NULL,
	`update_time` DATETIME NOT NULL COMMENT '记录更新时间',
	PRIMARY KEY (`id`),
	INDEX `property_interface_index` (`interface_id`),
	INDEX `interface_name_index` (`interface_name`, `param_key`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT='接口属性配置表';

/*CREATE TABLE `saf_invoke_topn` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `method` varchar(40) Binary NOT NULL COMMENT '方法名',
  `interfaceName` varchar(200) Binary NOT NULL COMMENT '接口名',
  `record` int(10) NOT NULL COMMENT '平均耗时',
  `createTime` datetime NOT NULL COMMENT '创建时间',
  `flag` varchar(50) NOT NULL COMMENT '标记(耗时, 最大耗时, 总调用次数)',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;*/

DROP TABLE IF EXISTS `saf_ipwb`;
CREATE TABLE `saf_ipwb` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `interface_id` int(10) unsigned NOT NULL COMMENT '接口id',
  interface_name varchar(200) not null,
  `alias_name` varchar(64) Binary NOT NULL,
  `ip_addr` varchar(64) NOT NULL,
  `ip_regular` varchar(255) not null comment 'IP正则表达式',
  `wb_type` tinyint(3) unsigned NOT NULL COMMENT '黑白名单类型:  1-white,2-black',
  `pc_type` tinyint(3) unsigned NOT NULL DEFAULT '1' COMMENT '1-provider调用接受consumer连接的ip ',
  `valid` TINYINT(3) UNSIGNED NOT NULL COMMENT '是否有效【1:有效; 0:无效】',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8 COMMENT='黑白名单表';

CREATE INDEX ipwb_interface_index ON `saf_ipwb` (`interface_id`);

DROP TABLE IF EXISTS `saf_notice`;
CREATE TABLE `saf_notice` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键',
  `title` varchar(100) NOT NULL COMMENT '标题',
  `content` varchar(5000) NOT NULL COMMENT '正文',
  `type` tinyint(1) DEFAULT '1' COMMENT '类型【1:公告; 2:最新动态】',
  `top` tinyint(1) DEFAULT '0' COMMENT '是否置顶【1:置顶; 0:不置顶】',
  `active` tinyint(1) DEFAULT '1' COMMENT '是否激活【1:激活（显示）; 0:不激活（不显示）】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_param`;
CREATE TABLE `saf_param` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '序号',
  `param_key` varchar(32) NOT NULL COMMENT '参数key',
  `param_name` varchar(32) NOT NULL COMMENT '参数名字',
  `param_value` varchar(128) DEFAULT NULL COMMENT '参数值',
  `param_type` tinyint(3) unsigned NOT NULL COMMENT '参数类型（区分是注册中心还是客户端等）',
  `note` varchar(255) DEFAULT NULL COMMENT '备注',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `index_param_key` (`param_key`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='系统参数表';

DROP TABLE IF EXISTS `saf_privilege`;
CREATE TABLE `saf_privilege` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，无意义',
  `name` varchar(30) DEFAULT NULL COMMENT '权限名称',
  `desc` varchar(80) DEFAULT NULL COMMENT '权限描述',
  `code` varchar(100) DEFAULT NULL COMMENT '权限码',
  `type` tinyint(1) DEFAULT '1' COMMENT '权限类型【1:模块权限; 2:资源权限】',
  `valid` tinyint(1) DEFAULT '1' COMMENT '是否有效【1:有效; 0:无效】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_role`;
CREATE TABLE `saf_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，角色ID',
  `name` varchar(30) NOT NULL COMMENT '角色名称',
  `type` tinyint(1) DEFAULT NULL COMMENT '角色类型【1:模块管理员; 2:资源管理员; 3:普通管理员】',
  `desc` varchar(80) DEFAULT NULL COMMENT '角色描述',
  `valid` tinyint(1) DEFAULT '1' COMMENT '是否有效【1:有效; 0:无效】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_role_privilege`;
CREATE TABLE `saf_role_privilege` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `roleId` int(10) NOT NULL,
  `privilegeId` int(10) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_room`;
CREATE TABLE `saf_room` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `room` tinyint(2) NOT NULL COMMENT '机房类型',
  `ip_section` varchar(128) NOT NULL COMMENT '机房ip描述',
  `ip_regular` varchar(255) NOT NULL COMMENT '机房ip正则表达式',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='机房描述信息';

DROP TABLE IF EXISTS `saf_server`;
CREATE TABLE `saf_server` (
  `server_id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `server_uniquekey` varchar(128) Binary NOT NULL COMMENT 'ip;port;alias(group:version);protocol;serviceInfoId , 将这5个字段作为provider的唯一判断和索引，在此不做唯一约束，通过程序进行判断',
  `interface_id` int(10) unsigned NOT NULL,
  `interface_name` varchar(255) Binary NOT NULL,
  `server_alias` varchar(64) Binary NOT NULL COMMENT '默认alias,group:version',
  `server_ip` varchar(16) NOT NULL COMMENT 'server的IP地址',
  `server_port` int(10) unsigned NOT NULL COMMENT 'server的端口号',
  `server_pid` int(10) unsigned NOT NULL COMMENT 'server的PID',
  `server_status` tinyint(2) unsigned NOT NULL,
  `server_room` tinyint(2) unsigned NOT NULL,
  `server_timeout` int(8) unsigned DEFAULT NULL,
  `server_weight` int(6) unsigned DEFAULT NULL,
  `server_apppath` varchar(128) NOT NULL,
  `protocol` tinyint(2) NOT NULL,
  `context_path` varchar(128) DEFAULT NULL,
  `saf_version` int(10) NOT NULL,
  `is_random` tinyint(1) unsigned NOT NULL,
  `src_type` tinyint(2) unsigned NOT NULL COMMENT 'data type: 1-registry, 2-manual, 3-zookeeper 用于数据同步',
  `ins_key` varchar(32) DEFAULT NULL COMMENT '实例key, ip_port_starttime(后5位)',
  `attr_url` varchar(255) DEFAULT NULL COMMENT '保存safurl中的attr参数，过滤掉一些在表中已经存在的参数',
  `safurl_desc` varchar(1023) DEFAULT NULL,
  `start_time` bigint(20) unsigned NOT NULL,
  `opt_type` tinyint(2) unsigned NOT NULL DEFAULT 1 COMMENT '操作类型，0-下线，1-上线',
  `del_time` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '删除时间',
  `last_hbtime` bigint(20) unsigned NOT NULL DEFAULT 0 COMMENT '最后心跳时间',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`server_id`),
  KEY interface_name_idx (interface_name),
  KEY index_ser_ip (server_ip),
  UNIQUE KEY `server_uniquekey_UNIQUE` (`server_uniquekey`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='provider信息表';

CREATE INDEX server_interface_index ON `saf_server` (`interface_id`);

DROP TABLE IF EXISTS `saf_server_alias`;
CREATE TABLE `saf_server_alias` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `server_id` int(10) unsigned NOT NULL COMMENT 'server  id',
  `server_uniquekey` varchar(128) Binary NOT NULL,
  `interface_id` int(10) unsigned NOT NULL COMMENT '接口id',
  `alias_name` varchar(64) Binary NOT NULL COMMENT '分组名称',
  `src_type` tinyint(3) unsigned NOT NULL,
  `alias_type` tinyint(3) unsigned NOT NULL comment '别名类型',
  `update_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='provider分组';

CREATE INDEX serveralias_interface_index ON `saf_server_alias` (`interface_id`);

DROP TABLE IF EXISTS `saf_user`;
CREATE TABLE `saf_user` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，用户ID',
  `pin` varchar(30) NOT NULL COMMENT '登陆账号,一般是用户ERP账号',
  `nick` varchar(30) DEFAULT '' COMMENT '显示名称',
  `realName` varchar(100) DEFAULT '' COMMENT '真实姓名',
  `mail` varchar(64) DEFAULT NULL COMMENT '邮箱',
  `tel` varchar(20) DEFAULT NULL COMMENT '电话',
  `password` VARCHAR(32) NULL DEFAULT NULL COMMENT '密码,注册账号使用',
	`type` INT UNSIGNED NOT NULL DEFAULT '1' COMMENT '账号类型【1:sso; 2:register】',
  `valid` tinyint(4) DEFAULT '1' COMMENT '是否有效【1:有效; 0:无效】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  `lastLoginTime` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='用户表';

/*CREATE TABLE `saf_user_interface` (
  `id` int(10) NOT NULL AUTO_INCREMENT COMMENT '自动生成的主键，无意义',
  `pin` varchar(30) NOT NULL COMMENT '登陆账号,一般是用户ERP账号',
  `interfaceId` varchar(150) NOT NULL COMMENT '接口ID',
  `interfaceName` varchar(150) Binary NOT NULL COMMENT '接口名字',
  `methods` varchar(2000) Binary DEFAULT NULL COMMENT '所属项目',
  `groupId` tinyint(2) DEFAULT NULL COMMENT '所属项目ID',
  `status` tinyint(2) NOT NULL DEFAULT '0' COMMENT '当前状态：1启动 2停用 3已删除',
  `isDisplay` bigint(20) DEFAULT NULL COMMENT '是否显示【0:不显示; 1:显示】',
  `modifier` varchar(40) DEFAULT NULL COMMENT '修改者',
  `modifiedTime` datetime DEFAULT NULL COMMENT '修改时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '创建者',
  `createdTime` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8 COMMENT='用户接口关联表';*/

DROP TABLE IF EXISTS `saf_user_resource`;
CREATE TABLE `saf_user_resource` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `pin` varchar(20) DEFAULT NULL COMMENT '用户账号',
  `resId` int(10) DEFAULT NULL COMMENT '资源ID',
  `roleId` int(10) DEFAULT NULL COMMENT '角色ID',
  `resType` tinyint(4) DEFAULT NULL COMMENT '资源类型【1:项目; 2:接口】 ',
  `pcType` TINYINT(4) NOT NULL DEFAULT '1' COMMENT 'provider:1, conumser:2',
  PRIMARY KEY (`id`),
  INDEX `PIN_RES_INDEX` (`pin`, `resId`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_user_resource`;
CREATE TABLE `saf_user_role` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `pin` varchar(30) NOT NULL COMMENT '用户账号',
  `roleId` int(10) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=100 DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_user_action`;
CREATE TABLE `saf_user_action` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `userName` varchar(32) NOT NULL COMMENT '用户名',
  `pin` varchar(32) NOT NULL COMMENT 'erp帐户',
  `ip` VARCHAR(50) NOT NULL,
  `actionType` int NOT NULL COMMENT '操作类型',
  `createdTime` datetime NOT NULL,
  `detail` varchar(5000) DEFAULT NULL,
  `isSucc` tinyint DEFAULT NULL,
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT='用户操作日志';

/*CREATE TABLE `saf_index` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `saf_version` varchar(20) NOT NULL COMMENT 'saf版本 1.0 or 2.0',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `last_update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_enable` tinyint(1) NOT NULL COMMENT '预留',
  `remark` varchar(200) DEFAULT NULL COMMENT '描述',
  `name` varchar(100) NOT NULL COMMENT '名字',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8;*/

DROP TABLE IF EXISTS `saf_server_set`;
CREATE TABLE `saf_server_set` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `interface_id` int(11) NOT NULL COMMENT '接口ID',
  `server_id` int(11) NOT NULL COMMENT 'serverId',
  `server_uniquekey` varchar(128) Binary NOT NULL,
  `value` varchar(1000) DEFAULT NULL COMMENT 'map值',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8  COMMENT '配置表';

DROP TABLE IF EXISTS `saf_worker`;
CREATE TABLE `saf_worker` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT COMMENT '序号',
  `worker_name` varchar(255) NOT NULL COMMENT 'worker业务类型 与worker一一对应',
  `worker_type` char(1) NOT NULL COMMENT 'Worker类别single(0) distribute(1)两种类别',
  `worker_desc` varchar(255) NOT NULL COMMENT 'worker实现类',
  `cron_expression` varchar(255) DEFAULT NULL COMMENT 'Worker执行表达式',
  `worker_parameters` varchar(255) DEFAULT NULL COMMENT 'Worker相关的业务参数 为json串',
  `worker_manager` varchar(255) NOT NULL COMMENT 'Worker类别single(0) distribute(1)两种类别',
  `error_alert` char(1) DEFAULT NULL COMMENT '是否需要出错报警',
  `active` char(1) NOT NULL DEFAULT '1' COMMENT '是否启动，默认启动',
  `immediate` char(1) DEFAULT '0' COMMENT '1 代表true 0 代表false如果为true worker的执行是不阻塞的，到点就执行。如果为false worker的执行受上次执行影响，上次执行完了才执行如果时间到了的情况下',
  `create_time` datetime NOT NULL,
  `update_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='系统worker';

DROP TABLE IF EXISTS `saf_index_rule`;
CREATE TABLE `saf_index_rule` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
  `saf_version` varchar(64) DEFAULT NULL COMMENT '客户端saf版本',
  `client_ip` varchar(128) DEFAULT NULL COMMENT '客户端ip规则',
  `language` varchar(128) DEFAULT NULL COMMENT '客户端语言',
  `app_name` varchar(128) DEFAULT NULL COMMENT '客户端项目名称',
  `address` varchar(512) NOT NULL COMMENT '规则对应的注册中心地址',
  `remark` varchar(512) DEFAULT NULL COMMENT '描述',
  `seq` smallint(6) NOT NULL COMMENT '排序号，序号越小的规则越先执行',
  `enable` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否启用，0停用，1启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `app_id` varchar(512) DEFAULT NULL,
  `register_num` smallint(6) NOT NULL COMMENT '本规则优先注册中心个数',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Index规则表，管理端维护，indexserver读取。';

DROP TABLE IF EXISTS `saf_iface_router`;
CREATE TABLE `saf_iface_router` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `interface_name` varchar(255) NOT NULL COMMENT '接口名',
  `interface_id` int(11) NOT NULL COMMENT '接口ID',
  `type` tinyint(4) NOT NULL COMMENT '类型，1：IP路由；2：参数路由',
  `value` varchar(255) NOT NULL COMMENT '路由值',
  `valid` tinyint not null COMMENT '1有效；0无效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8  comment '服务路由表';

DROP TABLE IF EXISTS `saf_scanstatus_log`;
CREATE TABLE `saf_scanstatus_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(32) DEFAULT NULL,
  `pid` int(11) DEFAULT NULL,
  `ins_key` varchar(63) DEFAULT NULL COMMENT '实例key',
  `interface_name` varchar(255) DEFAULT NULL,
  `scan_type` tinyint(4) DEFAULT NULL,
  `del_type` tinyint(4) DEFAULT 9 COMMENT '删除类型：4-反注册，5-逻辑删, 9-物理删',
  `detail_info` varchar(1536) DEFAULT NULL,
  `creator` varchar(45) DEFAULT NULL,
  `creator_ip` varchar(32) DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

DROP TABLE IF EXISTS `saf_checkdb`;
CREATE TABLE `saf_checkdb` (
  `id` bigint(20) NOT NULL,
  `update_time` datetime NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '测试表，用于判断数据库是否连接正常';

DROP TABLE IF EXISTS `saf_callback_log`;
CREATE TABLE `saf_callback_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ip` varchar(31) NOT NULL COMMENT '实例IP',
  `interface_name` varchar(255) NULL COMMENT '接口名',
  `alias` varchar(64) NULL COMMENT '别名',
  `dataversion` datetime NULL COMMENT '版本号',
  `ins_key` varchar(63) NOT NULL COMMENT '实例key',
  `notify_type` int(11) NOT NULL COMMENT 'notify的通知类型',
  `log_note` varchar(512) NOT NULL COMMENT '通知日志',
  `param` varchar(512) NOT NULL DEFAULT '' COMMENT '参数通知信息',
  `log_type` int(11) DEFAULT NULL COMMENT '日志类型，包括注册中心下发配置、服务列表、异常类型',
  `reg_ip` varchar(31) NOT NULL COMMENT '创建者ip',
  `creator` varchar(31) NOT NULL COMMENT '创建者',
  `create_time` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=utf8;


/*CREATE TABLE `saf_iperp_relate` (
  `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `erp` varchar(50) NOT NULL COMMENT 'erp',
  `ips` varchar(1000) DEFAULT NULL COMMENT '负责的ip',
  `pc_type` varchar(10) NOT NULL COMMENT '所属端',
  `src` int(11) NOT NULL COMMENT '来源1worker；2管理端',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`)
)  comment 'ip erp 关联';*/

DROP TABLE IF EXISTS `saf_view_cfg`;
CREATE TABLE `saf_view_cfg` (
  `saf_key` varchar(100) NOT NULL COMMENT 'key',
  `describe` varchar(100) NOT NULL COMMENT '描述',
  `pc_type` tinyint(4) NOT NULL COMMENT '类型1provider；0consumer',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`saf_key`,`pc_type`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 ;

DROP TABLE IF EXISTS `saf_iface_apply`;
CREATE TABLE `saf_iface_apply` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `interfaceName` varchar(255) NOT NULL COMMENT '接口名字',
  `ownerUser` varchar(300) DEFAULT NULL COMMENT '允许查看的erp帐号，多个分号分隔',
  `department` varchar(32) DEFAULT NULL COMMENT '所属部门',
  `departmentCode` VARCHAR(8) NULL DEFAULT NULL COMMENT '所属部门编号，记录用户选择的一级，二级，三级部门中的最低级部门' COLLATE 'utf8_unicode_ci',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `status` tinyint(1) DEFAULT '2' COMMENT '是否有效【1:有效; 2:新建(待提交)；3:待审核; 4: 已驳回】',
  `modifier` varchar(40)  DEFAULT NULL COMMENT '修改者erp',
  `auditTime` datetime DEFAULT NULL COMMENT '审核时间',
  `creator` varchar(40) DEFAULT NULL COMMENT '提交者erp',
  `auditor` varchar(40)  DEFAULT NULL COMMENT '审核者erp',
  `applyTime` datetime DEFAULT NULL COMMENT '创建时间',
  `uid` varchar(50) DEFAULT '' COMMENT '和邮件关联的id',
  `rejectReason` varchar(100) DEFAULT '' COMMENT '和邮件关联的id',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UN_INTERFACENAME` (`interfaceName`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8  comment '接口申请表';

DROP TABLE IF EXISTS `saf_iface_visit`;
CREATE TABLE `saf_iface_visit` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `interface_id` int(11) NOT NULL  COMMENT '接口id',
  `visitor_name` varchar(63) NOT NULL COMMENT '访问者名称',
  `interface_name` varchar(255) Binary NOT NULL COMMENT '接口名',
  `isvalid` tinyint(2) NOT NULL DEFAULT '1' COMMENT '是否有效0-无效,1-有效',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口访问授权表';

DROP TABLE IF EXISTS `saf_app_ins`;
CREATE TABLE `saf_app_ins` (
  `jsf_appins_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'app实例表主键',
  `app_ins_id` varchar(16) NOT NULL COMMENT '自动部署的实例id',
  `jsf_app_id` int(11) NOT NULL COMMENT 'app表主键',
  `app_id` int(11) NOT NULL COMMENT '自动部署的应用id',
  `app_ip` varchar(32) NOT NULL,
  `ins_key` varchar(32) NOT NULL DEFAULT '' COMMENT '实例key, ip_port_starttime(后5位)',
  `src_type` tinyint(2) unsigned NOT NULL COMMENT '数据来源：0-自动部署, 1-手动录入',
  `create_time` datetime NOT NULL,
  `creator` varchar(64) NOT NULL,
  `update_time` datetime NOT NULL,
  `modifier` varchar(64) NOT NULL,
  PRIMARY KEY (`jsf_appins_id`),
  UNIQUE KEY `appins_uniq` (`app_id`,`app_ins_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app实例表';

CREATE INDEX saf_app_ins_index ON `saf_app_ins` (`app_id`);

DROP TABLE IF EXISTS `saf_app`;
CREATE TABLE `saf_app` (
  `jsf_app_id` int(11) NOT NULL AUTO_INCREMENT COMMENT 'app表主键',
  `app_id` int(11) NOT NULL UNIQUE,
  `app_name` varchar(128) NOT NULL,
  `src_type` tinyint(2) unsigned NOT NULL COMMENT '数据来源：0-自动部署, 1-手动录入',
  `create_time` datetime NOT NULL,
  `creator` varchar(64) NOT NULL,
  `app_type` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '应用类型 比如tomcat之类',
  `level` VARCHAR(10) NOT NULL DEFAULT '' COMMENT '系统级别',
  `level_name` VARCHAR(20) NOT NULL DEFAULT '' COMMENT '系统级别名称',
  `jone_app_level` VARCHAR(10) NOT NULL DEFAULT '' COMMENT 'JONE应用级别',
  `domain` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '域名',
  `dept_name` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '部门信息 / 分隔',
  `first_branch` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '一级部门',
  `second_branch` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '二级部门',
  `app_developgroup_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '开发组名',
  `developer` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '开发组erp 逗号分隔' ,
  `developer_leader` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '应用负责人',
  `leader` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '开发组负责人',
  `leader_name` VARCHAR(50) NOT NULL DEFAULT '' COMMENT '开发组负责人名字',
  `app_token` VARCHAR(32) NOT NULL DEFAULT '' COMMENT '调用上下线接口的token',
  PRIMARY KEY (`jsf_app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app表';

CREATE INDEX saf_app_index ON `saf_app` (`app_id`);

/*--init data sql*/
/* jsf 明文密码:123456, md5加密:e10adc3949ba59abbe56e057f20f883e */
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`password`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (2, 'jsf','jsf','jsf','','','e10adc3949ba59abbe56e057f20f883e',2,1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (3,'xietingfeng','xietingfeng','谢霆锋','','',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (4,'xiaofeng','xiaofeng','肖锋','','',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (5,'zhouzhiruo','zhouzhiruo','周芷若','','',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (6,'linzhiling','linzhiling','林志玲','','',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user`(`id`,`pin`,`nick`,`realName`,`mail`,`tel`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (7,'zhaobenshan','zhaobenshan','赵本山','','',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_role`(`id`,`name`,`type`,`desc`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (1,'超级管理员',0,'超级管理员',1,'','2015-08-04 14:00','','2015-08-04 14:00');
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(2,'jsf',1);
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(3,'xietingfeng',1);
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(4,'xiaofeng',1);
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(5,'zhouzhiruo',1);
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(6,'linzhiling',1);
INSERT INTO `saf_user_role`(`id`,`pin`,`roleId`)VALUES(7,'zhaobenshan',1);

INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (1,'globalsetting.version','全局配置版本号（勿删）','1',1,'全局配置版本号，订阅时，比较版本号',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (2,'reg.hb.interval','注册中心心跳时间（勿删）','15000',1,'客户端定时向注册中心发心跳的间隔',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (3,'reg.ck.interval','定时连注册中心检查时间（勿删）','300000',1,'客户端定时从注册中心check数据的间隔',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (4,'mntr.send.interval','监控发送间隔（勿删）','20000',1,'',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (5,'mntr.send.open','全局监控是否开启（勿删）','true',1,'',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (6,'safVersion','SAF版本','108,109,210',2,'',CURRENT_TIMESTAMP(),CURRENT_TIMESTAMP());
INSERT INTO `saf_param` (`id`,`param_key`,`param_name`,`param_value`,`param_type`,`note`,`create_time`,`update_time`) VALUES (7,'srv.sudo.passwd','Provider的sudo密码（误删）','saf618',1,'Provider的sudo密码','2014-06-18 13:55:44','2014-07-24 18:15:38');

/*--2015-03-10*/
DROP TABLE IF EXISTS `saf_app_user`;
CREATE TABLE `saf_app_user` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
	`app_id` INT(11) NOT NULL DEFAULT '0' COMMENT 'appId外键引用，为saf_app_ip中的app_id值',
	`pin` VARCHAR(255) NOT NULL COMMENT 'erp',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 comment 'App用户关联表';

DROP TABLE IF EXISTS `saf_app_iface`;
CREATE TABLE `saf_app_iface` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`interface_id` INT(11) NULL DEFAULT '0' COMMENT '接口id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
	`method` VARCHAR(100) NOT NULL COMMENT '方法名',
	`app_id` INT(11) NOT NULL COMMENT 'app_id',
	`alias` VARCHAR(100) NULL DEFAULT NULL COMMENT '别名',
	`interval` INT(11) NOT NULL COMMENT '时间间隔',
	`invoke_time` INT(11) NOT NULL COMMENT '次数限制',
	`counter_type` TINYINT(4) NOT NULL COMMENT '计数器实现（0:monitor服务，不完全精准但是性能损耗小 1：counter服务，精准但是性能损耗稍大）',
	`creator` VARCHAR(50) NOT NULL,
	`modifier` VARCHAR(50) NOT NULL,
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	INDEX `iface_index` (`interface_name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 comment '接口App调用次数设置表';

DROP TABLE IF EXISTS `saf_iface_mock`;
CREATE TABLE `saf_iface_mock` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`interface_id` INT(11) NOT NULL DEFAULT '0' COMMENT '接口id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
	`method` VARCHAR(100) NOT NULL COMMENT '方法名',
	`ip` VARCHAR(100) NOT NULL COMMENT 'IP',
	`alias` VARCHAR(100) NOT NULL COMMENT '别名',
	`mock_open` TINYINT(4) NOT NULL DEFAULT '0' COMMENT '开关,0:关；1:开',
	`mock_value` VARCHAR(1000) NULL DEFAULT NULL COMMENT 'mock值',
	`creator` VARCHAR(50) NOT NULL,
	`modifier` VARCHAR(50) NOT NULL,
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
	PRIMARY KEY (`id`),
	INDEX `iface_index` (`interface_name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口mock表';

DROP TABLE IF EXISTS `saf_monitor_disattr`;
CREATE TABLE `saf_monitor_disattr` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT 'id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
	`method` VARCHAR(100) NOT NULL COMMENT '方法名',
	`time_disattr` VARCHAR(50) NOT NULL COMMENT '时间分布',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	PRIMARY KEY (`id`),
	INDEX `iface_index` (`interface_name`)
) COMMENT='consumer监控时间分布';

DROP TABLE IF EXISTS `saf_gw_interface`;
CREATE TABLE `saf_gw_interface` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`interface_id` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '接口id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名称',
	`app_id` INT(11) NOT NULL COMMENT 'appId',
	`alias` VARCHAR(100) NOT NULL COMMENT '别名',
	`method` VARCHAR(100) NOT NULL COMMENT '方法',
	`partition_name` VARCHAR(100) NULL DEFAULT NULL COMMENT '网关分区',
	`invoke_time` INT(11) NOT NULL COMMENT '调用次数',
	`status` TINYTEXT NOT NULL COMMENT '状态，0：新建；1审核通过；2:驳回',
	`applyer` VARCHAR(30) NOT NULL COMMENT '申请人',
	`auditor` VARCHAR(30) NULL DEFAULT NULL COMMENT '审核人',
	`remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
	`uid` VARCHAR(50) NULL DEFAULT NULL COMMENT '和邮件管联的id',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`audit_time` DATETIME NULL DEFAULT NULL COMMENT '审核时间',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `gwinterface_key` (`interface_name`, `method`, `alias`, `app_id`),
	INDEX `interfaceid_key` (`interface_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口网关调用';

DROP TABLE IF EXISTS `saf_iface_appinvoke`;
CREATE TABLE `saf_iface_appinvoke` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`interface_id` BIGINT(20) NOT NULL DEFAULT '0' COMMENT '接口id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名称',
	`app_id` INT(11) NOT NULL COMMENT 'appId',
	`status` TINYTEXT NOT NULL COMMENT '状态，0：新建；1审核通过；2:驳回',
	`applyer` VARCHAR(30) NOT NULL COMMENT '申请人',
	`auditor` VARCHAR(30) NULL DEFAULT NULL COMMENT '审核人',
	`remark` VARCHAR(500) NULL DEFAULT NULL COMMENT '备注',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`audit_time` DATETIME NULL DEFAULT NULL COMMENT '审核时间',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `appinvoke_key` (`interface_name`, `app_id`),
	INDEX `interfaceid_key` (`interface_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口app调用表';

DROP TABLE IF EXISTS `saf_ifaceapp_deptserver`;
CREATE TABLE `saf_ifaceapp_deptserver` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
	`app_id` INT(11) NOT NULL,
	`interface_id` INT(11) NOT NULL,
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
	`first_dept` VARCHAR(100) NOT NULL COMMENT '一级部门',
	`second_dept` VARCHAR(100) NOT NULL COMMENT '二级部门',
	`third_dept` VARCHAR(100) NULL DEFAULT NULL COMMENT '三级部门',
	`create_time` DATETIME NOT NULL,
	`creator` VARCHAR(64) NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `interface_id` (`interface_id`, `second_dept`, `third_dept`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='app表';

DROP TABLE IF EXISTS `saf_lastday_calltime`;
CREATE TABLE `saf_lastday_calltime` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '表主键',
	`calltimes` VARCHAR(100) NOT NULL,
	`invoke_date` VARCHAR(100) NOT NULL,
	`create_time` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `saf_calltime_index` (`invoke_date`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='昨日调用量';

DROP TABLE IF EXISTS `saf_reg_hb`;
CREATE TABLE `saf_reg_hb` (
  `reg_ip` VARCHAR(64) NOT NULL COMMENT '注册中心地址',
  `hb_time` BIGINT(20) NOT NULL COMMENT '更新时间',
  `valid_flag` TINYINT(2) NOT NULL DEFAULT '1' COMMENT '是否有效, 0-无效，不检测注册中心关联的实例心跳, 1-有效，检查注册中心关联的实例心跳',
  PRIMARY KEY (`reg_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '注册中心心跳表，用于检测注册中心与db是否正常';

/*--2015-06-10 makeyang added*/
DROP TABLE IF EXISTS `saf_iface_invokehisto`;
CREATE TABLE `saf_iface_invokehisto` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '表主键',
	`interface_name` VARCHAR(100) NOT NULL comment '接口名',
	`method` VARCHAR(100) NOT NULL comment '方法名',
	`calltimes` BIGINT(15) NOT NULL,
	`invoke_date` VARCHAR(100) NOT NULL,
	`create_time` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `saf_calltime_index` (`interface_name`,`invoke_date`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口历史调用量';

DROP TABLE IF EXISTS `saf_index_registry`;
CREATE TABLE `saf_index_registry` (
	`index_id` INT(10) UNSIGNED NOT NULL,
	`registry_id` INT(10) UNSIGNED NOT NULL,
	UNIQUE INDEX `Index all` (`registry_id`, `index_id`),
	INDEX `Index reg_id` (`registry_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8
COMMENT='index规则和注册中心关系表'
COLLATE='utf8_general_ci';

DROP TABLE IF EXISTS `saf_iface_predeploy`;
CREATE TABLE `saf_iface_predeploy` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`interface_id` INT(11) NOT NULL COMMENT '接口ID',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名',
	`ip` VARCHAR(50) NOT NULL COMMENT 'IP',
	`port` INT(5) NOT NULL COMMENT 'port',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	PRIMARY KEY (`id`),
	INDEX `ifaceid_index` (`interface_id`),
	INDEX `interface_name_idx` (`interface_name`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='接口预发布配置表';

/*add by linzhiling at 2015-12-3*/
DROP TABLE IF EXISTS `saf_index_deptrule`;
CREATE TABLE `saf_index_deptrule` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`first_dept` VARCHAR(50) DEFAULT NULL COMMENT '一级部门',
	`second_dept` VARCHAR(50) DEFAULT NULL COMMENT '二级部门',
	`address` VARCHAR(512) NOT NULL COMMENT '规则对应的注册中心地址',
  `remark` varchar(512) DEFAULT NULL COMMENT '描述',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` DATETIME NOT NULL COMMENT '更新时间',
	PRIMARY KEY (`id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='部门Index规则表';


#2015.12.24 zcc 新的部门表
DROP TABLE IF EXISTS `saf_department`;
CREATE TABLE `saf_department` (
	`id` INT(11) NOT NULL AUTO_INCREMENT COMMENT '自增id',
	`departmentCode` VARCHAR(8) NOT NULL DEFAULT '' COMMENT '部门编号，必须与主数据平台一致',
	`departmentName` VARCHAR(100) NOT NULL DEFAULT '' COMMENT '部门名称',
	`departmentLevel` INT(11) NULL DEFAULT '0' COMMENT '部门级别,分为1,2,3,4级',
	`parentCode` VARCHAR(8) NULL DEFAULT NULL COMMENT '父部门编号',
	`synchro` TINYINT(1) NULL DEFAULT '1' COMMENT '是否与主数据平台同步标志位，1：同步；0：不同步',
	`isShow` TINYINT(1) NULL DEFAULT '1' COMMENT '是否展示该项部门数据，1：展示，0：不展示',
	`createdTime` DATETIME NULL DEFAULT NULL COMMENT '创建时间',
	`updateTime` DATETIME NULL DEFAULT NULL COMMENT '更新时间',
	PRIMARY KEY (`id`),
	UNIQUE INDEX `departmentCode` (`departmentCode`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='新的部门表';

#2015.12.24 zcc 插入一级部门数据
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00018997','京东集团-CPO体系',1,'',now(),now());
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00020462','京东集团-CTO体系',1,'',now(),now());
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00017156','京东集团-京东到家',1,'',now(),now());
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00008987','京东集团-京东金融',1,'',now(),now());
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00013807','京东集团-京东商城',1,'',now(),now());
insert into saf_department(departmentCode,departmentName,departmentLevel,parentCode,createdTime,updateTime) values('00011264','京东集团-拍拍网',1,'',now(),now());

DROP TABLE IF EXISTS `jsf_cap_rule`;
CREATE TABLE `jsf_cap_rule` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`type` TINYINT(4) NOT NULL COMMENT '规则类型 0tps 1耗时',
	`interface_id` INT(10) NOT NULL COMMENT '接口id',
	`interface_name` VARCHAR(255) NOT NULL COMMENT '接口名字',
	`method_name` VARCHAR(64) NOT NULL COMMENT '方法',
	`alias` VARCHAR(64) NULL DEFAULT NULL COMMENT '组',
	`app_id` VARCHAR(64) NULL DEFAULT NULL COMMENT '应用Id',
	`max_containers` INT(11) NOT NULL COMMENT '最大容器数量',
	`min_containers` INT(11) NOT NULL COMMENT '最小容器数量',
	`add_containers` INT(11) NOT NULL COMMENT '一次增加的容器数量',
	`del_containers` INT(11) NOT NULL COMMENT '一次删除的容器数量',
	`repeat_minutes` INT(11) NOT NULL COMMENT '最近几分钟数',
	`max_tpm` INT(11) NULL DEFAULT NULL COMMENT '每容器每分钟调用次数，到达TPM最大阀值增加容器',
	`min_tpm` INT(11) NULL DEFAULT NULL COMMENT '每容器每分钟调用次数，到达TPM最小阀值减少容器',
	`max_epm` INT(11) NULL DEFAULT NULL COMMENT '每容器每分钟调用平均耗时，到达EPM最大阀值增加容器',
	`min_epm` INT(11) NULL DEFAULT NULL COMMENT '每容器每分钟调用平均耗时，到达EPM最小阀值减少容器',
	`create_time` DATETIME NOT NULL,
	`update_time` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `interface_id` (`interface_id`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='弹性计算规则表';

DROP TABLE IF EXISTS `jsf_cap_elastic_event`;
CREATE TABLE `jsf_cap_elastic_event` (
	`id` INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
	`sequence` VARCHAR(100) NULL DEFAULT NULL COMMENT '每次操作序列号',
	`type` TINYINT(4) NOT NULL COMMENT '10 动态分组',
	`interface_id` INT(10) NOT NULL COMMENT '接口id',
	`app_id` INT(10) NULL DEFAULT NULL COMMENT 'appId',
	`alias` VARCHAR(64) NULL DEFAULT NULL,
	`desc` VARCHAR(1024) NULL DEFAULT NULL,
	`create_time` DATETIME NOT NULL,
	`update_time` DATETIME NULL DEFAULT NULL,
	PRIMARY KEY (`id`),
	INDEX `sequence` (`sequence`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='弹性计算事件表';

DROP TABLE IF EXISTS `jsf_ins_server`;
CREATE TABLE `jsf_ins_server` (
	`ins_key` VARCHAR(32) NOT NULL COMMENT '实例key',
	`server_uniquekey` varchar(128) Binary NOT NULL COMMENT 'ip;port;alias(group:version);protocol;interfaceId , 将这5个字段作为provider的唯一判断和索引，在此不做唯一约束，通过程序进行判断',
	`create_time` DATETIME NOT NULL COMMENT '创建时间',
	`update_time` DATETIME NOT NULL COMMENT '更新时间',
	PRIMARY KEY (`ins_key`, `server_uniquekey`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='实例与provider关联表';

DROP TABLE IF EXISTS `jsf_ifacealias_version`;
CREATE TABLE `jsf_ifacealias_version` (
  `interface_id` int(11) NOT NULL COMMENT '接口id',
  `alias` VARCHAR(64) Binary NOT NULL COMMENT '别名',
  `update_timestamp` bigint(20) unsigned NOT NULL COMMENT '接口alias的provider更新时间戳',
  `update_time` DATETIME NOT NULL COMMENT '接口alias的provider更新时间戳',
  PRIMARY KEY (`interface_id`, `alias`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='记录接口alias的provider时间变化';

/*--2016-02-01 maggie added*/
DROP TABLE IF EXISTS `jsf_check_history`;
CREATE TABLE `jsf_check_history` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`src_ip` VARCHAR(50) NOT NULL COMMENT '请求源地址',
	`src_room` VARCHAR(50) NULL DEFAULT NULL COMMENT '请求机房',
	`dst_ip` VARCHAR(50) NOT NULL COMMENT '目标IP（例如注册中心IP）',
	`dst_port` INT(5) NOT NULL COMMENT '目标端口（例如注册中心端口）',
	`dst_room` VARCHAR(50) NULL DEFAULT NULL COMMENT '目标机房',
	`result_code` INT(11) NOT NULL COMMENT '检查结果 0正常 1连不通 2连通业务异常',
	`result_message` VARCHAR(4096) NULL DEFAULT NULL COMMENT '检查结果明细',
	`check_time` DATETIME NOT NULL COMMENT '检查时间',
	`create_time` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	INDEX `check_time_idx` (`check_time`)
)ENGINE=InnoDB DEFAULT CHARSET=utf8
COMMENT='检查服务网络情况历史记录表'
COLLATE='utf8_general_ci';

/*在管理端配置的参数请随手写到这里*/
INSERT INTO `saf_param` (`id`, `param_key`, `param_name`, `param_value`, `param_type`, `note`, `create_time`, `update_time`) VALUES
	(27, 'invoke.compress.size', 'Consumer开启压缩的最小大小（勿删）', '2048', 1, '大于数值（默认2k）才启动压缩', '2014-06-18 13:55:44', '2015-07-15 11:17:55'),
	(28, 'invoke.compress.open', 'Consumer开启压缩全局开关（勿删）', 'true', 1, 'Consumer开启压缩全局开关', '2014-06-18 13:55:44', '2014-07-24 18:15:38'),
	(29, 'alarm.admin.erp', 'saf管理员erp', 'xiaofeng', 2, 'saf管理员erp', '2014-08-15 18:33:14', '2015-04-27 13:18:03'),
	(30, 'reg.bk.dir', '服务列表备份文件地址（勿删）', '/export/data/jsf', 1, '代码里默认为$HOME/jsf', '2014-08-19 10:33:09', '2014-10-24 16:17:23'),
	(32, 'saf1.interface.updateversion', 'saf1.0接口信息(部门,erp,备注)同步时间戳', '1448435225719', 3, 'synzkdbworker', '2014-08-19 14:56:04', '2015-11-25 15:07:06'),
	(34, 'registry_room', 'registry_room', 'yz,b28,鲁谷,yf,香港,廊坊', 2, '', '2014-08-26 19:16:55', '2015-08-06 11:37:30'),
	(35, 'srv.sudo.whitelist', 'Provider的sudo白名单（勿删）', '192.168.151.144,192.168.150.121,192.168.159.94,192.168.162.16,192.168.183.93,127.0.0.1', 1, '只有有白名单telnet才能开启sudo命令', '2014-09-11 10:41:01', '2015-11-26 15:50:31'),
	(36, 'saf_tracelog_threshold', '上下线记录报警阀值', '500', 2, '', '2014-10-09 16:05:23', '2014-10-09 16:05:23'),
	(37, 'saf_tracelog_threshold_if', '接口上下线次数阀值', '1000', 2, '', '2014-10-09 16:06:02', '2014-10-09 16:06:02'),
	(38, 'system.aboutus', '关于我们', '', 4, '', '2014-10-27 13:17:36', '2014-10-27 13:17:36'),
	(39, 'is_system_online', '判断是否线上环境', 'false', 4, '勿删', '2014-10-27 14:22:52', '2015-04-10 09:04:09'),
	(41, 'room.weight.factor', '本地/远程机房权重系数', '11', 3, '注册中心使用', '2014-11-06 15:31:28', '2014-11-26 14:42:34'),
	(42, 'registry_command', 'registry命令', 'conf,envi,stat,cons,wchs,dbex', 4, 'registry命令', '2014-11-27 17:46:11', '2014-11-27 17:46:11'),
	(44, 'user.view.provider.cfg', '用户查看provider配置属性', 'weight,timeout', 4, '用户查看provider配置属性', '2014-12-09 11:11:09', '2014-12-09 11:11:09'),
	(45, 'user.view.consumer.cfg', '用户查看consumer配置属性', 'timeout,retries', 4, '用户查看consumer配置属性', '2014-12-09 11:12:01', '2014-12-09 11:12:01'),
	(46, 'bdb.open.switch', '是否开启berkeleyDB-总开关', 'true', 3, '注册中心注册使用,true-开启,false-关闭', '2014-12-15 14:16:30', '2015-10-26 17:54:10'),
	(47, 'bdb.open.switch.provider', '是否开启berkeleyDB-provider', 'true', 3, '注册中心注册provider使用,true-开启,false-关闭', '2014-12-15 14:17:30', '2014-12-16 11:58:49'),
	(48, 'worker.alarm.erp', 'worker报警erp名单', 'xietingfeng;xiaofeng;', 3, '', '2014-12-23 17:13:28', '2015-10-26 17:24:24'),
	(49, 'jsf.serialization', 'JSF版本与序列化对应关系', '{"1000":{"list":["msgpack","hessian","java"]},"1001":{"list":["json","msgpack","hessian","java"]}}', 3, 'JSF版本与序列化对应关系', '2015-01-09 17:48:47', '2015-01-15 15:21:09'),
	(50, 'worker.alarm.consumer.threshold', 'consumer下线过多阈值', '5', 3, '', '2015-01-23 13:13:01', '2015-01-26 15:26:15'),
	(51, 'deploy.app.token', 'deploy.app.token', 'BE205043347C3C91B638DD4F1C52DB9B', 4, 'deploy.app.token', '2015-03-09 17:56:32', '2015-03-09 17:56:32'),
	(52, 'inst.operate.fordeploy.whitelist', 'deploy调用白名单', '10.12.122.17,192.168.162.16', 4, 'deploy调用白名单', '2015-03-18 11:47:59', '2015-03-30 11:22:49'),
	(53, 'counter.timeout', '计数器服务超时时间', '600', 1, '', '2015-04-27 11:56:44', '2015-07-22 13:22:03'),
	(54, 'jsf.ump.alarm', '是否采用ump报警方式', 'true', 4, '是否采用ump报警方式', '2015-05-14 16:06:02', '2015-06-04 15:21:40'),
	(56, 'monitor.iface.blacklist', 'monitor接口统计黑名单', 'com.ipd.jsf.gd.monitor.JSFMonitorService,com.ipd.jsf.service.BenchmarkService', 4, 'monitor接口统计黑名单', '2015-05-21 10:25:08', '2015-05-21 17:24:40'),
	(57, 'alarmworker.alarmnum', '关于报警worker的报警量增加阀值', '50', 4, '关于报警worker的报警量增加阀值', '2015-06-01 17:51:24', '2015-06-01 17:51:24'),
	(58, 'jsf.version.int', '注册中心检查客户端的JSF版本号', '1221', 3, '注册中心检查客户端的JSF版本号', '2015-06-16 15:50:35', '2015-06-24 14:25:48'),
	(59, 'jsf.version.string', '注册中心检查客户端的JSF版本号', '1.2.2', 3, '注册中心检查客户端的JSF版本号', '2015-06-16 15:51:01', '2015-06-24 14:25:42'),
	(61, 'callbacklog.switch', 'callback正常日志开关', 'true', 3, '控制注册中心保存callback正常日志,true-保存,false-不保存', '2015-10-23 18:28:36', '2015-10-26 18:26:36'),
	(62, 'telnet.switch', 'telnet开关', 'true', 4, '页面telnet开关，true-打开telnet，false-关闭telnet', '2015-10-26 19:03:17', '2015-10-27 10:29:54'),
	(63, 'logicdel.alarm.frequency', '逻辑删除前告警执行频率', '30', 2, '逻辑删除前告警执行频率(单位分钟)', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(64, 'logicdel.alarm.range', '逻辑删除前告警时间范围', '7', 2, '逻辑删除前告警时间范围', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(65, 'logicdel.alarm.switch', '逻辑删除前告警开关', '1', 2, '逻辑删除前告警开关 0 关 1 开', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(66, 'logicdel.alarm.contacts', '逻辑删除前告警接收人', '', 2, '逻辑删除前告警接收人 格式 mail:phone;mail:phone;.....', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(67, 'logicdel.alarm.threshold', '逻辑删除前告警阀值', '5', 2, '逻辑删除前告警阀值 5=5%', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(68, 'worker.tagtodel.interval', '逻辑删除时间间隔', '480', 2, '单位是分钟, 默认8小时(480分钟)', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(69, 'worker.tagtodel.switch', '逻辑删除节点开关', 'true', 2, '逻辑删除节点开关', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(70, 'worker.revivaldeadnode.switch', '复活死亡或逻辑删除节点开关', 'true', 2, '复活死亡或逻辑删除节点开关', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(71, 'worker.scandeadnode.switch', '扫描死亡节点开关', 'true', 2, '扫描死', '2016-03-31 12:34:56', '2016-03-31 12:34:56'),
	(72, 'monitor.source', '监控数据源', '1', 4, '逻辑删除前告警执行频率(单位分钟)', '2016-03-31 12:34:56', '2016-03-31 12:34:56');

INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (100,'监控管理-服务监控','控制【监控管理-服务监控】条目是否显示','/monitor/smonitor',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (101,'监控管理-模块显示','控制【监控管理】模块是否显示','/monitor',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (102,'服务管理-服务管理','控制服务信息-服务管理条目是否显示','/iface/manage',1,1,'bjxiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (103,'服务信息-模块显示','控制服务信息模块是否显示','/iface',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (104,'监控展示设置','展示设置','/monitor/display',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (105,'实例管理员（勿删）','实例管理员（勿删）','/saf_ins_admin',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (106,'服务路由管理（勿删）','服务路由管理','/manage/server/iface_router',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (107,'server实例停止consumer（勿删）','server实例停止consumer','/manage/server/iface_server_stopconsumer',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (108,'服务实例管理员（勿删）','服务实例管理员','/manage.*',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (109,'刷新provider列表（勿删）','刷新provider列表','/manage/client/iface_refresh_server',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (110,'刷新客户端配置（勿删）','刷新客户端配置','/manage/client/iface_refresh_clientcfg',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (111,'服务上下线（勿删）','服务上下线','/manage/server/iface_updown',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (112,'client端配置下发（勿删）','client端配置下发','/manage/client/iface_client_pincfg',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (113,'server端配置下发（勿删）','server端配置下发','/manage/server/iface_server_pincfg',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (114,'黑白名单权限（勿删）','黑白名单权限','/manage/server/iface_bw_list',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (115,'刷新注册中心缓存（勿删）','刷新注册中心缓存','/manage/server/iface_refresh_registry',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (116,'方法调用（勿删）','方法调用','/manage/server/method_invoke',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (117,'server实例管理（勿删）','server实例管理','/manage/server.*',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (118,'client实例管理（勿删）','client实例管理','/manage/client.*',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (119,'注册中心查看权限（勿删）','注册中心查看权限','/registry',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (120,'系统权限（勿删）','系统权限','/system',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (121,'上下线统计（勿删）','上下线统计','/saf/operate/onoffstat',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (122,'上下线记录（勿删）','上下线记录','/saf/operate/onoff',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (123,'接口管理(增删改)（勿删）','接口管理(增删改)','/iface/iface_info_admin',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (124,'项目主页条目(勿删)','项目主页条目','/monitor/phome',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (125,'服务实例增删（勿删）','服务实例增删（勿删）','/manage/server/iface_add_del',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (126,'SAF运营（勿删）','SAF运营','/saf/operate.*',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (127,'添加项目成员（勿删）','添加项目成员','/res_add_mem',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (128,'客户端切换alias（勿删）','客户端切换alias','/manage/client/change_alias',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (129,'JSF报表','jsf报表','/jsf/jsfchart',1,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (130,'接口app调用权限审核','接口app调用权限审核','/iface_app_invoke',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (131,'app调用监控配置','app调用监控配置','/manage/server/iface_app',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');
INSERT INTO `saf_privilege` (`id`,`name`,`desc`,`code`,`type`,`valid`,`modifier`,`modifiedTime`,`creator`,`createdTime`) VALUES (132,'动态分组','动态分组','/manage/server/iface_alias',2,1,'xiaofeng','2015-08-04 13:48:00','xiaofeng','2015-08-04 11:38:53');

/*系统依赖的接口都需要写在这里，提前初始化到数据库中去*/
INSERT INTO `saf_interface` (`id`, `interfaceName`, `groupId`, `department`, `ownerUser`, `important`, `hasJsfClient`, `cross_lang`, `remark`, `valid`, `syntozk`, `appInvoke`, `modifier`, `modifiedTime`, `creator`, `createdTime`, `src`, `uid`) VALUES
	(1, 'com.ipd.jsf.gd.monitor.MonitorService', NULL, '云平台', 'xiaofeng', 0, 0, 0, '', 1, 0, 0, 'xiaofeng', '2014-10-08 10:39:32', 'xiaofeng', '2014-10-08 10:39:32', 2, NULL),
	(2, 'com.ipd.jsf.gd.monitor.JSFMonitorService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-10-30 13:30:58', 'xiaofeng', '2014-10-30 13:30:58', 2, NULL),
	(3, 'com.ipd.jsf.service.RegistryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-11-07 12:56:54', 'xiaofeng', '2014-11-07 12:56:54', 2, NULL),
	(4, 'JsfEventBus', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-11-07 16:25:43', 'xiaofeng', '2014-11-07 16:25:43', 2, NULL),
	(5, 'com.ipd.jsf.service.RegistryCtrlService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-11-07 16:26:34', 'xiaofeng', '2014-11-07 16:26:34', 2, NULL),
	(6, 'com.ipd.jsf.service.RegistryQueryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-11-07 16:27:55', 'xiaofeng', '2014-11-07 16:27:55', 2, NULL),
	(7, 'com.ipd.jsf.service.RegistryStatusService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-11-07 16:28:40', 'xiaofeng', '2014-11-07 16:28:40', 2, NULL),
	(8, 'com.ipd.jsf.monitor.service.SafInterfaceDetailService', NULL, '云平台', 'xiaofeng;', 0, 0, 0, NULL, 1, 0, 0, 'xiaofeng', '2014-11-07 17:03:40', 'synzkdbworker', '2014-11-07 17:03:40', 1, NULL),
	(9, 'com.ipd.jsf.monitor.query.MonitorDataService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'influxdb 查询服务', 1, 0, 0, 'xiaofeng', '2014-11-11 11:36:52', 'xiaofeng', '2014-11-11 11:36:08', 2, NULL),
	(10, 'com.ipd.jsf.telnet.service.JsfIfaceInfoService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '取得接口描述', 1, 0, 0, 'zhangjunfeng7', '2014-12-11 11:14:51', 'zhangjunfeng7', '2014-12-11 11:14:51', 2, NULL),
	(12, 'com.ipd.jsf.monitor.service.MonitorRegistryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2014-12-25 15:18:25', 'xiaofeng', '2014-12-25 15:18:25', 2, NULL),
	(13, 'com.ipd.jsf.monitor.query.RegistryDataService', NULL, '云平台', 'xiaofeng', 0, 1, 2, '注册中心TopN数据查询接口', 1, 0, 0, 'xiaofeng', '2015-01-04 14:39:24', 'xiaofeng', '2014-12-29 16:33:38', 2, NULL),
	(14, 'com.ipd.jsf.serializer.jsf.JSFAlarmService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '', 1, 0, 0, 'xiaofeng', '2015-01-30 09:55:49', 'xiaofeng', '2015-01-30 09:55:49', 2, NULL),
	(15, 'com.ipd.jsf.benchmark.manage.RegistryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'benchmark测试', 1, 0, 0, 'zhangjunfeng7', '2015-02-10 16:00:10', 'zhangjunfeng7', '2015-02-10 16:00:10', 2, NULL),
	(18, 'com.ipd.jsf.unittest.JsfAutomationService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'jsf测试服务', 1, 0, 0, 'xiaofeng', '2015-03-10 12:02:11', 'xiaofeng', '2015-03-10 12:02:11', 2, NULL),
	(19, 'com.ipd.jsf.monitor.service.AppInvokeStatService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '监控统计', 1, 0, 0, 'xiaofeng', '2015-03-13 14:05:40', 'xiaofeng', '2015-03-13 14:05:40', 2, NULL),
	(21, 'com.ipd.jsf.deploy.app.InstOperateForDeployService', NULL, '云平台', 'xiaofeng', 0, 1, 2, 'deploy接口', 1, 0, 0, 'xiaofeng', '2015-03-18 10:31:39', 'xiaofeng', '2015-03-18 10:31:39', 2, NULL),
	(23, 'com.ipd.jsf.gw.GWServerRegistryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'jsf-http-gateway', 1, 0, 0, 'xiaofeng', '2015-03-27 18:00:27', 'xiaofeng', '2015-03-27 17:05:15', 2, NULL),
	(28, 'com.ipd.jsf.count.service.JSFCountService', NULL, '云平台', 'xietingfeng', 0, 1, 0, 'count', 1, 0, 0, 'xietingfeng', '2015-04-10 13:24:26', 'xietingfeng', '2015-04-10 13:24:26', 2, NULL),
	(29, 'com.ipd.jsf.Counter', NULL, '云平台', 'xietingfeng', 0, 1, 2, '调用次数计数器', 1, 0, 0, 'xietingfeng', '2015-04-13 18:12:17', 'xietingfeng', '2015-04-13 18:12:17', 2, NULL),
	(30, 'com.ipd.jsf.monitor.service.GWMonitorService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'http网关monitor数据接口', 1, 0, 0, 'zhangjunfeng7', '2015-04-16 13:39:43', 'zhangjunfeng7', '2015-04-16 13:39:43', 2, NULL),
	(31, 'com.ipd.jsf.gw.provider.HttpGWService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'http网关测试', 1, 0, 0, 'xiaofeng', '2015-04-17 15:04:31', 'xiaofeng', '2015-04-17 15:04:31', 2, NULL),
	(32, 'com.ipd.jsf.monitor.query.MonitorInfoQueryService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'jone monitor 接口', 1, 0, 0, 'xiaofeng', '2015-04-20 18:20:40', 'xiaofeng', '2015-04-20 18:20:40', 2, NULL),
	(33, 'com.ipd.jsf.monitor.query.MonitorGWDataService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'http网关monitor数据查询', 1, 0, 0, 'xiaofeng', '2015-04-22 14:20:37', 'xiaofeng', '2015-04-22 14:20:37', 2, NULL),
	(36, 'com.ipd.jsf.jos.service.InterfaceCheckService', NULL, '云平台', 'xiaofeng', 0, 1, 2, 'interface 监测', 1, 0, 0, 'xiaofeng', '2015-04-29 14:26:25', 'xiaofeng', '2015-04-29 14:26:25', 2, NULL),
	(37, 'com.ipd.jsf.monitor.query.InterfaceMapping', NULL, '云平台', 'xietingfeng', 0, 1, 0, 'com.ipd.jsf.monitor.query.InterfaceMapping', 1, 0, 0, 'xietingfeng', '2015-05-08 11:28:36', 'xietingfeng', '2015-05-08 11:28:36', 2, NULL),
	(38, 'com.ipd.jsf.service.RegistryHttpService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'http协议注册中心接口', 1, 0, 0, 'xiaofeng', '2015-05-08 15:52:00', 'xiaofeng', '2015-05-08 15:52:00', 2, NULL),
	(39, 'com.ipd.jsf.mq.service.JsfCapService', NULL, '云平台', 'xiaofeng', 0, 1, 0, '提供给mq的接口', 1, 0, 0, 'xiaofeng', '2015-05-12 10:45:02', 'xiaofeng', '2015-05-12 10:45:02', 2, NULL),
	(41, 'com.ipd.jsf.service.GWInterfaceDescService', NULL, '云平台', 'xiaofeng', 0, 1, 2, 'com.ipd.jsf.service.GWInterfaceDescService', 1, 0, 0, 'xiaofeng', '2015-05-18 17:14:46', 'xiaofeng', '2015-05-18 17:14:46', 2, NULL),
	(42, 'com.ipd.jsf.alarm.JSFAlarmService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'jsf报警服务', 1, 0, 0, 'xiaofeng', '2015-05-28 14:50:56', 'xiaofeng', '2015-05-28 14:50:56', 2, NULL),
	(43, 'com.ipd.jsf.monitor.service.ProviderAlarmStatService', NULL, '云平台', 'xietingfeng', 0, 1, 0, '服务统计报警', 1, 0, 0, 'xietingfeng', '2015-05-29 11:40:54', 'xietingfeng', '2015-05-29 11:40:54', 2, NULL),
	(45, 'com.ipd.jsf.dbcenter.service.DataCenterRegistryService', NULL, '云平台', 'makeyang,xiaofeng', 0, 1, 0, 'provider/consumer注册时访问db的方法', 1, 0, 0, 'makeyang', '2015-06-08 18:04:51', 'makeyang', '2015-06-08 18:04:51', 2, NULL),
	(46, 'com.ipd.jsf.lds.service.LocalDataStoreService', NULL, '云平台', 'xiaofeng', 0, 1, 0, 'JSF-LDS', 1, 0, 0, 'xiaofeng', '2015-07-03 10:28:12', 'xiaofeng', '2015-07-03 10:28:12', 2, NULL),
	(47, 'JsfIfaceServerService', NULL, '云平台', 'xiaofeng', 0, 0, 0, '', 1, 0, 0, 'xiaofeng', '2015-12-04 10:39:32', 'xiaofeng', '2015-12-04 10:39:32', 2, NULL),
	(48, 'JsfCheckHistoryService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-03-31 19:39:32', 'zhouzhiruo', '2016-03-31 10:39:32', 2, NULL),
  (49, 'com.ipd.app.health.monitor.client.service.AppHealthReportService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-03-31 19:39:32', 'zhouzhiruo', '2016-03-31 10:39:32', 2, NULL),
  (50, 'com.ipd.mobilePhoneMsg.sender.client.service.SmsMessageRpcService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-03-31 19:39:32', 'zhouzhiruo', '2016-03-31 10:39:32', 2, NULL),
  (51, 'com.ipd.official.omdm.is.hr.HrOrganizationService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-03-31 19:39:32', 'zhouzhiruo', '2016-03-31 10:39:32', 2, NULL),
  (52, 'com.ipd.official.omdm.is.hr.HrUserService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-03-31 19:39:32', 'zhouzhiruo', '2016-03-31 10:39:32', 2, NULL),
  (53, 'com.ipd.jsf.openapi.callLimit.ServerCallLimitService', NULL, '云平台', 'zhouzhiruo', 0, 0, 0, '', 1, 0, 0, 'zhouzhiruo', '2016-05-01 19:39:32', 'zhouzhiruo', '2016-05-01 10:39:32', 2, NULL),
  (54, 'com.ipd.jone.api.v2.service.JoneAppService', NULL, '持续交付创新部', 'bjdongxu', 0, 0, 0, '', 1, 0, 0, 'bjdongxu', '2017-08-26 01:36:50', 'bjdongxu', '2017-08-26 01:36:50', 2, NULL),
  (55,'com.ipd.jsf.monitor.query.MonitorRawDataService', NULL, '服务架构组', 'liuzheng8', 0, 0, 0, '', 1, 0, 0, 'liuzheng8', '2017-08-26 01:36:50', 'liuzheng8', '2017-08-26 01:36:50', 2, NULL),
  (56,'com.ipd.ssa.service.SsoService', NULL, '架构服务部', 'yechanglun', 0, 0, 0, '', 1, 0, 0, 'yechanglun', '2017-08-26 01:36:50', 'yechanglun', '2017-08-26 01:36:50', 2, NULL);

DROP TABLE IF EXISTS `saf_reg_health`;
CREATE TABLE `saf_reg_health` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `reg_addr` VARCHAR(50) NOT NULL COMMENT '注册中心地址',
  `hb_time` BIGINT(20) NOT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `reg_addr_idx` (`reg_addr`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT '注册中心心跳表，用于检测注册中心与db是否正常';

DROP TABLE IF EXISTS `saf_iface_invokehisto_hour`;
CREATE TABLE `saf_iface_invokehisto_hour` (
  `id` INT(11) UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '表主键',
  `interface_name` VARCHAR(100) NOT NULL COMMENT '接口名',
  `method` VARCHAR(100) NOT NULL COMMENT '方法名',
  `calltimes` int(11) NOT NULL COMMENT '访问量',
  `invoke_date` VARCHAR(30) NOT NULL  COMMENT '访问时间',
  `invoke_date_time` VARCHAR(30) NOT NULL COMMENT '访问小时时间',
  `create_time` DATETIME NOT NULL COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_saf_calltimes_date` (`interface_name`, `method`,`invoke_date`),
  INDEX `idx_saf_calltimes_hour` (`interface_name`, `method`,`invoke_date_time`)
)
COMMENT='接口历史调用量'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=1;
