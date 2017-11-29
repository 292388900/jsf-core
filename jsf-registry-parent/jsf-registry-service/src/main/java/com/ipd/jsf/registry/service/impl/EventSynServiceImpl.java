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
package com.ipd.jsf.registry.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.error.NoAliveProviderException;
import com.ipd.jsf.gd.transport.Callback;
import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.service.EventSynService;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.worker.common.ScheduleServerInfo;
import com.ipd.jsf.worker.common.utils.WorkerSwitchUtil;
import com.ipd.jsf.worker.service.vo.CliEvent;
import com.ipd.jsf.worker.service.vo.RegistryInfo;
import com.ipd.jsf.worker.service.vo.CliEvent.Cause;
import com.ipd.jsf.worker.service.vo.CliEvent.NotifyType;
import com.ipd.jsf.worker.service.vo.CliEvent.Status;
import com.ipd.jsf.worker.service.JsfEventBus;

@Service
public class EventSynServiceImpl implements EventSynService {
    private Logger logger = LoggerFactory.getLogger(EventSynServiceImpl.class);
    private JsfEventBus eventBus;
    //增加开关，控制开启和关闭事件同步
    private boolean useEventSyn = true;
    //是否需要注册。如果eventworker主从切换，或者重启，需要重新注册
    private Callback<List<CliEvent>, String> callback = null;
    private RegistryInfo info = null;
    private final String eventSynFlag = "event.syn.flag";
    private final String eventSynWorkerName = "event.syn.workername";
    private final String eventSynToken = "event.syn.token";

    //event worker名称
    private String workerName = null;

    private ConsumerConfig<JsfEventBus> consumerConfig = null;

    /* (non-Javadoc)
     * @see com.ipd.saf.registry.service.EventSynService#register(com.ipd.saf.worker.vo.RegistryInfo, com.ipd.saf.gd.transport.Callback)
     */
    @Override
    public void register(RegistryInfo info, Callback<List<CliEvent>, String> callback) throws Exception {
        if (useEventSyn) {
            if (info == null || callback == null) {
                return;
            }
            try {
                if (this.callback == null) {
                    //记录下callback
                    this.callback = callback;
                }
                if (this.info == null) {
                    //记录注册信息
                    this.info = info;
                }
                //开始注册
                eventBus.register(info, callback);
                logger.info("event Register is successful : {}", info.toString());
            } catch (NoAliveProviderException e) {
                //如果没有provider，就重新检查下
                reloadMasterNode();
                logger.warn("{}, 重新refer...", e.getMessage());
                eventBus.register(info, callback);
            }
        } else {
            logger.info("event flag is false.");
        }
    }

    /* (non-Javadoc)
     * @see com.ipd.saf.registry.service.EventSynService#unregister(com.ipd.saf.worker.vo.RegistryInfo)
     */
    @Override
    public void unregister(RegistryInfo info) throws Exception {
        if (useEventSyn) {
            eventBus.unregister(info);
        }
    }

    @Override
    public void eventCollectInsDisconnect(JsfIns ins) throws Exception {
        if (ins == null) return;
        if (useEventSyn) {
            CliEvent event = new CliEvent();
            event.setHost(ins.getIp());
            event.setPid(ins.getPid());
            event.setStartTime(ins.getStartTime());
            event.setRegistryId(RegistryUtil.getRegistryIPPort());
            event.setEventTimes(RegistryUtil.getSystemCurrentTime());
            event.setStatus(Status.offline);
            event.setCauseby(Cause.connBreak);
            eventBus.collect(event);
        }
    }

	@Override
	public void eventCollectServerRegister(List<Server> serverList, Date updateVersion, List<NotifyType> isNotifyList) throws Exception {
		if (serverList == null || serverList.isEmpty()) return;
		if (useEventSyn) {
			List<CliEvent> eventList = new ArrayList<CliEvent>();
			CliEvent event;
			for (int i = 0; i < serverList.size(); i++) {
				event = getEventFromServer(serverList.get(i), updateVersion, Status.online, Cause.register, isNotifyList.get(i));
				if (event != null) {
					eventList.add(event);
				}
			}
			if (!eventList.isEmpty()) {
				eventBus.collectList(eventList);
			}
		}
	}

	private CliEvent getEventFromServer(Server server, Date updateVersion, Status status, Cause cause, NotifyType notifyType) {
		if (server.getIp().equals(RegistryUtil.getRegistryIP()) && server.getPort() == RegistryUtil.getRegistryPort()) {
            return null;
        }
		CliEvent event = new CliEvent();
		event.setHost(server.getIp());
		event.setPid(server.getPid());
		event.setPort(server.getPort());
		event.setStartTime(server.getStartTime());
		event.setAlias(server.getAlias());
		event.setProtocol(server.getProtocol());
		event.setRegistryId(RegistryUtil.getRegistryIPPort());
		event.setEventTimes(RegistryUtil.getSystemCurrentTime());
		event.setStatus(status);
		event.setCauseby(cause);
		event.setSafVer((short)server.getSafVer());
		event.setProvider(true);
		event.setNotify(notifyType);
		List<Integer> list = new ArrayList<Integer>();
		list.add(server.getInterfaceId());
		event.setInterfaceIds(list);
		event.setInterfaceId(server.getInterfaceId());
		event.setAliasVersion(updateVersion.getTime());
		return event;
	}

