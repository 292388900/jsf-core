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

public final class  DataEnum {
	
	public static enum IfaceStatus {
		deleted("已删除", 0),
		added("已添加", 1),
		tobeCommit("待提交", 2),
		tobeAdd("待审核", 3),
		reject("已驳回", 4);
	

		private String name;
		private int value;
	
		private IfaceStatus(String name, int value) {
			this.name = name;
			this.value = value;
		}
	
		public String getName() {
			return name;
		}
	
		public int getValue() {
			return value;
		}
	
	}
	
	
	
	public static enum RoleTypeEnum {
		MODEL("模块管理员", 1),
		RESOURCE("资源管理员", 2);
	

		private String name;
		private int value;
	
		private RoleTypeEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
	
		public String getName() {
			return name;
		}
	
		public int getValue() {
			return value;
		}
	
		public static RoleTypeEnum fromName(String name) {
			for (RoleTypeEnum rt : RoleTypeEnum.values()) {
				if (rt.name.equals(name))
					return rt;
			}
			return null;
		}
		
		public static RoleTypeEnum fromValue(int value) {
			for (RoleTypeEnum rt : RoleTypeEnum.values()) {
				if (rt.value == value)
					return rt;
			}
			return null;
		}
	}
	
	public static enum ResourceTypeEnum {
		EMPTY("空类型", 0),
		GROUP("项目", 1),
		INTERFACE("接口", 2),
		IP("IP", 3),
		APP("APP", 4);

		private String name;
		private int value;
		
		private ResourceTypeEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static ResourceTypeEnum fromName(String name) {
			for (ResourceTypeEnum rt : ResourceTypeEnum.values()) {
				if (rt.name.equals(name))
					return rt;
			}
			return null;
		}
		
		public static ResourceTypeEnum fromValue(int value) {
			for (ResourceTypeEnum rt : ResourceTypeEnum.values()) {
				if (rt.value == value)
					return rt;
			}
			return null;
		}
	}
	
	public static enum PrivilegeTypeEnum {
		MODEL("模块", 1),
		RESOURCE("资源", 2);
		
		private String name;
		private int value;
		
