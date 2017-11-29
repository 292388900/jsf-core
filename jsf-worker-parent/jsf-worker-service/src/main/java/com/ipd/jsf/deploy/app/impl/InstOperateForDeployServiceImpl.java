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
package com.ipd.jsf.deploy.app.impl;

import com.ipd.jsf.deploy.app.InstOperateForDeployService;
import com.ipd.jsf.deploy.app.domain.DeployRequest;
import com.ipd.jsf.deploy.app.error.FailInvokeException;
import com.ipd.jsf.gd.error.RpcException;
import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.worker.common.PropertyFactory;
import com.ipd.jsf.worker.common.ScheduleServerInfo;
import com.ipd.jsf.worker.common.utils.WorkerSwitchUtil;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.domain.JsfApp;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.manager.JsfAppManager;
import com.ipd.jsf.worker.manager.JsfDeployManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import com.ipd.jsf.worker.service.vo.CliEvent;
import com.ipd.jsf.worker.service.vo.CliEvent.Cause;
import com.ipd.jsf.worker.service.vo.CliEvent.NotifyType;
import com.ipd.jsf.worker.service.vo.CliEvent.Status;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InstOperateForDeployServiceImpl implements InstOperateForDeployService {
	
	protected final Logger LOGGER = LoggerFactory.getLogger(InstOperateForDeployServiceImpl.class);

	//服务防重  防止频繁调用 保存每个key对应的最近3分钟第一次调用的时间
	private ConcurrentHashMap<String, Long> onlineFirstInvokeMap = new ConcurrentHashMap<String, Long>();
	private ConcurrentHashMap<String, Long> offlineFirstInvokeMap = new ConcurrentHashMap<String, Long>();

	private ConcurrentHashMap<String, AtomicInteger> onlineInvokeTimesMap = new ConcurrentHashMap<String, AtomicInteger>();
	private ConcurrentHashMap<String, AtomicInteger> offlineInvokeTimesMap = new ConcurrentHashMap<String, AtomicInteger>();

	private ConcurrentHashMap<Integer, String> appTokenMap = new ConcurrentHashMap<Integer, String>();

	private final int time_threshold = 3; //最近三分钟
	private final int invoke_times = 10;      //每三分钟允许调用10次
	private final String WHITE_LIST = "inst.operate.fordeploy.whitelist";
	private volatile String whiteList = "";  //白名单

    //增加开关，控制开启和关闭事件同步
    private boolean useEventSyn = true;
    private final String eventSynWorkerName = "event.syn.workername";
    private final String eventSynToken = "event.syn.token";
    //event worker名称
    private String workerName = null;

	@Autowired
	private JsfDeployManager JsfDeployManagerImpl;
	
	@Autowired
	private SysParamManager sysParamManagerImpl;

	@Autowired
	private JsfAppManager jsfAppManagerImpl;

	private Executor threadPool = Executors.newFixedThreadPool(10, new WorkerThreadFactory("instOperateForDeploy_eventNotifyPool"));
	
	@Override
	public boolean doOnline(Integer appId, Integer appInsId) throws FailInvokeException{
		return true;
	}

	@Override
	public boolean doOffline(Integer appId, Integer appInsId)  throws FailInvokeException {
		return true;
	}

	@Override
	public boolean doInsOnline(DeployRequest request) throws Exception {
		return true;
	}

	@Override
	public boolean doInsOffline(DeployRequest request) throws Exception {
		boolean result = false;
		LOGGER.info("request appId:{}, appInsId:{}, pid:{}", request.getAppId(), request.getAppInsId(), request.getPid());
		if (request != null && request.getAppId() != null && request.getAppInsId() != null && request.getPid() > 0) {
			checkAppToken(request.getAppId(), request.getToken());
			checkInvokeTime("删除操作", request.getAppId(), request.getAppInsId(), request.getPid(), offlineFirstInvokeMap, offlineInvokeTimesMap);
			try {
				Date updateVersion = new Date();
				List<Server> servers = JsfDeployManagerImpl.delByDeploy(request.getAppId(), request.getAppInsId(), request.getPid(), updateVersion);
				if (servers != null && !servers.isEmpty()) {
					LOGGER.info("删除操作, appId: {}, appInsId: {}, pid: {}", request.getAppId(), request.getAppInsId(), request.getPid());
				} else {
					LOGGER.info("删除操作，没有找到要删除的实例和provider, appId: {}, appInsId: {}, pid: {}", request.getAppId(), request.getAppInsId(), request.getPid());
				}
				//发送event通知
				eventNotify(servers, Status.offline, Cause.unregister, updateVersion);
				result = true;
			} catch (Exception e) {
				String errMsg = "下线实例异常, appId:" + request.getAppId() + ", appInsId:" + request.getAppInsId() + ", pid:" + request.getPid();
				LOGGER.error(errMsg + ", " + e.getMessage(), e);
				throw new FailInvokeException(errMsg);
			}
		}
		return result;
	}

	/**
	 * 通知到event
	 * @param servers
	 * @param status
	 * @param cause
	 */
	private void eventNotify(final List<Server> servers, final Status status, final Cause cause, final Date date) {
		threadPool.execute(new Runnable() {
			@Override
			public void run() {
				if (servers != null && !servers.isEmpty()) {
					List<CliEvent> eventList = new ArrayList<CliEvent>();
					for (Server server : servers) {
						try {
							CliEvent event = new CliEvent();
							event.setHost(server.getIp());
							event.setPid(server.getPid());
							event.setPort(server.getPort());
							event.setStartTime(server.getStartTime());
							event.setAlias(server.getAlias());
							event.setProtocol(server.getProtocol());
							event.setRegistryId(WorkerUtil.getWorkerIP());
							event.setEventTimes(System.currentTimeMillis());
							event.setStatus(status);
							event.setCauseby(cause);
							event.setSafVer((short)server.getSafVer().intValue());
							event.setProvider(true);
							List<Integer> list = new ArrayList<Integer>();
							list.add(server.getInterfaceId());
							event.setInterfaceIds(list);
							event.setInterfaceId(server.getInterfaceId());
							event.setAliasVersion(date.getTime());
							if (server.getUniqKey() == null) {
								event.setNotify(NotifyType.onlyNotify);
							}
							eventList.add(event);
						} catch (Exception e) {
							LOGGER.error(e.getMessage(), e);
						}
					}
				}
			}
		});
	}
	
	/**
	 * 检查
	 * @param type
	 * @param appId
	 * @param appInsId
	 * @param pid
	 * @param firstInvokeMap
	 * @param invokeTimeMap
	 */
	private void checkInvokeTime(String type, Integer appId, String appInsId, int pid, ConcurrentHashMap<String, Long> firstInvokeMap, ConcurrentHashMap<String, AtomicInteger> invokeTimeMap) {
		long now = System.currentTimeMillis();
		String key = appId + "::" + appInsId;
		Long firstInvokeTime = firstInvokeMap.get(key);       // 每个三分钟内第一次调用的时间
		AtomicInteger invokeTimes = invokeTimeMap.get(key);   // 每个三分钟内已经调用的次数
		if (invokeTimes == null) {
			invokeTimes = new AtomicInteger(0);
		}
		if (firstInvokeTime == null) {
			firstInvokeTime = now;
			firstInvokeMap.put(key, firstInvokeTime);
		} else {
			long timeTofirstInvoke = (now - firstInvokeTime.longValue()) / 1000 / 60;// 距每个3分钟内第一次调用的时间间隔
			if (timeTofirstInvoke <= time_threshold) {// 当前时间在最近的三分钟内
				if (invokeTimes.getAndIncrement() >= invoke_times) {// 调用次数超过了阀值
					String errorMsg = type + ",超过调用阀值," + time_threshold + "分钟内不能超过" + invoke_times + "次调用." + "appId:" + appId + ",appInsId:" + appInsId + " ,pid:" + pid;
					LOGGER.error(errorMsg);
					throw new FailInvokeException(errorMsg);
				}
			} else {
				firstInvokeMap.remove(key);
				invokeTimeMap.remove(key);
			}
		}
		invokeTimeMap.put(key, invokeTimes);
	}

	private boolean checkAppToken(Integer appId, String token) throws Exception {
		if (appId != null && token != null && !token.isEmpty()) {
			if (token.equals(appTokenMap.get(appId))) {
				return true;
			}
		}
		throw new RpcException("token is error!");
	}

    /**
     * 事件worker，主从切换
     */
    @PostConstruct
    public void init() {
    	schedule();
    	initEvent();
    }

    private void schedule() {
    	ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("deploy-schedule-1"));
    	scheduledExecutorService.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				try {
					loadAuthor();
					clearMap();
					loadAppToken();
				} catch (Exception e) {
					LOGGER.error(e.getMessage(), e);
				}
			}
    	}, 5, 300, TimeUnit.SECONDS);   //5分钟加载一次
    }

    /**
     * 加载白名单
     */
    private void loadAuthor() {
    	try {
			whiteList = sysParamManagerImpl.findValueBykey(WHITE_LIST);
			LOGGER.info("load white list:{}", whiteList);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
    }

    /**
     * 清除map
     */
    private void clearMap() {
    	try {
			long now = System.currentTimeMillis();
			long interval = time_threshold * 60 * 1000;
			if (!onlineFirstInvokeMap.isEmpty()) {
				//清除上线的记录
				List<String> removeList = new ArrayList<String>();
				for (String key : onlineFirstInvokeMap.keySet()) {
					if (now - onlineFirstInvokeMap.get(key).longValue() > interval) {
						removeList.add(key);
					}
				}
				for (String key : removeList) {
					onlineFirstInvokeMap.remove(key);
					onlineInvokeTimesMap.remove(key);
				}
			}
			if (!offlineFirstInvokeMap.isEmpty()) {
				//清除下线的记录
				List<String> removeList = new ArrayList<String>();
				for (String key : offlineFirstInvokeMap.keySet()) {
					if (now - offlineFirstInvokeMap.get(key).longValue() > interval) {
						removeList.add(key);
					}
				}
				for (String key : removeList) {
					offlineFirstInvokeMap.remove(key);
					offlineInvokeTimesMap.remove(key);
				}
			}
			LOGGER.info("clear is over. onlineFirstInvokeMap:{}, onlineInvokeTimesMap:{}, offlineFirstInvokeMap:{}, offlineInvokeTimesMap:{}", onlineFirstInvokeMap.size(),
					onlineInvokeTimesMap.size(), offlineFirstInvokeMap.size(), offlineInvokeTimesMap.size());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			
		}
    }

    private void loadAppToken() {
    	List<JsfApp> jsfAppList = jsfAppManagerImpl.getAppListWithToken();
    	if (jsfAppList != null && !jsfAppList.isEmpty()) {
    		Map<Integer, String> map = new HashMap<Integer, String>();
    		for (JsfApp app : jsfAppList) {
    			map.put(app.getAppId(), app.getToken());
    			appTokenMap.put(app.getAppId(), app.getToken());
    		}
    		
    		Set<Integer> set = new HashSet<Integer>();
    		for (Integer key : appTokenMap.keySet()) {
    			if (!map.containsKey(key)) {
    				set.add(key);
    			}
    		}
    		for (Integer key : set) {
    			appTokenMap.remove(key);
    		}
    		LOGGER.info("load app token is finished! appTokenMap size:{}", appTokenMap.size());
    		
    	}
    }
    
    
    private void initEvent() {
        try {
            workerName = (String)PropertyFactory.getProperty(eventSynWorkerName);
            if (useEventSyn) {
                ScheduleServerInfo serverInfo = WorkerSwitchUtil.getWorkerMasterServerInfo(workerName, new WorkerSwitchUtil.MasterSwitchCallback() {
                    @Override
                    public void execute(ScheduleServerInfo newestServerInfo) {
                    	LOGGER.info("-----------switch new master-------"+newestServerInfo.getWorkerType()+"-----id--"+newestServerInfo.getId());
                        getRefer(newestServerInfo);
                    }
                });
                getRefer(serverInfo);
            }
        } catch (Exception e) {
        	LOGGER.error(e.getMessage(), e);
        }
    }

    
    /**
     * @param serverInfo
     */
    private void getRefer(ScheduleServerInfo serverInfo) {
        if (serverInfo != null) {
            if (useEventSyn) {
                try {
                    String ip = serverInfo.getWorkerParameters().getString("ip");
                    int port = serverInfo.getWorkerParameters().getIntValue("port");

                    LOGGER.info("-----------get new master-------"+serverInfo.getWorkerType()+"-----id--"+serverInfo.getId());
                } catch (Exception e) {
                	LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

}
