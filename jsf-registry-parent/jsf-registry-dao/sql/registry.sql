CREATE DATABASE IF NOT EXISTS saf21 CHARACTER SET utf8;

CREATE TABLE IF NOT EXISTS saf_ins_hb (
  id INT UNSIGNED NOT NULL AUTO_INCREMENT,
  ins_ip VARCHAR(16) NOT NULL COMMENT 'saf实例IP',
  ins_pid SMALLINT UNSIGNED NOT NULL COMMENT 'saf实例pid',
  hb_time DATETIME NOT NULL COMMENT '心跳时间',
  ins_status TINYINT UNSIGNED NOT NULL COMMENT '实例状态',
  start_time DATETIME NOT NULL COMMENT 'saf实例启动时间',
  create_time DATETIME NOT NULL COMMENT '记录创建时间',
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = '实例心跳表。记录实例心跳时间';

CREATE TABLE IF NOT EXISTS saf_server (
  server_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  server_uniquekey VARCHAR(128) NOT NULL COMMENT 'ip;port;alias(group:version);protocol;interfaceId , 将这5个字段作为provider的唯一判断和索引，在此不做唯一约束，通过程序进行判断',
  interface_id INT(10) UNSIGNED NOT NULL,
  interface_name VARCHAR(255) NOT NULL,
  server_ip VARCHAR(16) NOT NULL COMMENT 'server的IP地址',
  server_port SMALLINT(5) UNSIGNED NOT NULL COMMENT 'server的端口号',
  server_pid SMALLINT(5) UNSIGNED NOT NULL COMMENT 'server的PID',
  server_alias VARCHAR(32) NOT NULL COMMENT '服务别名。对应saf1.0的group+version',
  server_status TINYINT(2) UNSIGNED NOT NULL,
  server_room TINYINT(2) UNSIGNED NOT NULL,
  server_timeout INT(8) UNSIGNED NULL,
  server_weight INT(6) UNSIGNED NULL,
  server_apppath VARCHAR(128) NOT NULL,
  protocol VARCHAR(16) NULL,
  context_path VARCHAR(128) NULL,
  saf_version VARCHAR(16) NOT NULL,
  is_random TINYINT(1) UNSIGNED NOT NULL,
  src_type TINYINT(2) UNSIGNED NOT NULL COMMENT 'data type: 1-registry, 2-manual, 3-zookeeper 用于数据同步',
  attr_url VARCHAR(255) NULL COMMENT '保存safurl中的attr参数，过滤掉一些在表中已经存在的参数',
  safurl_desc VARCHAR(1023) NULL,
  start_time DATETIME NOT NULL,
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  PRIMARY KEY (server_id),
  INDEX query_index_server_uniquekey (server_uniquekey ASC),
  UNIQUE INDEX server_uniquekey_UNIQUE (server_uniquekey ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'provider信息表';

CREATE TABLE IF NOT EXISTS saf_client (
  client_id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  client_uniquekey VARCHAR(128) NOT NULL COMMENT 'ip;pid;alias(group:version);interfaceId , 将这4个字段作为consumer的唯一判断和索引。为了降低唯一约束对在此不做唯一约束，通过程序进行唯一判断',
  interface_id INT(10) UNSIGNED NOT NULL,
  interface_name VARCHAR(255) NOT NULL,
  client_ip VARCHAR(16) NOT NULL,
  client_pid SMALLINT UNSIGNED NOT NULL,
  client_alias VARCHAR(32) NOT NULL COMMENT '服务别名。对应saf1.0的group+version',
  client_apppath VARCHAR(128) NULL,
  client_status TINYINT(2) UNSIGNED NOT NULL,
  saf_version VARCHAR(16) NOT NULL,
  src_type TINYINT(2) UNSIGNED NOT NULL COMMENT '数据来源：1-proxy, 2-manual, 3-zookeeper',
  safurl_desc VARCHAR(1023) NULL,
  start_time VARCHAR(32) NOT NULL COMMENT '节点启动时间',
  create_time DATETIME NOT NULL,
  update_time DATETIME NOT NULL,
  PRIMARY KEY (client_id),
  INDEX query_index_client_uniquekey (client_uniquekey ASC),
  UNIQUE INDEX client_uniquekey_UNIQUE (client_uniquekey ASC))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = 'consumer信息表';

CREATE TABLE IF NOT EXISTS saf_server_alias (
  id INT UNSIGNED NOT NULL,
  server_id INT UNSIGNED NOT NULL COMMENT 'server  id',
  interface_id INT UNSIGNED NOT NULL COMMENT '接口id',
  alias_name VARCHAR(32) NOT NULL COMMENT '服务别名。对应saf1.0的group+version',
  src_type TINYINT UNSIGNED NOT NULL,
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = '服务别名分组表。保存管理端添加的组';


CREATE TABLE IF NOT EXISTS saf_ipwb (
  id INT UNSIGNED NOT NULL,
  interface_id INT UNSIGNED NOT NULL COMMENT '接口id',
  ip_addr VARCHAR(16) NOT NULL,
  wb_type TINYINT UNSIGNED NULL COMMENT '被白名单类型:  1-white,2-black',
  pc_type TINYINT UNSIGNED NULL COMMENT '1-provider, 2-consumer',
  PRIMARY KEY (id))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8
COMMENT = '连接注册中心的黑白名单表';



