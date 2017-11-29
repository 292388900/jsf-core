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

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.worker.common.PropertyFactory;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.dao.JsfCheckHistoryDao;
import com.ipd.jsf.worker.domain.*;
import com.ipd.jsf.worker.log.dao.JsfRegAddrDao;
import com.ipd.jsf.worker.manager.*;
import com.ipd.jsf.worker.service.ScanNodeStatusService;
import com.ipd.jsf.worker.util.*;
import com.ipd.jsf.worker.vo.ByRoomResult;
import com.ipd.jsf.worker.vo.InstanceVo;
import com.ipd.jsf.common.enumtype.AlarmType;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ScanNodeStatusServiceImpl implements ScanNodeStatusService {

    private Logger logger = LoggerFactory.getLogger(ScanNodeStatusServiceImpl.class);

    //线程池, 用于查找数据库并telnet
    private ExecutorService telnetServicePool = null;

    //线程池, 用于查找数据库并telnet
    private ExecutorService servicePool = null;

    //保存es
    private boolean saveEsSwitch = false;

    //ump服务上下线报警key
    private String umpAlarmKey = null;
    //ump报警key
    private String umpAlarmProviderNoInsKey = null;

    //物理删除反注册实例的时间间隔-8小时
    private final long delUnregInsInterval = 8 * 3600000L;

    //物理删除实例的时间间隔-1周
    private final long delInsInterval = 3 * 24 * 3600000L;

    //删除实例的模, 两小时执行一次物理删除
    private final int delInsMod = 60;

    //删除实例的计数
    private int delInsCount = 0;

    //无实例provider的报警时间间隔(23小时30分钟)
    private long noinsAlarmInterval = (24 * 60 * 60 - 30 * 60) * 1000L;

    //报警内容长度上限
    private final int contentLimit = 1023;

    private volatile List<Integer> currTaskRooms;
    private volatile boolean isByRoom = false;
    private volatile boolean matchWorkerType = false;

    private ScanNodeStatusHelper scanNodeStatusHelper;

    @Autowired
    private ScanInsManager scanInsManager;

    @Autowired
    private ScanServerManager scanServerManagerImpl;

    @Autowired
    private ScanClientManager scanClientManager;

    @Autowired
    private InterfaceInfoManager interfaceInfoManager;

    @Autowired
    private ServiceTraceLogManager serviceTraceLogManager;

    @Autowired
    private AlarmManager alarmManager;

    @Autowired
    private IfaceAlarmManager ifaceAlarmManager;

    @Autowired
    private SysParamManager sysParamManager;

    @Autowired
    private JsfRegAddrDao jsfRegAddrDao;

    @Autowired
    private RegHbManager regHbManager;

    @Autowired
    private InstanceManager instanceManager;

    @Autowired
    private JsfCheckHistoryDao jsfCheckHistoryDao;

    @PostConstruct
    public void init() {
        try {
            saveEsSwitch = Boolean.parseBoolean((String) PropertyFactory.getProperty("scan.es.saveswitch"));
            int thread = PropertyFactory.getProperty(WorkerAppConstant.TELNET_THREAD_CNT, 10);
            telnetServicePool = Executors.newFixedThreadPool(thread, new WorkerThreadFactory("scanNodeStatusService_telnetServicePool"));
            servicePool = Executors.newFixedThreadPool(10, new WorkerThreadFactory("scanNodeStatusService_servicePool"));
            scanNodeStatusHelper = new ScanNodeStatusHelper();
            scanNodeStatusHelper.init(serviceTraceLogManager, ifaceAlarmManager, sysParamManager,
                    interfaceInfoManager, jsfRegAddrDao, regHbManager, jsfCheckHistoryDao);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void scanStatus() throws Exception {
        try {
            //只用来单机处理
            singleRun();
        } finally {
            scanNodeStatusHelper.clearData();
        }
        logger.info("iface->erp map size:{}, log queue size:{}", scanNodeStatusHelper.ifaceErpMap.size(), scanNodeStatusHelper.getQueueSize());
    }

    @Override
    public ByRoomResult isByRoom(String currWorkerType) throws Exception {
        ByRoomResult result = new ByRoomResult(false, ScanNodeStatusHelper.defaultWorkerAlias, false);
        String byRoom = sysParamManager.findValueBykey("by.room");
        if (byRoom == null || byRoom.isEmpty()) {
            return result;
        }
        String[] config = byRoom.split(":");
        if (config == null || config.length == 0) {
            matchWorkerType = currWorkerType.equals(result.getWorkerAlias());
            result.setMatchWorkerType(matchWorkerType);
            return result;
        }
        if (config.length > 0) {
            byRoom = config[0];
        }
        if (Boolean.TRUE.toString().equals(byRoom)) {
            result.setByRoom(true);
        }
        if (config.length >= 2) {
            result.setWorkerAlias(config[1]);
        }
        isByRoom = result.isByRoom();
        matchWorkerType = currWorkerType.equals(result.getWorkerAlias());
        result.setMatchWorkerType(matchWorkerType);
        return result;
    }

    @Override
    public void setCurrTaskRooms(List<Integer> rooms) throws Exception {
        this.currTaskRooms = rooms;
    }

    private List<Integer> getCurrTaskRooms(String msg) {
        logger.info(msg, currTaskRooms != null ? Joiner.on(",").join(currTaskRooms) : "nil");
        return currTaskRooms;
    }

    /**
     * 只在一台机器上运行
     */
    private void singleRun() {
        //加载配置
        scanNodeStatusHelper.loadProperty();

        //获取有病的注册中心列表出现问题
        if (!scanNodeStatusHelper.unCheckRegistryFlag) { //没有获取到有病的注册中心
            logger.error("未进行任何操作, 获取未检测的注册中心列表失败！");
            return;
        }

        scanNodeStatusHelper.loadInterface();
        scanNodeStatusHelper.loadIfaceAlarmRule();

        //处理该标记为死亡的实例
        handleDeadInstance();
        //恢复处理，对心跳正常，但provider和consumer已经删除的节点进行恢复操作
        revivalInstance();

        //逻辑删除之前是否告警处理
        //当前时间向前推N小时 < 8
        checkDeadInsToDel();

        //处理该标记为删除的实例
        tagDeadInsToDel();

    	if (delInsCount++ % delInsMod == 0) {
    		delInsCount = 0;
    		//物理删除反注册实例
            realDeleteUnregInstance();
            //物理删除实例
            realDeleteInstance();
            //无实例处理
            handleNoIns();
    	}
    }

    /**
     * 每个实例心跳时，都会记录该实例连的注册中心地址
     * 对于单机，返回的list是 不需要 扫描的注册中心地址类表
     * @return
     */
    private List<String> getUnCheckRegistryList() {
        List<String> list = new ArrayList<String>();
        if (!scanNodeStatusHelper.getUncheckRegistryList().isEmpty()) {
            list.addAll(scanNodeStatusHelper.getUncheckRegistryList());
        }
        if (list.isEmpty()) {
            //返回null，便于sqlmap语句的判断
            return null;
        }
        return list;
    }

    /**
     * 扫描心跳死亡的实例、server、client
     */
    private void handleDeadInstance() {
    	if (!scanNodeStatusHelper.scanDeadNodeSwitch) {
    		logger.error("handleDeadInstance is closed.");
    		return;
    	}
        long start = System.currentTimeMillis();
        logger.info("handleDeadInstance  starting...");
        Date scanDeadDate = null;
        try {
            long scanDeadTime = WorkerUtil.getDeadTime();
            scanDeadDate = new Date(scanDeadTime);
            //获取在线且没有心跳的实例
            List<JsfIns> insList;
            if (isByRoom) {
                insList = scanInsManager.getOnlineInsBeforeTimeByRooms(scanDeadDate, null, getCurrTaskRooms("handle dead instance rooms list:{}"));
            } else {
                insList = scanInsManager.getOnlineInsBeforeTime(scanDeadDate, null);
            }
            if (insList != null && insList.size() > 0) {
                logger.info("handle dead instance list:{}", insList.toString());
                final CountDownLatch latch = new CountDownLatch(insList.size());
                for (final JsfIns ins : insList) {
                	servicePool.execute(new Runnable() {
						@Override
						public void run() {
							try {
		                		handleDeadClient(ins.getInsKey());
		                		handleDeadServer(ins.getInsKey());
		                		//将在线且没有心跳的实例状态改为死亡状态
		                		scanInsManager.updateStatusOffline(ins.getInsKey());
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
							try {
								latch.countDown();
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					});
                }
                latch.await(180, TimeUnit.SECONDS);
            }
            logger.info("handleDeadInstance  end, elapse:{}ms", (System.currentTimeMillis() - start));
        } catch (Exception e) {
            logger.error("handleDeadInstance error, scanDeadDate:" + scanDeadDate, e);
        }
    }

    /**
     * 将状态为上线的server改为死亡
     * @param insKey
     */
    private void handleDeadServer(String insKey) throws Exception {
        List<Server> serverList = scanServerManagerImpl.getOnlineServersByIns(insKey);
        if (!CollectionUtils.isEmpty(serverList)) {
        	final List<Server> deleteServerList = Collections.synchronizedList(new ArrayList<Server>());
        	//将server的状态改为死亡
    		scanServerManagerImpl.updateStatusOffline(serverList);
        	try {
        		//记录上下线日志
        		scanNodeStatusHelper.recordServerTraceLog(serverList, ServiceTraceLog.offline);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
            logger.info("handle dead server list:{}", serverList.toString());
            addAlarmOnOffline(serverList, false);
            
            //下面对接口+alias不存在的情况进行检查，并将状态置为强制下线
            List<Future<Boolean>> taskFutureList = new ArrayList<Future<Boolean>>();
            for (final Server server : serverList) {
                Future<Boolean> future = telnetServicePool.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            byte result = scanNodeStatusHelper.doCheck(server.getInterfaceName(), server.getIp(), server.getPort(), server.getAlias(), server.getProtocol());
                            if (result == scanNodeStatusHelper.TELNET_NOTEXIST) {
                            	deleteServerList.add(server);
                            	server.setNote("not exist");
                            }
                        } catch (Exception e) {
                            logger.error("handleDeadServer error, server uniqkey:" + server.getUniqKey() + ", error:" + e.getMessage(), e);
                        }
                        return Boolean.TRUE;
                    }
                });
                taskFutureList.add(future);
            }
            for (Future<Boolean> task : taskFutureList) {
                try {
                    task.get();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            
            scanServerManagerImpl.tagServerToDel(deleteServerList);
            logger.info("handle dead server, server is not existed:{}", getServerUniqKeyListString(deleteServerList));
        }
    }

    /**
     * 将状态为上线的client改为死亡
     * @throws Exception
     */
    private void handleDeadClient(String insKey) throws Exception {
        //获取状态为上线但已经死亡的client
        List<Client> list = scanClientManager.getOnlineClientsByIns(insKey);
        //更改client为死亡状态
        scanClientManager.updateStatusOffline(list);
        if (list != null && !list.isEmpty()) {
        	try {
        		//记录上下线日志
        		scanNodeStatusHelper.recordClientTraceLog(list, ServiceTraceLog.offline);
        		logger.info("handle dead client list:{}", list.toString());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
        }
    }

    /**
     * 找到实例心跳正常，但是provider和consumer已经被逻辑删除的实例,并进行恢复
     * @throws Exception
     */
    private void revivalInstance() {
    	if (!scanNodeStatusHelper.revivalNodeSwitch) {
    		logger.warn("revivalInstance  is closed.");
    		return;
    	}
    	long start = System.currentTimeMillis();
    	int insSSize = 0;
    	int insCSize = 0;
    	int sResult = 0;
    	int cResult = 0;
        try {
        	logger.info("revivalInstance  starting...");
        	//找到server相关实例
			List<JsfIns> insList;
            if (isByRoom) {
                insList = scanInsManager.getRevivalInsListByServerAndRooms(getCurrTaskRooms("handle revival server instance rooms list:{}"));
            } else {
                insList = scanInsManager.getRevivalInsListByServer();
            }
			insSSize = insList.size();
			//recover server状态
			sResult = scanServerManagerImpl.updateServerToRevival(insList);

			//找到client相关实例
            if (isByRoom) {
                insList = scanInsManager.getRevivalInsListByClientAndRooms(getCurrTaskRooms("handle revival client instance rooms list:{}"));
            } else {
                insList = scanInsManager.getRevivalInsListByClient();
            }

			insCSize = insList.size();
			//recover client状态
			cResult = scanClientManager.updateClientToRevival(insList);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        logger.info("revivalInstance end elapse:{}, server insList size:{}, server size:{}, client insList size:{}, client size:{}",
        		(System.currentTimeMillis() - start), insSSize, sResult, insCSize, cResult);
    }

    /**
     * 逻辑删除死亡实例和provider,consumer. 注：只要实例在，就可以通过实例的心跳检查provider和consumer，所以在删除实例前，要检查实例心跳
     * 1. 逻辑删除provider和consumer
     * 2. 逻辑删除实例
     * @throws Exception
     */
    private void tagDeadInsToDel() {
        long start = System.currentTimeMillis();
        logger.info("tagDeadInsToDel  starting...");
        List<JsfIns> deleteInsList = null;
        try {
            Date deleteTime = new Date(WorkerUtil.getInstanceDeleteTime());
            //1.找到心跳时间已经超过删除时间（8小时）的实例，然后删除掉对应的provider和consumer
            if (isByRoom) {
                deleteInsList = scanInsManager.getOfflineInsBeforeTimeByRooms(deleteTime,  getUnCheckRegistryList(), getCurrTaskRooms("handle tagDeadInsToDel rooms list:{}"));
            } else {
                deleteInsList = scanInsManager.getOfflineInsBeforeTime(deleteTime,  getUnCheckRegistryList());
            }
            logger.info("tag instance to delete time is {}, inslist size: {}", deleteTime, deleteInsList.size());
            //删除server和client
            if (deleteInsList != null && !deleteInsList.isEmpty()) {
            	ListPaging<JsfIns> listPaging = new ListPaging<JsfIns>(deleteInsList, 50);
            	final CountDownLatch latch = new CountDownLatch(listPaging.getTotalPage());
            	while (true) {
            		final List<JsfIns> list = listPaging.nextPageList();
            		if (list.isEmpty()) {
            			break;
            		}
            		servicePool.execute(new Runnable() {
						@Override
						public void run() {
							try {
								Map<String, InsVo> map = getInsMap(list);
								for (Map.Entry<String, InsVo> entry : map.entrySet()) {
									try {
				            			tagClientToDel(entry.getValue());
				            			if (tagServerToDel(entry.getValue())) {
				            				//2.删除心跳时间已经超过删除时间的实例
				            				scanInsManager.tagInsToDel(entry.getValue().ins);
				            			} else {
				            				logger.info("can not tag ins to del, insKey:{}, wait next time.", entry.getKey());
				            			}
									} catch (Exception e) {
										logger.error(e.getMessage() + entry.getValue().ins.toString(), e);
									}
								}
							} catch (Exception e) {
								logger.error(e.getMessage() + list.toString(), e);
							}
							try {
								latch.countDown();
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
							}
						}
					});
            	}
            	latch.await(180, TimeUnit.SECONDS);
            }
        } catch (Exception e) {
            logger.error("tagDeadInsToDel error: " + e.getMessage(), e);
        }
        long end = System.currentTimeMillis();
        logger.info("tagDeadInsToDel  end, elapse:{}ms, inslist size: {}", (end - start), deleteInsList == null ? 0 : deleteInsList.size());
    }

    private Map<String, InsVo> getInsMap(List<JsfIns> list) throws Exception {
    	Map<String, InsVo> map = new HashMap<String, InsVo>();
    	for (JsfIns ins : list) {
    		map.put(ins.getInsKey(), new InsVo(ins));
    	}
    	List<Server> serverList = scanServerManagerImpl.getServersByInsKeyList(new ArrayList<String>(map.keySet()));
    	List<Client> clientList = scanClientManager.getClientsByInsKeyList(new ArrayList<String>(map.keySet()));
    	for (Server server : serverList) {
			map.get(server.getInsKey()).serverList.add(server);
    	}
    	for (Client client : clientList) {
			map.get(client.getInsKey()).clientList.add(client);
    	}
    	return map;
    }

    /**
     * 逻辑删除死亡的server
     * @param ins
     * @return  true 可以删除实例，false 不能删除实例
     */
    private boolean tagServerToDel(final InsVo ins) {
    	//记录状态更新失败provider对应的实例key
    	final List<String> noDelInsKeyList = Collections.synchronizedList(new ArrayList<String>());
        try {

        	List<Server> serverList = ins.serverList;
            if (serverList != null && !serverList.isEmpty()) {
                final List<Server> aliveServerList = Collections.synchronizedList(new ArrayList<Server>());
                final List<Server> deleteServerList = Collections.synchronizedList(new ArrayList<Server>());
                List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
                logger.info("tagServerToDel inskey:{}, server list : {}", ins.ins.getInsKey(), serverList.toString());
                for (final Server server : serverList) {

                	if (server.getStatus() == InstanceStatus.unreg.value().intValue()) {
                		continue;
                	}
                    Future<Boolean> future = telnetServicePool.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                        	try {
                        		 byte result = scanNodeStatusHelper.TELNET_NOTCONNECT;
                                 logger.info("tagServerToDel docheck server:{}", server.toString());
                                 result = scanNodeStatusHelper.doCheck(server.getInterfaceName(), server.getIp(), server.getPort(), server.getAlias(), server.getProtocol());
                                 if (result == scanNodeStatusHelper.TELNET_NOTEXIST) {  //若不存在, 就逻辑删掉
                                     deleteServerList.add(server);
                                     server.setNote("not exist");
                                     logger.info("TELNET_NOTEXIST, tag server to delete :{}", server.toString());
                                 } else if (result == scanNodeStatusHelper.TELNET_NOTCONNECT) { //不能telnet通
                                 	 deleteServerList.add(server);
                                     if (server.isRandom()) {   //如果是随机端口，就删掉
                                         logger.info("TELNET_NOTCONNECT, tag random server to delete  :{}", server.toString());
                                     } else {  //如果是固定端口，且到了删除时间，就删掉
                                         logger.info("TELNET_NOTCONNECT, tag server to delete :{}", server.toString());
                                     }
                                 } else if (result == scanNodeStatusHelper.TELNET_OK) {
                                     //检查server的注册中心地址是否合法 
                                     if (scanNodeStatusHelper.checkRegistry(server.getIp(), server.getPort()) == false) {
                                         //将不在当前环境注册中心地址列表的server删除
                                         deleteServerList.add(server);
                                         logger.info("TELNET_OK, but registry({}) of server is not correct. {}", scanNodeStatusHelper.getConnectedRegistry(server.getIp(), server.getPort()), server.toString());
                                     } else {
                                         //检查server的实例key与telnet结果中的实例key是否一致
                                         if (scanNodeStatusHelper.checkInstanceKey(server.getIp(), server.getPort(), server.getInsKey()) == true) {
                                             aliveServerList.add(server);
                                             noDelInsKeyList.add(server.getInsKey());
                                             logger.info("TELNET_OK, add to alive server :{}", server.toString());
                                         } else {
                                         	//如果telnet进去，实例key发生变化了，说明节点重启了。
                                         	//服务重启，停止和启动时间间隔正好与检查的周期相同，并且重新注册的provider信息还未更新到db中
                                             logger.info("TELNET_OK, server may restart :{}", server.toString());
                                         }
                                     }
                                 }
                                 return Boolean.TRUE;
							} catch (Throwable e) {
								logger.error(e.getMessage(), e);
								noDelInsKeyList.add(server.getInsKey());
							}
                        	return Boolean.TRUE;
                        }
                    });
                    futureList.add(future);
                }
                for (Future<Boolean> task : futureList) {
                    try {
                        task.get();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (!aliveServerList.isEmpty()) logger.info("========alive serverlist:{}", aliveServerList.toString());
                if (!deleteServerList.isEmpty()) logger.info("========delete serverlist:{}", deleteServerList.toString());
                //1.将端口存活，但是没有心跳的节点报警
                if (!aliveServerList.isEmpty()) {
                    //将存活provider进行报警
                    addAlarmNoInsProvider(aliveServerList, "实例无心跳，但provider存活");
                }
                //2.逻辑删除provider
                if (!deleteServerList.isEmpty()) {
                    scanServerManagerImpl.tagServerToDel(deleteServerList);
                }
            }
        } catch (Exception e) {
            logger.error("delete server error(Throwable). params:" + ins.ins.getInsKey(), e);
        }
        if (!noDelInsKeyList.isEmpty()) {
        	return false;
        }
        return true;
    }

    /**
     * 逻辑删除死亡的client
     * @param ins
     * @throws Exception 
     */
    private boolean tagClientToDel(InsVo ins) throws Exception {

    	List<Client> clientList = ins.clientList;
    	List<Client> deletaClientList = new ArrayList<Client>();
    	if (!CollectionUtils.isEmpty(clientList)) {
    		for (Client client : clientList) {
    			//如果有反注册，就不逻辑删除了
    			if (client.getStatus() == InstanceStatus.unreg.value().intValue()) {
    				continue;
    			}
    			deletaClientList.add(client);
    		}
    	}
        scanClientManager.tagClientToDel(deletaClientList);
        logger.info("insKey: {}, tagClientToDel: {}", ins.ins.getInsKey(), getClientUniqKeyListString(deletaClientList));
        return true;
    }

    private String getClientUniqKeyListString(List<Client> clientList) {
    	StringBuilder sb = new StringBuilder();
    	for (Client client : clientList) {
    		sb.append(",uniqKey:").append(client.getUniqKey());
    	}
        return sb.toString();
    }
    
    private String getServerUniqKeyListString(List<Server> serverList) {
    	StringBuilder sb = new StringBuilder();
    	for (Server server : serverList) {
    		sb.append(",uniqKey:").append(server.getUniqKey());
    	}
    	return sb.toString();
    }

    /**
     * 物理删除反注册实例和provider
     * 1. 删除provider
     * 2. 删除实例
     * @throws Exception
     */
    private void realDeleteUnregInstance() {
        long start = System.currentTimeMillis();
        //实例8小时没有心跳的，物理删除反注册的实例/provider/consumer
        Date time = new Date(System.currentTimeMillis() - delUnregInsInterval);
        logger.info("real delete unreg instance  starting... del unreg time : {}", time);
        int result = realDelIns(time, 1);
        logger.info("real delete unreg instance  size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
    }

    /**
     * 物理删除实例和provider
     * 1. 删除provider
     * 2. 删除实例
     * @throws Exception
     */
    private void realDeleteInstance() {
        long start = System.currentTimeMillis();
        //实例8小时没有心跳的，物理删除反注册的实例/provider/consumer
        Date time = new Date(System.currentTimeMillis() - delInsInterval);
        logger.info("real delete instance  starting... delete time:{}", time);
        int result = realDelIns(time, 2);
        logger.info("real delete instance size:{}, elapse:{}ms ", result, (System.currentTimeMillis() - start));
    }

	/**
	 * @param time
	 * @param type
	 */
	private int realDelIns(Date time, int type) {
		List<JsfIns> deleteInsList = null;
        int result = 0;
        try {
            while (true) {
                //批量删除，每次先取100条，然后删除，并记录日志
                if (isByRoom) {
                    deleteInsList = scanInsManager.getDelInsBeforeTimeByRooms(time, type, getCurrTaskRooms("handle real dead instance rooms list:{}"));
                } else {
                    deleteInsList = scanInsManager.getDelInsBeforeTime(time, type);
                }

                if (deleteInsList != null && !deleteInsList.isEmpty()) {
                	ListPaging<JsfIns> listPaging = new ListPaging<JsfIns>(deleteInsList, 50);
                	final CountDownLatch latch = new CountDownLatch(listPaging.getTotalPage());
                	while (true) {
                		final List<JsfIns> list = listPaging.nextPageList();
                		if (list.isEmpty()) {
                			break;
                		}
                		servicePool.execute(new Runnable() {
							@Override
							public void run() {
								try {
									Map<String, InsVo> map = getInsMap(list);
									for (Map.Entry<String, InsVo> entry : map.entrySet()) {
			                			List<Server> serverList = entry.getValue().serverList;
			                			List<Client> clientList = entry.getValue().clientList;

                                        if (saveEs(entry, serverList, clientList)) {
                                            //删除consumer
                                            scanClientManager.deleteById(clientList);
                                            //删除provider
                                            scanServerManagerImpl.deleteServerById(serverList);
                                            //批量删除实例
                                            scanInsManager.deleteByInsKey(entry.getValue().ins);
                                        }
									}
								} catch (Exception e) {
									logger.error(e.getMessage() + list.toString(), e);
								}
								try {
									latch.countDown();
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
							}

                            private boolean saveEs(Map.Entry<String, InsVo> entry, List<Server> serverList, List<Client> clientList) {
                                if (!saveEsSwitch) {
                                    return true;
                                }
                                int appId = 0;
                                String appName = "";
                                try {
                                    JsfApp app = instanceManager.getAppByInsKey(entry.getKey());
                                    if (app != null) {
                                        appId = app.getAppId();
                                        appName = app.getAppName();
                                    }
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                return Boolean.TRUE;
                            }
                        });
                	}
                	latch.await(180, TimeUnit.SECONDS);
                	result += deleteInsList.size();
                } else {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("real delete instance error: " + e.getMessage(), e);
        }
        return result;
	}

    /**
     * 删除无实例的provider和consumer
     */
    private void handleNoIns() {
        if (!matchWorkerType) {
            return;
        }
    	long start = System.currentTimeMillis();
		Map<String, JsfIns> insMap = new HashMap<String, JsfIns>();
		Map<String, List<Server>> insServerListMap = new HashMap<String, List<Server>>();
		Map<String, List<Client>> insClientListMap = new HashMap<String, List<Client>>();
		Date time = new Date(System.currentTimeMillis() - delInsInterval);
		// 删除无实例的provider和consumer
		try {
			// 检查无实例的server，检查当前时间大于 <最后一次操作时间+删除间隔>
			List<Server> serverList = scanServerManagerImpl.getNoInsServers(time.getTime());
			List<Client> clientList = scanClientManager.getNoInsClients(time.getTime());
			//将查找出来的serverList按照实例的维度整理
			if (serverList != null && !serverList.isEmpty()) {
				for (Server server : serverList) {
					if (server.getInsKey() == null || server.getInsKey().isEmpty()) {
						//手动添加的节点没有inskey，要补下
						server.setInsKey(server.getIp() + "_" + server.getPort());
					}
					if (insMap.get(server.getInsKey()) == null) {
						insMap.put(server.getInsKey(), scanNodeStatusHelper.getInsFromServer(server));
					}
					if (insServerListMap.get(server.getInsKey()) == null) {
						insServerListMap.put(server.getInsKey(), new ArrayList<Server>());
					}
					insServerListMap.get(server.getInsKey()).add(server);
				}
			}
			//将查找出来的clientList按照实例的维度整理
			if (clientList != null && !clientList.isEmpty()) {
				for (Client client : clientList) {
					if (insMap.get(client.getInsKey()) == null) {
						insMap.put(client.getInsKey(), scanNodeStatusHelper.getInsFromClient(client));
					}
					if (insClientListMap.get(client.getInsKey()) == null) {
						insClientListMap.put(client.getInsKey(), new ArrayList<Client>());
					}
					insClientListMap.get(client.getInsKey()).add(client);
				}
			}
			//按照实例维度做删除
			for (String insKey : insMap.keySet()) {
				int appId = 0;
    			String appName = "";
    			try {
    				JsfApp app = instanceManager.getAppByInsKey(insKey);
					if (app != null) {
						appId = app.getAppId();
						appName = app.getAppName();
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
    			try {
    				List<Server> insServerList = insServerListMap.get(insKey);
    				List<Client> insClientList = insClientListMap.get(insKey);
    				// provider无实例但存活的，要报警
    				addAlarmNoInsProvider(insServerList, "无实例provider");

                    //根据实例批量删除client
                    scanClientManager.deleteById(insClientList);
                    // 批量删除server节点
                    scanServerManagerImpl.deleteServerById(insServerList);
                    logger.warn(" providers which have no ins are living --------->>>>>> {}", serverList.toString());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			logger.info("handleNoIns elapse:{}ms", (System.currentTimeMillis() - start));
		}
    }

    /**
     * provider存活，但实例不存在，放入报警队列
     * @param serverList
     * @throws Exception 
     */
    private void addAlarmNoInsProvider(List<Server> serverList, String value) {
        try {
            if (serverList == null || serverList.isEmpty()) return;
            // insMap的key=insKey
            Map<String, InstanceVo> insMap = new HashMap<String, InstanceVo>();
            //将server转为实例, 用insMap去重实例
            String insKey;
            for (Server server : serverList) {
                insKey = server.getIp() + "_" + server.getPid();
                if (insMap.get(insKey) == null) {
                    InstanceVo vo = new InstanceVo();
                    vo.setIp(server.getIp());
                    vo.setPid(server.getPid());
                    if (vo.getMap() == null) {
                        vo.setMap(new HashMap<Integer, InterfaceInfo>());
                    }
                    if (scanNodeStatusHelper.ifaceErpMap.get(server.getInterfaceId()) != null) {
                        vo.getMap().put(server.getInterfaceId(), scanNodeStatusHelper.ifaceErpMap.get(server.getInterfaceId()));
                    }
                    try {
                    	vo.setJsfVersion(scanNodeStatusHelper.getJsfVersion(server.getIp(), server.getPort()));
                    	vo.setRegIpPort(scanNodeStatusHelper.getRegistryIpPort(server.getIp(), server.getPort()));
					} catch (Throwable e) {
						logger.error(e.getMessage(), e);
					}
                    insMap.put(insKey, vo);
                }
            }

            //获取报警历史记录，并转换为set
            List<JsfAlarmHistory> alarmedList = alarmManager.getAlarmHistoryList(new Date(System.currentTimeMillis() - noinsAlarmInterval), AlarmType.NOINSPROVIDERALIVE.getValue());
            Set<String> alarmedSet = new HashSet<String>();
            for (JsfAlarmHistory history : alarmedList) {
                //将扩展字段(ip_pid)放入set中
                alarmedSet.add(history.getExtendKey2());
            }

            List<JsfAlarmHistory> alarmList = new ArrayList<JsfAlarmHistory>();
            Date createTime = new Date();
            try {
                //往报警表中插入记录
                JsfAlarmHistory alarm;
                StringBuilder content;
                String tempIfaceName = null;
                for (Map.Entry<String, InstanceVo> entry : insMap.entrySet()) {
                	tempIfaceName = null;
                    if (alarmedSet.contains(entry.getKey()) == false) {  //如果报警间隔大于一天，就报警
                        String jsfVersion = entry.getValue().getJsfVersion() == null ? "JSF" : entry.getValue().getJsfVersion();
                        alarm = new JsfAlarmHistory();
                        try {
                            alarm.setAlarmKey(umpAlarmProviderNoInsKey);
                            alarm.setAlarmType((byte)AlarmType.NOINSPROVIDERALIVE.getValue());
                            alarm.setCreateTime(createTime);
                            content = new StringBuilder("[").append(jsfVersion)
                                    .append("]").append(value).append(",实例IP:")
                                    .append(entry.getValue().getIp()).append(",pid:").append(entry.getValue().getPid());
                            for (InterfaceInfo iface : entry.getValue().getMap().values()) {
                                //获取实例下的接口信息
                                if (!StringUtils.isEmpty(iface.getInterfaceName())) {
                                    content.append(",").append(iface.getInterfaceName());
                                    if (tempIfaceName == null) {
                                    	tempIfaceName = iface.getInterfaceName();
                                    }
                                    break;
                                }
                            }
                            content.append(",regip:").append(entry.getValue().getRegIpPort());
                            alarm.setErps(scanNodeStatusHelper.workerAlarmErps);
                            alarm.setContent(WorkerUtil.limitString(content.toString(), contentLimit));
                            alarm.setInterfaceName(tempIfaceName);
                            alarm.setAlarmIp(entry.getValue().getIp());
                            alarm.setExtendKey2(entry.getKey());
                            alarm.setRegIp(entry.getValue().getRegIpPort());
                            alarmList.add(alarm);
                            logger.info(content.toString() + ", {}", tempIfaceName);
                        } catch (Exception e) {
                            logger.error(JSON.toJSONString(alarm) + ", " + value + "异常，error:" + e.getMessage(), e);
                        }
                    } else {
                        logger.info(value + "检查，24小时内已经报警，不用报警了！server:{}", JSON.toJSONString(entry.getValue()));
                    }
                }
            } finally {
                //保存到数据库中
                if (!alarmList.isEmpty()) {
                	scanNodeStatusHelper.saveAlarm(alarmList);
                }
            }
        } catch (Exception e) {
            logger.error("add alarm error:" + e.getMessage(), e);
        }
    }

    /**
     * 上下线实例，放入报警队列
     * @param serverList
     * @throws Exception 
     */
    private void addAlarmOnOffline(List<Server> serverList, boolean isOnline) {
        try {
            if (serverList == null || serverList.isEmpty()) return;
            String value = isOnline ? ", 服务端恢复上线" : ",服务端下线";
            // insMap的key=insKey
            Map<String, InstanceVo> insMap = new HashMap<String, InstanceVo>();
            //将server转为实例, 用insMap去重实例
            for (Server server : serverList) {
                //检查是否需要报警
                if (scanNodeStatusHelper.needAlarm(server.getInterfaceName(), server.getIp())) {
                    String insKey = server.getIp() + "_" + server.getPid();
                    if (insMap.get(insKey) == null) {
                        InstanceVo vo = new InstanceVo();
                        vo.setIp(server.getIp());
                        vo.setPid(server.getPid());
                        if (vo.getMap() == null) {
                            vo.setMap(new HashMap<Integer, InterfaceInfo>());
                        }
                        //以实例为主，把每个server的interface放入insMap中. 即每个实例对应多个接口
                        if (scanNodeStatusHelper.ifaceErpMap.get(server.getInterfaceId()) != null) {
                            vo.getMap().put(server.getInterfaceId(), scanNodeStatusHelper.ifaceErpMap.get(server.getInterfaceId()));
                        }
                        insMap.put(insKey, vo);
                    }
                } else {
                    logger.warn("未放入报警表中,接口:{},ip:{},pid:{} {}, server:{} ", server.getInterfaceName(), server.getIp(), server.getPid(), value, server.toString());
                }
            }

            List<JsfAlarmHistory> alarmList = new ArrayList<JsfAlarmHistory>();
            Date createTime = new Date();
            String tempIfaceName = null;
            //往报警表中插入记录
            try {
                for (Map.Entry<String, InstanceVo> entry : insMap.entrySet()) {
                	tempIfaceName = null;
                    JsfAlarmHistory alarm = new JsfAlarmHistory();
                    try {
                        alarm.setAlarmKey(umpAlarmKey);
                        alarm.setAlarmType((byte)AlarmType.SERVERONOFF.getValue());
                        alarm.setCreateTime(createTime);
                        Set<String> erpSet = new HashSet<String>();
                        StringBuilder content = new StringBuilder().append("[JSF]实例IP:").append(entry.getValue().getIp()).append(", pid:").append(entry.getValue().getPid()).append(value);
                        for (InterfaceInfo iface : entry.getValue().getMap().values()) {
                            //获取实例下的每个接口的负责人erp帐号
                            if (!StringUtils.isEmpty(iface.getOwnerUser())) {
                                String erps = scanNodeStatusHelper.getAlarmRuleErps(iface.getInterfaceName(), entry.getValue().getIp());
                                if (erps != null && !erps.isEmpty()) {
                                    String erpArray[] = erps.split(",");
                                    if (erpArray != null && erpArray.length > 0) {
                                        //erp去重
                                        erpSet.addAll(Arrays.asList(erpArray));
                                    }
                                }
                            }
                            //获取实例下的接口信息
                            if (!StringUtils.isEmpty(iface.getInterfaceName())) {
                                content.append(",").append(iface.getInterfaceName());
                                //获取其中的一个接口名即可
                                if (tempIfaceName == null) {
                                	tempIfaceName = iface.getInterfaceName();
                                }
                            }
                        }
                        alarm.setInterfaceName(tempIfaceName);
                        alarm.setAlarmIp(entry.getValue().getIp());
                        alarm.setErps(WorkerUtil.getStringFromSet(erpSet));
                        alarm.setContent(WorkerUtil.limitString(content.toString(), contentLimit));
                        alarm.setExtendKey2(entry.getKey() + "-" + String.valueOf(isOnline));
                        alarmList.add(alarm);
                    } catch (Exception e) {
                        logger.error(JSON.toJSONString(alarm) + "，上下线报警异常，error:" + e.getMessage(), e);
                    }
                }
            } finally {
                //保存到数据库中
                if (!alarmList.isEmpty()) {
                    scanNodeStatusHelper.saveAlarm(alarmList);
                }
            }
        } catch (Exception e) {
            logger.error("add alarm error:" + e.getMessage(), e);
        }
    }

    private void checkDeadInsToDel() {
        LogicDelAlarmUtil.checkDeadInsToDel(instanceManager, sysParamManager);
    }

    class InsVo {
        public InsVo(JsfIns jsfIns) {
            this.ins = jsfIns;
        }
        public JsfIns ins;
        public List<Client> clientList = new ArrayList<Client>();
        public List<Server> serverList = new ArrayList<Server>();
    }

}