		private PrivilegeTypeEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static PrivilegeTypeEnum fromName(String name) {
			for (PrivilegeTypeEnum type : PrivilegeTypeEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static PrivilegeTypeEnum fromValue(int value) {
			for (PrivilegeTypeEnum type : PrivilegeTypeEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum IsAlarmedEnum {
		YES("是", 1),
		NO("否", 0);
		
		private String name;
		private int value;
		
		private IsAlarmedEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static IsAlarmedEnum fromName(String name) {
			for (IsAlarmedEnum type : IsAlarmedEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static IsAlarmedEnum fromValue(int value) {
			for (IsAlarmedEnum type : IsAlarmedEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum IsDisplayEnum {
		YES("是", 1),
		NO("否", 0);
		
		private String name;
		private int value;
		
		private IsDisplayEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static IsDisplayEnum fromName(String name) {
			for (IsDisplayEnum type : IsDisplayEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static IsDisplayEnum fromValue(int value) {
			for (IsDisplayEnum type : IsDisplayEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum IsRelatedEnum {
		YES("是", 1),
		NO("否", 0);
		
		private String name;
		private int value;
		
		private IsRelatedEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static IsRelatedEnum fromName(String name) {
			for (IsRelatedEnum type : IsRelatedEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static IsRelatedEnum fromValue(int value) {
			for (IsRelatedEnum type : IsRelatedEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum NoticeTypeEnum {
		AFFICHE("公告", 1),
		NEWS("最新动态", 2);
		
		private String name;
		private int value;
		
		private NoticeTypeEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static NoticeTypeEnum fromName(String name) {
			for (NoticeTypeEnum type : NoticeTypeEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static NoticeTypeEnum fromValue(int value) {
			for (NoticeTypeEnum type : NoticeTypeEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum ExceptionCodeEnum {
		PRIVILEGE("权限拒绝异常", 1),
		EXCEPTION("通用异常", 2);
		
		private String name;
		private int value;
		
		private ExceptionCodeEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static ExceptionCodeEnum fromName(String name) {
			for (ExceptionCodeEnum type : ExceptionCodeEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static ExceptionCodeEnum fromValue(int value) {
			for (ExceptionCodeEnum type : ExceptionCodeEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum WBList {
		whitelist("白名单", 1),
		blacklist("黑名单", 2);
		
		private String name;
		private int value;
		
		private WBList(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static WBList fromName(String name) {
			for (WBList type : WBList.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static WBList fromValue(int value) {
			for (WBList type : WBList.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	public static enum ServerStatusEnum {
		DEAD("死亡", 0),
		UP("在线", 1),
		DOWN("下线", 2);

		
		private String name;
		private int value;
		
		private ServerStatusEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static ServerStatusEnum fromName(String name) {
			for (ServerStatusEnum type : ServerStatusEnum.values()) {
				if (type.name.equals(name))
					return type;
			}
			return null;
		}
		
		public static ServerStatusEnum fromValue(int value) {
			for (ServerStatusEnum type : ServerStatusEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	//用户操作日志start
	public static enum UserActionEnum {
		SERVERONOFF("服务上下线", 0),
		SERVERDEL("服务节点删除", 1),
		SERVERADD("服务节点添加", 2),
		SERVERCLEAN("删除死亡节点", 3),
		INVOKECOMMOND("执行命令", 4),
		INSONOFF("实例上下线", 5),
		IMPORTSERVER("导出导出服务实例", 6),
		RELATEALIAS("动态分组设置", 7),
		CANCELALIAS("取消动态分组", 8),
		REFRESH_REG_CONF("刷新注册中心", 9),
		REFRESH_CONF("调用端刷新", 10),
		PINPOINT_CONF("配置推送", 11),
		ADD_BWLIST("添加黑白名单", 12),
		UPD_BWLIST("编辑黑白名单", 13),
		DEL_BWLIST("删除黑白名单", 14),
		INS_STOP_CONSUMER("停止consumer", 15),
		SERVER_STOP_CONSUMER("停止consumer", 15),
		INS_ADD_BWLIST("根据实例增加黑白名单", 16),
		IFACE_DEL("删除接口", 17),
		ROUTER_ADD("添加路由规则", 18),
		ROUTER_UPD("更新路由规则", 19),
		ROUTER_DEL("删除路由规则", 20),
		MONITOR_SETTING("更改monitor配置", 21),
		MONITOR_SETTING_DEL("删除monitor配置", 22),
		MOCK_SETTING("mock设置", 23),
		MOCK_SETTING_DEL("删除mock设置", 24),
		APPINVOKE_SETTING_DEL("删除APP调用设置", 25),
		IFACEGAEWAY_SETTING_DEL("删除网关调用设置", 26),
		INFLUXDB_ALLDOWN_OPTION("influxdb分片操作", 27),
		INFLUXDB_PARTIALDOWN_OPTION("influxdb分片操作", 27),
		INFLUXDB_ALLTRANSFER_OPTION("influxdb分片操作", 27),
		INFLUXDB_PARTIALTRANSFER_OPTION("influxdb分片操作", 27),
		INFLUXDB_SHARDMODIFY_OPTION("influxdb分片操作", 27),
		INFLUXDB_RECOVERY_OPTION("influxdb分片操作", 27),
		ADD_INDEX_RULE("Index Rule操作", 28),
		UPDOWN_INDEX_RULE("Index Rule操作", 28),
		IFACE_PREDEPLOY_SETTING("接口预发布设置", 29),
		PROPERTY_SETTING("属性配置", 30),
		TELNETCOMMOND("TELNET命令", 31),
		OPENAPI_RELATEALIAS("动态分组设置-开放API",32),
		OPENAPI_LIMITFLOW("服务端限流-开放API",33),
		OPENAPI_IFACEDETAILS("获取服务接口详情-开放API",34),
		OPENAPI_DEPINFO("获取依赖/被依赖关系-开放API",35),
		IFACE_ERP_OPERATION("接口人员权限修改操作",36),
		OPENAPI_WEIGHT("服务端权重设置-开放API",37);

		private String name;
		private int value;
		
		private UserActionEnum(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static UserActionEnum fromName(String name) {
			for (UserActionEnum type : UserActionEnum.values()) {
				if (type.name().equals(name))
					return type;
			}
			return null;
		}
		
		public static UserActionEnum fromValue(int value) {
			for (UserActionEnum type : UserActionEnum.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
	
	//用户操作日志end

	//报警类型start
/**
	public static enum AlarmType {
		SERVERONOFF("服务上下线", 0),
		PROVIDERTHRESHOLD("provider阀值", 1),
		CONSUMERTHRESHOLD("consumer阀值", 2),
		INVOKETHRESHOLD("方法调用次数阀值", 3),
		IFACEONOFFSTAT("服务上下线统计", 4),
		REGISTRYALIVE("注册中心存活", 5),
		INSTANCEONOFF("实例上下线", 6),
        WORKEREXECUTEFAILED("worker执行失败",7),
		MONITORSTATUS("监控服务端状态",8),
		NOINSPROVIDERALIVE("provider无实例但存活",9),
		MORELOSSCONSUMER("consumer下线过多",10),
        SERVICEPCALLTIMIEALARM("PROVIDER 调用次数报警",11),
        SERVICEERRORALARM("PROVIDER 异常次数报警",12),
        SERVICETPSALARM("PROVIDER 调用耗时报警",13),
        JSFHTTPGWALARM("JSF Http GW 异常次数报警",14),
        ALARMWORKERCHECK("报警WORKER状态",15),
        //Deprecated
        CONSUMERERRORALARM("CONSUMER 异常次数报警",16),
        CONSUMERTPSALARM("CONSUMER 调用耗时报警",17),
		LDSALARM("LDS与DB数据不一致报警",18);

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
*/
	
	//报警类型end
	
	public static enum SysParamType {
		All("所有类型", 0),
		CLientConfig("客户端配置", 1),
		GlobalConfig("全局配置", 2),
		Worker("worker配置", 3),
		AdminWeb("管理端配置", 4),
	    GateWay("网关配置", 5);
		
		private String name;
		private int value;
		
		private SysParamType(String name, int value) {
			this.name = name;
			this.value = value;
		}
		
		public String getName() {
			return name;
		}
		
		public int getValue() {
			return value;
		}
		
		public static SysParamType fromName(String name) {
			for (SysParamType type : SysParamType.values()) {
				if (type.name().equals(name))
					return type;
			}
			return null;
		}
		
		public static SysParamType fromValue(int value) {
			for (SysParamType type : SysParamType.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}


    public static enum AliasType {
        Roadmap("分组替换", 1),
        Extend("分组追加", 2);

        private String name;
        private int value;
        
        private AliasType(String name, int value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public int getValue() {
            return value;
        }
        
        public static AliasType fromName(String name) {
            for (AliasType type : AliasType.values()) {
                if (type.name().equals(name))
                    return type;
            }
            return null;
        }
        
        public static AliasType fromValue(int value) {
            for (AliasType type : AliasType.values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }
    
    public static enum RouterType {
    	ipRouter("IP路由", 1),
    	paramRouter("参数路由", 2),
    	aliasRouter("分组路由", 3);

    	private String name;
    	private int value;
    	
    	private RouterType(String name, int value) {
    		this.name = name;
    		this.value = value;
    	}
    	
    	public String getName() {
    		return name;
    	}
    	
    	public int getValue() {
    		return value;
    	}
    	
    	public static RouterType fromName(String name) {
    		for (RouterType type : RouterType.values()) {
    			if (type.name().equals(name))
    				return type;
    		}
    		return null;
    	}
    	
    	public static RouterType fromValue(int value) {
    		for (RouterType type : RouterType.values()) {
    			if (type.value == value)
    				return type;
    		}
    		return null;
    	}
    }

    public static enum ScanStatusLogType {
        server("provider", (byte)1),
        instance("instance", (byte)2);

        private String name;
        private byte value;
        
        private ScanStatusLogType(String name, byte value) {
            this.name = name;
            this.value = value;
        }
        
        public String getName() {
            return name;
        }
        
        public byte getValue() {
            return value;
        }
        
        public static ScanStatusLogType fromName(String name) {
            for (ScanStatusLogType type : ScanStatusLogType.values()) {
                if (type.name().equals(name))
                    return type;
            }
            return null;
        }
        
        public static ScanStatusLogType fromValue(int value) {
            for (ScanStatusLogType type : ScanStatusLogType.values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }
    
    public static enum RoomStrategyType {
        //0-无配置，1-推送同机房节点，2-放大同机房节点权重
        noStrategy("noStrategy", (byte)0),
        sameRoom("sameRoom", (byte)1),
        weightFactor("weightFactor", (byte)2);

        private String name;
        private byte value;

        private RoomStrategyType(String name, byte value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public byte getValue() {
            return value;
        }

        public static RoomStrategyType fromName(String name) {
            for (RoomStrategyType type : RoomStrategyType.values()) {
                if (type.name().equals(name))
                    return type;
            }
            return null;
        }

        public static RoomStrategyType fromValue(byte value) {
            for (RoomStrategyType type : RoomStrategyType.values()) {
                if (type.value == value)
                    return type;
            }
            return null;
        }
    }

	public static enum AccountType {
		UNKNOWN("未知", 0),
		SSO("单点登录", 1),
		Register("注册账户", 2);

		private String name;
		private int value;

		private AccountType(String name, int value) {
			this.name = name;
			this.value = value;
		}

		public String getName() {
			return name;
		}

		public int getValue() {
			return value;
		}

		public static AccountType fromName(String name) {
			for (AccountType type : AccountType.values()) {
				if (type.name().equals(name))
					return type;
			}
			return null;
		}

		public static AccountType fromValue(int value) {
			for (AccountType type : AccountType.values()) {
				if (type.value == value)
					return type;
			}
			return null;
		}
	}
}
