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
package com.ipd.jsf.worker.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.ipd.jsf.worker.dao.JsfCheckHistoryDao;
import com.ipd.jsf.worker.log.dao.JsfRegAddrDao;
import com.ipd.jsf.worker.service.common.URL;
import com.ipd.jsf.worker.util.AlarmUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.ipd.jsf.alarm.JSFAlarmService;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.gd.util.NetUtils;
import com.ipd.jsf.common.constant.HeartbeatConstants;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.util.GenerateRegular;
import com.ipd.jsf.common.util.SafTelnetClient;
import com.ipd.jsf.worker.common.PropertyFactory;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.IfaceAlarm;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.JsfAlarmHistory;
import com.ipd.jsf.worker.domain.JsfIns;
import com.ipd.jsf.worker.domain.JsfRegAddr;
import com.ipd.jsf.worker.domain.RegHealthInfo;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.domain.ServiceTraceLog;
import com.ipd.jsf.worker.domain.SysParam;
import com.ipd.jsf.worker.manager.IfaceAlarmManager;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import com.ipd.jsf.worker.manager.RegHbManager;
import com.ipd.jsf.worker.manager.ServiceTraceLogManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import com.ipd.jsf.worker.util.WorkerAppConstant;

public class ScanNodeStatusHelper {

    private Logger logger = LoggerFactory.getLogger(ScanNodeStatusHelper.class);
    //telnet不通
    public final byte TELNET_NOTCONNECT = 0;

    //telnet通，且节点存在
    public final byte TELNET_OK = 1;

    //telnet通，但节点不存在
    public final byte TELNET_NOTEXIST = 2;

    //上线
    public final String TYPE_ON = "on";

    //下线
    public final String TYPE_OFF = "off";

    private final int COMMAND_TYPE_LS = 1;

    private final int COMMAND_TYPE_CONF = 2;
    
    private final String PREFIX_TYPE = "T-";
    
    private final String PREFIX_JSF = "JSF";

    private final String PREFIX_REG = "REG-";

    private final String PREFIX_INS = "INS-";

    private final String TELNET = "telnet_yes";

    //报警erp参数的key，对应参数表的key
    private final String WORKER_ALARM_ERP = "worker.alarm.erp";

    //扫描死亡节点开关
    private final String WORKER_SCANDEADNODE_SWITCH = "worker.scandeadnode.switch";

    //复活死亡或逻辑删除节点开关
    private final String WORKER_REVIVALDEADNODE_SWITCH = "worker.revivaldeadnode.switch";

    //逻辑删除节点时间间隔
    private final String WORKER_TAGTODEL_INTERVAL = "worker.tagtodel.interval";

    //telnet连接超时时间
    private int connTimeout = 2000;

    //telnet读取超时时间
    private int readTimeout = 2000;
    
    //保存注册中心地址
    private List<String> registryList = new ArrayList<String>();

    //记录没有心跳的注册中心
    private List<String> unCheckRegistryList = new ArrayList<String>();

    //获取没有心跳的注册中心是否成功
    public volatile boolean unCheckRegistryFlag = true;

    //报警erp的值，对应参数表的value
    public String workerAlarmErps = "";

    //注册中心心跳检查间隔
    private int registryInterval = 2 * 60 * 1000;

    private int alarmLimit = 10;

    private int checkTimeInterval = 30 * 60 * 1000;

    //标记死亡节点开关
    public boolean scanDeadNodeSwitch = true;

    //复活死亡或逻辑删除节点开关
    public boolean revivalNodeSwitch = true;

    //默认执行workerType
    public static volatile String defaultWorkerAlias = "LangFangScanNodeStatusWorker";

    //日志队列
    private LinkedBlockingQueue<ServiceTraceLog> logQueue = new LinkedBlockingQueue<ServiceTraceLog>(30000);

    //接口和erp负责人map，key：interfaceId
    public Map<Integer, InterfaceInfo> ifaceErpMap = new HashMap<Integer, InterfaceInfo>();

    //接口报警规则,key:interfaceName,value:IfaceAlarm list
    public ConcurrentHashMap<String, List<IfaceAlarm>> ifaceAlarmRuleMap = new ConcurrentHashMap<String, List<IfaceAlarm>>();

