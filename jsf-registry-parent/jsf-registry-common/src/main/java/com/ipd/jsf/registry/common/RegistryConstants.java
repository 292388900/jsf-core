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
package com.ipd.jsf.registry.common;


public class RegistryConstants {
    public final static String SPLITSTR_COLON = ":";
    public final static String SPLITSTR_COMMA = ",";
    public final static String SPLITSTR_UNDERLINE = "_";
    public final static String SPLITSTR_EQUALMARK = "=";
    public final static String GLOBALSETTING_VERSION = "globalsetting.version";  //全局配置版本号关键字
    public final static String APPPATH = "apppath";   //实例路径关键字
    public final static String LANGUAGE = "language";  //客户端语言关键字
    public final static String SAFVERSION = "safVersion";  //saf客户端版本号关键字
    public final static String JSFVERSION = "jsfVersion";  //jsf客户端版本号关键字
    public final static String TIMESTAMP = "timestamp";  //jsf客户端启动时间关键字
    public final static String CONTEXTPATH = "ctxpath";   //jsf客户端context path关键字
    public final static String WEIGHT = "weight";      //jsf客户端权重关键字
    public final static String DYNAMIC = "dynamic";    //jsf客户端灰度上线关键字
    public final static String REREG = "re-reg";    //jsf客户端灰度上线关键字, 是否第一次注册
    public final static String SERIALIZATION = "serialization";  //jsf客户端序列化关键字
    public final static String CONSUMER = "consumer";   //消费者
    public final static String CONSUMERID = "id";   //消费者
    public final static String VISITNAME = "visitname";   //限制访问注册中心的多语言客户端应用名
    public final static String APPNAME = "appName";   //自动部署系统提供的应用名
    public final static String APPID = "appId";   //自动部署系统提供的应用编号
    public final static String APPINSID = "appInsId";   //自动部署系统提供的实例编号

    public final static int SAFVERSION_VALUE = 210;    //saf客户端版本号
    public final static String JSFVERSION_VALUE = "1001";   //jsf客户端版本号
    public final static int JSFVERSION_IVALUE = 1001;   //jsf客户端版本号
    public final static long ORIGINAL_TIME = 1;
    public final static String APPNAME_VALUE = null;   //自动部署系统提供的应用名
    public final static int APPID_VALUE = 0;   //自动部署系统提供的应用编号
    public final static String APPINSID_VALUE = "";   //自动部署系统提供的实例编号
    
    //启动实例的参数名
    public static final String SAF_INSTANCE = "saf_instance";
    //启动实例调用的脚本名,比如inst\saf01.properties
    public static final String SAF_INSTANCE_VALUE = "saf01";

    public static final String PREFIX_REGISTRY = "reg_";
    public static final String PREFIX_UNREGISTRY = "unreg_";

    /** 同机房优先策略 **/
    public static final String ROOM_STRATEGY = "room.strategy";

    /** provider限制数量 **/
    public static final String PROVIDER_LIMIT = "p.limit.cnt";

    /** 机房权重系数 **/
    public static final String ROOM_WEIGHT_FACTOR = "room.weight.factor";

    /** 是否开启berkeleyDB-provider **/
    public static final String ISOPEN_BERKELEYDB_PROVIDER = "bdb.open.switch.provider";

    /** 是否开启berkeleyDB-总开关，如果设置为false，provider,consumer,instance都会关闭, 如果为true, consumer,instance使用,provider视自己开关而定 **/
    public static final String ISOPEN_BERKELEYDB = "bdb.open.switch";

    /** 使用clientId生成方法开关，true-使用clientId生成方法，false-不使用  **/
    public static final String GEN_CLIENTID_SWITCH_KEY = "gen.clientid.switch";

    /** JSF版本与序列化对应关系 **/
    public static final String JSF_SERIALIZATION = "jsf.serialization";

    /** JSF版本号，整型 **/
    public static final String JSF_CONFIG_VERSION_INT = "jsf.version.int";

    /** JSF版本号，字符串型 **/
    public static final String JSF_CONFIG_VERSION_STRING = "jsf.version.string";

    /** callback正常日志保存开关 **/
    public static final String CALLBACKLOG_SWITCH = "callbacklog.switch";
    
    /** mock key **/
    public static final String MOCK_KEY = "invoke.mockresult";
    
    /** appiface key **/
    public static final String APPIFACE_KEY = "invoke.applimit";

    /** 重新recover关键字  **/
    public static final String HB_RECOVER = "recover";

    /** 重新注册callback关键字 **/
    public static final String HB_CALLBACK = "callback";

    /** 心跳无需检查 **/
    public static final String HB_UNCHECK = "uncheck";

    public static final int SERVER_NOT_EXIST = 0;
    
    public static final int SERVER_EXIST = 1;
    
    public static final int SERVER_UPDATEVERSION = 2;

    public static final String SETTING_MAP_PARAM_ALIAS = "map.param.alias";

    /** 别名最大长度 */
    public static final int MAX_ALIAS_LENGTH = 60;

    public static final String UMP_DOREGISTER = "registry.doRegister";

    public static final String UMP_DOUNREGISTER = "registry.doUnRegister";

    public static final String UMP_DOSUBSCRIBE = "registry.doSubscribe";

    public static final String UMP_DOUNSUBSCRIBE = "registry.doUnSubscribe";

    public static final String UMP_LOOKUP = "registry.lookup";

    public static final String UMP_LOOKUPLIST = "registry.lookuplist";

    public static final String UMP_DOHEARTBEAT = "registry.doHeartbeat";

    public static final String UMP_SUBSCRIBECONFIG = "registry.subscribeConfig";

    public static final String UMP_GETCONFIG = "registry.getConfig";

    public static final String UMP_GETCONFIGLIST = "registry.getConfigList";

    public static final String UMP_DOREGISTERLIST = "registry.doRegisterList";

    public static final String UMP_DOUNREGISTERLIST = "registry.doUnRegisterList";
}