	@Override
	public void eventCollectClientRegister(List<Client> clientList) throws Exception {
		if (clientList == null || clientList.isEmpty()) return;
		if (useEventSyn) {
			List<CliEvent> eventList = new ArrayList<CliEvent>();
			CliEvent event;
			for (Client client : clientList) {
				event = getEventFromClient(client, Status.online, Cause.register);
				if (event != null) {
					eventList.add(event);
				}
			}
			eventBus.collectList(eventList);
		}
	}

	private CliEvent getEventFromClient(Client client, Status status, Cause cause) {
		CliEvent event = new CliEvent();
		event.setHost(client.getIp());
		event.setPid(client.getPid());
		event.setStartTime(client.getStartTime());
		event.setAlias(client.getAlias());
		event.setProtocol(client.getProtocol());
		event.setRegistryId(RegistryUtil.getRegistryIPPort());
		event.setEventTimes(RegistryUtil.getSystemCurrentTime());
		event.setStatus(status);
		event.setCauseby(cause);
		event.setSafVer((short)client.getSafVer());
		event.setProvider(false);
		List<Integer> list = new ArrayList<Integer>();
		list.add(client.getInterfaceId());
		event.setInterfaceIds(list);
		event.setInterfaceId(client.getInterfaceId());
		event.setNotify(NotifyType.onlyRecord);
		return event;
	}

	@Override
	public void eventCollectServerUnRegister(List<Server> serverList, Date updateVersion, List<NotifyType> isNotifyList) throws Exception {
		if (serverList == null || serverList.isEmpty()) return;
		if (useEventSyn) {
			List<CliEvent> eventList = new ArrayList<CliEvent>();
			CliEvent event;
			for (int i = 0; i < serverList.size(); i++) {
				event = getEventFromServer(serverList.get(i), updateVersion, Status.offline, Cause.unregister, isNotifyList.get(i));
				if (event != null) {
					eventList.add(event);
				}
			}
			eventBus.collectList(eventList);
		}
	}

	@Override
	public void eventCollectClientUnRegister(List<Client> clientList) throws Exception {
		if (clientList == null || clientList.isEmpty()) return;
		if (useEventSyn) {
			List<CliEvent> eventList = new ArrayList<CliEvent>();
			CliEvent event;
			for (Client client : clientList) {
				event = getEventFromClient(client, Status.offline, Cause.unregister);
				if (event != null) {
					eventList.add(event);
				}
			}
			eventBus.collectList(eventList);
		}
	}

    /**
     * 事件worker，主从切换
     */
    @PostConstruct
    public void init() {
        try {
            String eventsynflag = String.valueOf(PropertyFactory.getProperty(eventSynFlag));
            useEventSyn = eventsynflag.equals("true") ? true : false;
            workerName = (String)PropertyFactory.getProperty(eventSynWorkerName);
            if (useEventSyn) {
                ScheduleServerInfo serverInfo = WorkerSwitchUtil.getWorkerMasterServerInfo(workerName, new WorkerSwitchUtil.MasterSwitchCallback() {
                    @Override
                    public void execute(ScheduleServerInfo newestServerInfo) {
                        logger.info("-----------switch new master-------"+newestServerInfo.getWorkerType()+"-----id--"+newestServerInfo.getId());
                        getRefer(newestServerInfo);
                    }
                });
                getRefer(serverInfo);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
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

                    newConsumerConfig(ip, port);
                    if (eventBus == null) {
                        eventBus = consumerConfig.refer();
                    } else {
                        synchronized (eventBus) {
                            eventBus = consumerConfig.refer();
                        }
                    }
                    logger.info("-----------get new master-------"+serverInfo.getWorkerType()+"-----id--"+serverInfo.getId());
                    //节点切换后，需要重新注册下
                    register(this.info, this.callback);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /**
     * 创建consumerconfig
     * @param ip
     * @param port
     * @return
     */
    private void newConsumerConfig(String ip, int port) {
        if (consumerConfig != null) {
            consumerConfig.unrefer();
        }
        consumerConfig = new ConsumerConfig<JsfEventBus>();
        consumerConfig.setInterfaceId(JsfEventBus.class.getName());
        consumerConfig.setProtocol("jsf");
        consumerConfig.setAlias("saf");
        consumerConfig.setUrl(ip + ":" + port);
        consumerConfig.setCheck(false);
        if (PropertyFactory.getProperty(eventSynToken) != null) {
            consumerConfig.setParameter(".token", String.valueOf(PropertyFactory.getProperty(eventSynToken)));
        }
        consumerConfig.setRegister(false);//打开注释表示不走注册中心
    }

    /**
     * 重新获取主节点
     */
    private void reloadMasterNode() {
        ScheduleServerInfo serverInfo = WorkerSwitchUtil.getWorkerMasterServerInfo(workerName, null);
        getRefer(serverInfo);
    }
}