    //保存每次telnet provider后的结果，key:"IP:PORT", value:"uniqkey"
    private Map<String, Set<String>> instanceServerMap = new ConcurrentHashMap<String, Set<String>>();

    //报警线程池
    private ExecutorService alarmPool = Executors.newFixedThreadPool(2, new WorkerThreadFactory("scanNodeStatus_alarmPool"));

    private IfaceAlarmManager ifaceAlarmManager;

    private ServiceTraceLogManager serviceTraceLogManager;

    private SysParamManager sysParamManager;

    private InterfaceInfoManager interfaceInfoManager;

    private JsfRegAddrDao jsfRegAddrDao;

    private RegHbManager regHbManager;

    private JSFAlarmService jsfAlarmService;

    public void init(ServiceTraceLogManager serviceTraceLogManager,
            IfaceAlarmManager ifaceAlarmManager,
            SysParamManager sysParamManager,
            InterfaceInfoManager interfaceInfoManager,
            JsfRegAddrDao jsfRegAddrDao,
            RegHbManager regHbManager,
            JsfCheckHistoryDao jsfCheckHistoryDao) {
        this.serviceTraceLogManager = serviceTraceLogManager;
        this.ifaceAlarmManager = ifaceAlarmManager;
        this.sysParamManager = sysParamManager;
        this.interfaceInfoManager = interfaceInfoManager;
        this.jsfRegAddrDao = jsfRegAddrDao;
        this.regHbManager = regHbManager;
        this.jsfAlarmService = jsfAlarmService;
        connTimeout = PropertyFactory.getProperty(WorkerAppConstant.TELNET_CONN_TIMEOUT, 3000);
        readTimeout = PropertyFactory.getProperty(WorkerAppConstant.TELNET_READ_TIMEOUT, 3000);
        saveLogSchedule();
    }

    public int getQueueSize() {
        return logQueue.size();
    }

    public void clearData() {
        instanceServerMap.clear();
    }

    /**
     * 获取注册中心地址
     */
    private void loadRegistryList() {
        try {
            //如果为空，才获取
            List<JsfRegAddr> list = jsfRegAddrDao.listAll(); //TODO
            List<String> tmpRegistryList = new ArrayList<String>();
            if (list != null) {
                for (JsfRegAddr addr : list) {
                    tmpRegistryList.add(getIpPort(addr.getIp(), addr.getPort()));
                }
            }
            this.registryList.clear();
            this.registryList.addAll(tmpRegistryList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getIpPort(String ip, int port) {
        return ip + ":" + port;
    }

    /**
     * 获取无心跳的注册中心
     */
    private void loadUncheckRegistryList() {
        if (registryList == null || registryList.isEmpty()) {
            unCheckRegistryFlag = false;
            return;
        }
        try {
            //获取标记忽略逻辑删除的注册中心
            List<JsfRegAddr> list = jsfRegAddrDao.getUnCheckList();
            List<String> tmpRegistryList = new ArrayList<String>();
            if (list != null) {
                for (JsfRegAddr addr : list) {
                    tmpRegistryList.add(getIpPort(addr.getIp(), addr.getPort()));
                }
            }

            unCheckRegistryList.clear();
            if (list != null && !list.isEmpty()) {
                unCheckRegistryList.addAll(tmpRegistryList);
            }
            unCheckRegistryFlag = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            unCheckRegistryFlag = false;
            return;
        }
        //查出访问注册中心异常的记录
        try {
            long endTime = System.currentTimeMillis();
            long regHealthCheckRange = 15 * 60 * 1000L; //15分钟
            //心跳时间范围, 当前时间向前推多长时间
            String regHealthCheckRangeStr = sysParamManager.findValueBykey("reg.health.check.timerange");
            if (regHealthCheckRangeStr != null && !regHealthCheckRangeStr.isEmpty()) {
                try {
                    regHealthCheckRange = Long.parseLong(regHealthCheckRangeStr);
                } catch (Exception e) {
                    logger.warn("parse param {} error = {}!", "reg.health.check.timerange", e);
                }
            }
            long startTime = endTime - regHealthCheckRange - HeartbeatConstants.REG_HB_INTERVAL;
            long commonHbCount = regHealthCheckRange / HeartbeatConstants.REG_HB_INTERVAL; //正常心跳次数

            //按注册中心分组统计心跳数,
            //如果结果为空,所有注册中心都可能存在问题;
            //如果结果少于注册中心总数, 不在总数中的注册中心是可能存在问题的
            List<RegHealthInfo> regHealthInfos = regHbManager.getUncheckRegList(startTime, endTime);
            if (regHealthInfos != null && !regHealthInfos.isEmpty()) {
                Map<String, Integer> regHealthMap = new HashMap<String, Integer>();
                for (RegHealthInfo regHealthInfo : regHealthInfos) {
                    regHealthMap.put(regHealthInfo.getRegAddr(), regHealthInfo.getHbCount());
                }
                List<String> tmpList = new ArrayList<String>();
                for (String regAddr : registryList) {
                    Integer currHbCount = regHealthMap.get(regAddr);
                    logger.info("RegHealthInfo : regHealthCheckRange={}, currTime={}, startTime={}, commonHbCount={}, RegAddr={}, currHbCount={}",
                            regHealthCheckRange, new Date(endTime), new Date(startTime), commonHbCount, regAddr, currHbCount);

                    boolean isUnCheck = false;
                    if (currHbCount == null) {
                        isUnCheck = true;
                    } else {
                        if (commonHbCount > currHbCount.intValue()) {
                            isUnCheck = true;
                        }
                    }
                    if (isUnCheck && !unCheckRegistryList.contains(regAddr) && !tmpList.contains(regAddr) && registryList.contains(regAddr)) {
                        tmpList.add(regAddr);
                    }
                }
                if (!CollectionUtils.isEmpty(tmpList)) {
                    unCheckRegistryList.addAll(tmpList);
                }
                if (registryList.size() == unCheckRegistryList.size()) {
                    unCheckRegistryFlag = false;
                } else {
                    unCheckRegistryFlag = true;
                }
            } else {
                unCheckRegistryFlag = false;
                unCheckRegistryList.addAll(new ArrayList<String>(registryList));
            }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			unCheckRegistryFlag = false;
		}
    }

    /**
     * 加载erp信息
     */
    public void loadInterface() {
        try {
            List<InterfaceInfo> list = interfaceInfoManager.getErps();
            for (InterfaceInfo iface : list) {
                ifaceErpMap.put(iface.getInterfaceId(), iface);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 批量保存server的上下线日志
     * @param list
     * @param onoffType
     */
    public void recordServerTraceLog(List<Server> list , byte onoffType) {
        if (list != null && !list.isEmpty()) {
            for (Server server : list) {
				recordTraceLog(server.getInterfaceName(), server.getProtocol(), server.getIp(), server.getPid(), server.getPort(), server.getAlias(), ServiceTraceLog.provider, onoffType);
            }
        }
    }

    /**
     * 批量保存client的上下线日志
     * @param list
     * @param onoffType
     */
    public void recordClientTraceLog(List<Client> list , byte onoffType) {
        if (list != null && !list.isEmpty()) {
            for (Client client : list) {
				recordTraceLog(client.getInterfaceName(), client.getProtocol(), client.getIp(), client.getPid(), 0, client.getAlias(), ServiceTraceLog.consumer, onoffType);
            }
        }
    }

    /**
     * 记录上下线日志，放入队列
     * @return
     */
	public void recordTraceLog(String interfaceName, int protocol, String ip, int pid, int port, String alias, byte pcType, byte onoffType) {

            ServiceTraceLog log = new ServiceTraceLog();
            log.setIp(ip);
            log.setPid(pid);
            log.setInterfaceName(interfaceName);
            log.setAlias(alias);
            log.setPort(port);
            log.setEventTime(new Date());
            log.setOnoffType(onoffType);
            try {
                log.setProtocol(ProtocolType.valueOf(protocol).name());
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
                log.setProtocol(ProtocolType.jsf.name());
            }
            log.setSafVer(210);//old magic code
            log.setPcType(pcType);
            log.setCreator("status-scan-worker");
            logQueue.add(log);

    }






    /**
     * 保存日志
     */
    private void saveLogSchedule() {
        ExecutorService singleService = Executors.newSingleThreadExecutor(new NamedThreadFactory("trace_log"));
        singleService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        saveTraceLog();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                
            }
        });
    }

    /**
     * 保存上下线日志
     */
    private void saveTraceLog() {
        int limitTime = 15000;
        int limit = 20;
        long lastTime = System.currentTimeMillis(); 
        List<ServiceTraceLog> list = new ArrayList<ServiceTraceLog>();
        while (true) {
            if (logQueue.isEmpty()) {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            try {
                ServiceTraceLog log = logQueue.poll(100, TimeUnit.MILLISECONDS);
                if (log != null) {
                    list.add(log);
                }
                if (list.size() >= limit || (System.currentTimeMillis() - lastTime) > limitTime) {
                    break;
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (!list.isEmpty()) {
            try {
                serviceTraceLogManager.create(list);
                logger.info("保存服务上下线日志: {}", list.size());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * 加载配置信息
     */
    public void loadProperty() {
        try {
            loadRegistryList();
            loadUncheckRegistryList();
            logger.warn("unCheckRegistryList:{}", unCheckRegistryList);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        try {
        	SysParam temp = sysParamManager.get(WORKER_ALARM_ERP, DataEnum.SysParamType.Worker.getValue());
        	if (temp != null) {
        		workerAlarmErps = temp.getValue();
        	}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.info("worker报警erp名单：{}", workerAlarmErps);
		}
        try {
            SysParam temp1 = sysParamManager.get(WORKER_SCANDEADNODE_SWITCH, DataEnum.SysParamType.Worker.getValue());
            if (temp1 != null) {
            	scanDeadNodeSwitch = Boolean.parseBoolean(temp1.getValue());
            }
        } catch (Exception e) {
        	scanDeadNodeSwitch = true;
        	logger.error(e.getMessage(), e);
        } finally {
        	logger.info("扫描死亡节点开关：{}", scanDeadNodeSwitch);
        }
        try {
            SysParam temp2 = sysParamManager.get(WORKER_REVIVALDEADNODE_SWITCH, DataEnum.SysParamType.Worker.getValue());
            if (temp2 != null) {
            	revivalNodeSwitch = Boolean.parseBoolean(temp2.getValue());
            }
        } catch (Exception e) {
        	revivalNodeSwitch = true;
        	logger.error(e.getMessage(), e);
        } finally {
        	logger.info("复活死亡和逻辑删除节点开关：{}", revivalNodeSwitch);
        }

        try {
            SysParam temp4 = sysParamManager.get(WORKER_TAGTODEL_INTERVAL, DataEnum.SysParamType.Worker.getValue());
            if (temp4 != null) {
            	HeartbeatConstants.DELETE_DEAD_INSTANCE_FROM_DB_TIME = Integer.parseInt(temp4.getValue()) * 60 * 1000;  //页面配置的是分钟，要转换为毫秒
            }
        } catch (Exception e) {
        	HeartbeatConstants.DELETE_DEAD_INSTANCE_FROM_DB_TIME = HeartbeatConstants.DELETE_DEAD_INSTANCE_FROM_DB_TIME_DEFAULT;
        	logger.error(e.getMessage(), e);
        } finally {
        	logger.info("逻辑删除节点时间间隔：{}", HeartbeatConstants.DELETE_DEAD_INSTANCE_FROM_DB_TIME);
        }
    }

    /**
     * 加载报警规则
     */
    public void loadIfaceAlarmRule() {
        try {
            List<Integer> typeList = new ArrayList<Integer>();
            typeList.add(0);
            List<IfaceAlarm> list = ifaceAlarmManager.getListByAlarmType(typeList);

            if (list != null) {
                ifaceAlarmRuleMap.clear();
                for (IfaceAlarm ifaceAlarm : list) {
                    if (ifaceAlarmRuleMap.get(ifaceAlarm.getInterfaceName()) == null) {
                        ifaceAlarmRuleMap.put(ifaceAlarm.getInterfaceName(), new ArrayList<IfaceAlarm>());
                    }
                    ifaceAlarmRuleMap.get(ifaceAlarm.getInterfaceName()).add(ifaceAlarm);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 验证接口和ip是否符合上下线报警规则
     * @param interfaceName
     * @param ip
     * @return
     */
    public boolean needAlarm(String interfaceName, String ip) {
        List<IfaceAlarm> list = ifaceAlarmRuleMap.get(interfaceName);
        if (list != null) {
            for (IfaceAlarm alarm : list) {
                if (StringUtils.isNotEmpty(alarm.getAlarmIp()) && isIpMatch(GenerateRegular.getIpRegularList(alarm.getAlarmIp()), ip)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据接口和ip，获取报警规则的erp帐号
     * @param interfaceName
     * @param ip
     * @return
     */
    public String getAlarmRuleErps(String interfaceName, String ip) {
        List<IfaceAlarm> list = ifaceAlarmRuleMap.get(interfaceName);
        if (list != null) {
            StringBuilder result = new StringBuilder();
            for (IfaceAlarm alarm : list) {
                if (StringUtils.isNotEmpty(alarm.getAlarmIp()) && StringUtils.isNotEmpty(ip) && isIpMatch(GenerateRegular.getIpRegularList(alarm.getAlarmIp()), ip)) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    result.append(alarm.getAlarmUser());
                }
            }
            return result.toString();
        }
        return null;
    }
    
    private boolean isIpMatch(List<String> whiteList, String ip){
        for(String w : whiteList){
            if(NetUtils.isMatchIPByPattern(w, ip)){
                return true;
            }
        }
        return false;
    }

    /**
     * telnet 2次
     * @param interfaceName
     * @param ip
     * @param port
     * @param alias
     * @param protocol
     * @return
     */
    public byte doCheck(String interfaceName, String ip, int port, String alias, int protocol) {
        byte result = TELNET_NOTCONNECT;
        for (int i = 0; i < 2; i++) {
            result = isMatch(interfaceName, ip, port, alias, protocol);
            if (result == TELNET_OK || result == TELNET_NOTEXIST) break;
        }
        if (result == TELNET_NOTCONNECT) {
            logger.info(ip + ":" + port + " 连续2次telnet不通!");
        } else if (result == TELNET_NOTEXIST) {
            logger.info("ip:{}, port:{}, interfaceName:{}, alias:{}, protocol:{}, 1次telnet，provider不存在!", ip, port, interfaceName, alias, protocol);
        }
        return result;
    }

    private byte isMatch(String interfaceName, String ip, int port, String alias, int protocol) {
        byte result = TELNET_NOTCONNECT;
        try {
            Set<String> returnSet = getProviderKeys(ip, port, COMMAND_TYPE_LS);
            if (returnSet != null && !returnSet.isEmpty()) {
                String key = getKey(interfaceName, alias, protocol, port);
                if (returnSet.contains(key)) {
                    result = TELNET_OK;
                } else {
                    result = TELNET_NOTEXIST;
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(),e);
            logger.warn("interfaceName:{}, ip:{}, port:{}, alias:{}, protocol:{}, error:{}", interfaceName, ip, port, alias, protocol, e.getMessage());
            //如果有异常，则判断是否telnet正常
            if (instanceServerMap.get(ip + ":" + port).contains(TELNET)) {
                result = TELNET_OK;
            }
        }
        return result;
    }

    private Set<String> getProviderKeys(String ip, int port, int type) throws Throwable {
        String ipportkey = ip + ":" + port;
        Set<String> returnSet = null;
        String COMMAND_TYPE = PREFIX_TYPE + String.valueOf(type);
        if (instanceServerMap.containsKey(ipportkey) && instanceServerMap.get(ipportkey).contains(COMMAND_TYPE)) {
            returnSet = new HashSet<String>();
            returnSet.addAll(instanceServerMap.get(ipportkey));
            logger.info("existed , ip:{}, port:{} ", ip, port);
            return returnSet;
        }

        SafTelnetClient client = null;
        String serverDetailString = null;
        try {
            client = new SafTelnetClient(ip, port, connTimeout, readTimeout);
            returnSet = new HashSet<String>();
            instanceServerMap.put(ipportkey, returnSet);

            switch (type) {
                case COMMAND_TYPE_LS:
                    serverDetailString = client.send("ls -l", 1);
                    if(serverDetailString.trim().length()==0) throw new RuntimeException("解析错误");
                        parseLSResult(serverDetailString, returnSet);
                    break;

                case COMMAND_TYPE_CONF:
                    serverDetailString = client.send("conf -r", 1);
                    if(serverDetailString.trim().length()==0) throw new RuntimeException("解析错误");
                    parseConfFResult(serverDetailString, returnSet);
                    break;
                default:
                    throw new RuntimeException("no such command type...");

            }
            returnSet.add(TELNET);
            returnSet.add(COMMAND_TYPE);

        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Throwable e) {
                }
            }
        }

        return returnSet;
    }


    private Set<String> parseLSResult(String serverDetailString, Set<String> returnSet) {

        String[] interfacelist = serverDetailString.replace("jsf>", "").split("\n");
        for (String interfaceLine : interfacelist) {
            String[] itfz = interfaceLine.split("->");
            if(itfz.length !=2 ) continue;
            String interfaceName = itfz[0].trim();
            URL url = URL.valueOf(itfz[1].trim());
            returnSet.add(getKey(interfaceName, url.getParameter("alias"), ProtocolType.valueOf(url.getProtocol()).value(), url.getPort()));

        }

        return returnSet;
    }

    private Set<String> parseConfFResult(String serverDetailString, Set<String> returnSet) {
        //获取jsf的版本
        serverDetailString = serverDetailString.replace("dubbo>", "").replace("jsf>", "").replace("\n", "");
        logger.info(" client send to conf -r, result:{}", serverDetailString);
        Map<String, String> map = JSON.parseObject(serverDetailString, new TypeReference<Map<String, String>>() {
        });
        returnSet.add(PREFIX_JSF + map.get("jsfVersion"));
        returnSet.add(PREFIX_REG + map.get("connectedRegistry"));
        returnSet.add(PREFIX_INS + map.get("instanceKey"));
        return returnSet;
    }

    /**
     * 获取jsf版本号
     * @param ip
     * @param port
     * @return
     */
    public String getJsfVersion(String ip, int port) {
        try {
            Set<String> result = getProviderKeys(ip, port, COMMAND_TYPE_CONF);
            if (result != null && !result.isEmpty()) {
                for (String key : result) {
                    if (key != null && key.startsWith(PREFIX_JSF)) {
                        return key;
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return PREFIX_JSF;
    }

    /**
     * 检查当前provider的注册中心是否与当前环境的注册中心地址一致（检查线上provider，先注册到线上的注册中心，再注册到测试的注册中心）
     * 需要注意：如果新增的注册中心，必须立刻在管理端的注册中心管理中录入。否则，在检查的时候，会检查不到新增的注册中心地址!!!!!!!!
     * @param ip
     * @param port
     * @return
     */
    public boolean checkRegistry(String ip, int port) {
        try {
            String connectedRegistry = null;
            Set<String> result = getProviderKeys(ip, port, COMMAND_TYPE_CONF);
            if (result != null && !result.isEmpty()) {
                for (String key : result) {
                    if (key != null && key.startsWith(PREFIX_REG)) {
                        connectedRegistry = key.substring(PREFIX_REG.length());
                        break;
                    }
                }
                if (connectedRegistry != null && !registryList.isEmpty() && registryList.contains(connectedRegistry)) {
                    return true;
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            //抛异常就返回true，防止误删
            return true;
        }
        return false;
    }

    /**
     * 检查telnet provider结果中的实例key与当前数据库中的provider的实例key比较
     * 用来防止正在检查已经死亡的provider时，provider重启后，pid发生变化, 并产生误报警
     * @param ip
     * @param port
     * @param insKey
     * @return
     */
    public boolean checkInstanceKey(String ip, int port, String insKey) {
        try {
            Set<String> result = getProviderKeys(ip, port, COMMAND_TYPE_CONF);
            if (result != null && !result.isEmpty()) {
                for (String key : result) {
                    if (key != null && key.startsWith(PREFIX_INS) && key.contains(insKey)) {
                        return true;
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
		}
        return false;
    }

    /**
     * 获取当前实例连接的注册中心地址
     * @param ip
     * @param port
     * @return
     */
    public String getRegistryIpPort(String ip, int port) {
        String connectedRegistry = null;
        try {
            Set<String> result = getProviderKeys(ip, port, COMMAND_TYPE_CONF);
            if (result != null && !result.isEmpty()) {
                for (String key : result) {
                    if (key != null && key.startsWith(PREFIX_REG)) {
                        connectedRegistry = key.substring(PREFIX_REG.length());
                        break;
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return connectedRegistry == null ? "" : connectedRegistry;
    }

    /**
     * 检查当前provider的注册中心是否与当前环境的注册中心地址一致（检查线上provider，先注册到线上的注册中心，再注册到测试的注册中心）
     * 需要注意：如果新增的注册中心，必须立刻在管理端的注册中心管理中录入。否则，在检查的时候，会检查不到新增的注册中心地址!!!!!!!!
     * @param ip
     * @param port
     * @return
     */
    public String getConnectedRegistry(String ip, int port) {
        try {
            Set<String> result = getProviderKeys(ip, port, COMMAND_TYPE_CONF);
            if (result != null && !result.isEmpty()) {
                for (String key : result) {
                    if (key != null && key.startsWith(PREFIX_REG)) {
                        return key.substring(PREFIX_REG.length());
                    }
                }
            }
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
        }
        return PREFIX_REG;
    }

    /**
     * 检查是否匹配
     * @param interfaceName
     * @param ip
     * @param port
     * @param alias
     * @param protocol
     * @return
     */


    /**
     * 获取key
     * @param interfaceName
     * @param alias
     * @param protocol
     * @return
     */
    private String getKey(String interfaceName, String alias, int protocol, int port) {
        //加上端口，是为了如下情况：
        //一个jvm有两个端口，第一次启动时，接口1使用A端口和接口2使用了B端口，重启后，接口1使用了B或C端口，接口2使用了A端口，telnet从端口A检查接口1的信息
        //由于telnet的ls -l会返回jvm中的所有接口信息，因此依然认为接口1的A端口存在。
        return String.format("%s?alias=%s&protocol=%s&port=%s", interfaceName, StringUtils.defaultString(alias), StringUtils.defaultString(String.valueOf(protocol)), StringUtils.defaultString(String.valueOf(port)));
    }



    /**
     * 保存报警信息,多线程处理
     * @param alarmList
     */
    public void saveAlarm(final List<JsfAlarmHistory> alarmList) {
        alarmPool.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    List<com.ipd.jsf.alarm.domain.JsfAlarmHistory> remoteList = new ArrayList<com.ipd.jsf.alarm.domain.JsfAlarmHistory>();
                    for (JsfAlarmHistory history : alarmList) {
                        remoteList.add(AlarmUtil.convertToRemoteHistory(history));
                        if (remoteList.size() > alarmLimit) {
//                            alarmManager.batchInsert(list);
                            jsfAlarmService.batchInsert(remoteList);
                            remoteList.clear();
                        }
                    }
                    if (!remoteList.isEmpty()) {
//                    	alarmManager.batchInsert(list);
                        jsfAlarmService.batchInsert(remoteList);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        });
    }

    public List<String> getUncheckRegistryList() {
        return this.unCheckRegistryList;
    }

    /**
     * @param insList
     * @return
     */
    public Map<String, JsfIns> convertInsListToMap(List<JsfIns> insList) {
        final Map<String, JsfIns> insMap = new HashMap<String, JsfIns>();
        if (insList != null && !insList.isEmpty()) {
            for (JsfIns ins : insList) {
                insMap.put(ins.getInsKey(), ins);
            }
        }
        return insMap;
    }

    /**
     * 删除实例
     * @param insList
     * @param removeList
     */
    public void removeJsfIns(List<JsfIns> insList, List<String> removeList) {
        List<JsfIns> removeInsList = new ArrayList<JsfIns>();
        for (JsfIns ins : insList) {
            if (removeList.contains(ins.getInsKey())) {
                removeInsList.add(ins);
            }
        }
        insList.removeAll(removeInsList);
    }

    public JsfIns getInsFromServer(Server server) {
        if (server != null) {
            JsfIns ins = new JsfIns();
            ins.setInsKey(server.getInsKey());
            ins.setIp(server.getIp());
            ins.setPid(server.getPid());
            ins.setStartTime(server.getStartTime());
            return ins;
        }
        return null;
    }

    public JsfIns getInsFromClient(Client client) {
        if (client != null) {
            JsfIns ins = new JsfIns();
            ins.setInsKey(client.getInsKey());
            ins.setIp(client.getIp());
            ins.setPid(client.getPid());
            ins.setStartTime(client.getStartTime());
            return ins;
        }
        return null;
    }

    /**
     * @return the registryList
     */
    public List<String> getRegistryList() {
        return registryList;
    }

    /**
     * @param registryList the registryList to set
     */
    public void setRegistryList(List<String> registryList) {
        this.registryList = registryList;
    }

}