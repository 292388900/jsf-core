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
package com.ipd.jsf.registry.manager.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.worker.service.vo.CliEvent.NotifyType;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.dao.InsServerDao;
import com.ipd.jsf.registry.dao.InterfaceDataVersionDao;
import com.ipd.jsf.registry.dao.ScanStatusLogDao;
import com.ipd.jsf.registry.dao.ServerDao;
import com.ipd.jsf.registry.domain.IfaceAliasVersion;
import com.ipd.jsf.registry.domain.InsServer;
import com.ipd.jsf.registry.domain.ScanStatusLog;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.manager.ServerManager;
import com.ipd.jsf.registry.service.EventSynService;
import com.ipd.jsf.registry.util.DBLog;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.util.DateTimeZoneUtil;
import com.ipd.jsf.version.common.domain.IfaceAlias;
import com.ipd.jsf.version.common.domain.IfaceServer;
import com.ipd.jsf.version.common.service.AliasVersionService;

@Service
public class ServerManagerImpl implements ServerManager {

    private static Logger logger = LoggerFactory.getLogger(ServerManagerImpl.class);
    private final int APPPATH_LENGTH = 127;
    private final int ATTRURL_LENGTH = 254;
    private final int URLDESC_LENGTH = 1022;
    
    @Autowired
    private ServerDao serverDao;

    @Autowired
    private InterfaceDataVersionDao interfaceDataVersionDao;

    @Autowired
    private AliasVersionService aliasVersionService;
    
    @Autowired
    private ScanStatusLogDao scanStatusLogDao;

    @Autowired
    private InsServerDao insServerDao;

    @Autowired
    private EventSynService eventSynServiceImpl;
    
    /**
     * 填补server对象中的值
     * 1. 设置状态为 online
     * 2. 获取uniquekey
     * 3. 截取字符长度
     * @param server
     */
    private void prepareServer(Server server) throws Exception {
        if (server.getStatus() == null || server.getStatus().intValue() == 0) {
            server.setStatus(InstanceStatus.online.value());
        }
        server.setAppPath(RegistryUtil.limitString(server.getAppPath(), APPPATH_LENGTH));
        server.setAttrUrl(RegistryUtil.limitString(server.getAttrUrl(), ATTRURL_LENGTH));
        server.setUrlDesc(RegistryUtil.limitString(server.getUrlDesc(), URLDESC_LENGTH));
    }

    /**
     * 创建或者修改db中的server
     * 1. 填补server信息
     * 2. 判断server已经是否存在，如果不存在，则创建，如果存在，则更新
     * @throws Exception 
     */
    private List<IfaceAlias> registerServer(Server server, Date date, int updateFlag) throws Exception {
        prepareServer(server);
        return createServerDB(server, date);
    }

	@Override
	public void registerServer(List<Server> serverList, List<Integer> updateFlagList) throws Exception {
		if (serverList == null || serverList.isEmpty()) return;
		List<Server> eventServerList = new ArrayList<Server>();
		List<NotifyType> isNotifyList = new ArrayList<NotifyType>();
		Date updateVersion = new Date();
		int updateFlag;
		for (int i = 0; i < serverList.size(); i++) {
			Server server = serverList.get(i);
			try {
				updateFlag = updateFlagList.get(i);
			} catch (Exception e) {
				logger.error(e.getMessage());
				updateFlag = RegistryConstants.SERVER_NOT_EXIST;
			}
			List<IfaceAlias> aliasList = registerServer(server, updateVersion, updateFlag);
			getEventServerList(eventServerList, isNotifyList, server, aliasList);
		}
        try {
            //事件同步
            eventSynServiceImpl.eventCollectServerRegister(eventServerList, updateVersion, isNotifyList);
        } catch (Exception e) {
            logger.error("event syn is error:{}", e.getMessage());
        }
	}

    private Server cloneServer(Server server, IfaceAlias ifaceAlias) {
		if (server.getIp().equals(RegistryUtil.getRegistryIP()) && server.getPort() == RegistryUtil.getRegistryPort()) {
            return null;
        }
		Server result = new Server();
		result.setIp(server.getIp());
		result.setPid(server.getPid());
		result.setPort(server.getPort());
		result.setStartTime(server.getStartTime());
		//记录动态分组的alias
		result.setAlias(ifaceAlias.getAlias());
		result.setProtocol(server.getProtocol());
		result.setInterfaceId(server.getInterfaceId());
		return result;
	}

	@Override
	public boolean unRegisterServer(List<Server> serverList) throws Exception {
		List<Server> eventServerList = new ArrayList<Server>();
		List<NotifyType> isNotifyList = new ArrayList<NotifyType>();
		Date updateVersion = new Date();
		for (Server server : serverList) {
			List<IfaceAlias> aliasList = unRegisterServer(server, updateVersion);
			getEventServerList(eventServerList, isNotifyList, server, aliasList);
		}
		try {
            //事件同步
            eventSynServiceImpl.eventCollectServerUnRegister(eventServerList, updateVersion, isNotifyList);
        } catch (Exception e) {
            logger.error("event syn is error:{}", e.getMessage());
        }
		return true;
	}

	/**
	 * @param eventServerList
	 * @param isNotifyList
	 * @param server
	 * @param aliasList
	 */
	private void getEventServerList(List<Server> eventServerList, List<NotifyType> isNotifyList, Server server, List<IfaceAlias> aliasList) {
		if (!CollectionUtils.isEmpty(aliasList)) {
			//将动态分组的alias也加到event事件中
			for (IfaceAlias ifaceAlias : aliasList) {
				if (server.getAlias().equals(ifaceAlias.getAlias())) {
					eventServerList.add(server);
					//server的原始alias，通知并记录上下线日志
					isNotifyList.add(NotifyType.all);
				} else {
					eventServerList.add(cloneServer(server, ifaceAlias));
					//动态分组的只通知，不记录上下线日志
					isNotifyList.add(NotifyType.onlyNotify);
				}
			}
		}
	}

	/**
	 * 取消注册
	 * 删除provider节点
	 */
	private List<IfaceAlias> unRegisterServer(Server server, Date updateVersion) throws Exception {
		if (server == null) return null;
		int result = 0;
        result = serverDao.updateToUnreg(server.getUniqKey(), server.getPid(), server.getStartTime(), DateTimeZoneUtil.getTargetTime().getTime());
        interfaceDataVersionDao.update(server.getInterfaceId(), updateVersion);
        List<IfaceAlias> ifaceAliasList = aliasVersionService.updateByServerList(this.getIfaceServerList(server), updateVersion);
    	recordLog(server, updateVersion);
    	if (result > 0) {
    		DBLog.info("save unregister server succ:{}", server.toString());
    	} else {
    		DBLog.warn("save unregister server fail:{}", server.toString());
    	}
        return ifaceAliasList;
	}

	private void recordLog(Server server, Date date) throws Exception {
		ScanStatusLog log = new ScanStatusLog();
        try {
        	log.setIp(server.getIp());
        	log.setPid(server.getPid());
        	log.setInsKey(server.getInsKey());
        	log.setInterfaceName(server.getInterfaceName());
        	log.setType(DataEnum.ScanStatusLogType.server.getValue());
        	log.setDetailInfo(server.toString());
        	log.setCreator("registry");
        	log.setCreatorIp(RegistryUtil.getRegistryIP());
        	log.setCreateTime(date);
            scanStatusLogDao.create(log);
        } catch (Exception e) {
        	logger.error("saving logs of deleteServer is failed. log:" + log.toString() + ", " + e.getMessage(), e);
		}
	}

    /**
     * 创建或更新server到mysql中
     * @param server
     * @param date
     */
    private List<IfaceAlias> createServerDB(Server server, Date date) throws Exception {
        boolean needCreate = true;      //如果数据库中不存在这条记录，就创建
        boolean updateVersion = false;
        List<IfaceAlias> aliasList = null;
        Server queryServer = serverDao.getServerByUniqkey(server.getUniqKey());
        if (queryServer != null) {    //不为null，表示已经存在，就更新
        	resolveSrcType(queryServer, server);
        	if (!compareServer(queryServer, server)) {   //比较之后，不一致，就更新
	        	if (serverDao.update(server) > 0) {
	                DBLog.info("succ update server:{}", server);
	                needCreate = false;
	                updateVersion = true;   //需要更新版本
	            } else {
	                DBLog.info("fail update server:{}", server);
	            }
        	} else {
        		logger.info("no operate server:{}", server);
        		return aliasList;     //如果一致，就不用再创建了
        	}
        }

        if (needCreate) {   //需要创建server
            try {
                int i = serverDao.create(server);
                updateVersion = true;
                DBLog.info("add server, result:{}, {}", i, server);
            } catch (DuplicateKeyException e) {  //可能的情况，缓存还未加载，但数据库里有
                logger.warn("server duplicate key:{}, warn:{}", server.getUniqKey(), e.getMessage());
                if (serverDao.update(server) > 0) {
                	updateVersion = true;   //需要更新版本
                    DBLog.info("succ. Update server:{}", server);
                } else {
                    DBLog.info("fail. update server:{}", server);
                }
            } catch (DataIntegrityViolationException e) {  //可能的情况，缓存还未加载，但数据库里有
                logger.warn("server duplicate key:{}, warn2:{}", server.getUniqKey(), e.getMessage());
                if (serverDao.update(server) > 0) {
                	updateVersion = true;   //需要更新版本
                    DBLog.info("succ. Update server:{}", server);
                } else {
                    DBLog.info("fail. update server:{}", server);
                }
            }
        }
        try {
        	insServerDao.create(getInsServer(server));
		} catch (Exception e) {
			//关联表的异常，不做任何处理
			logger.error(e.getMessage() + server.toString(), e);
		}
        if (updateVersion) {  //需要更新版本号
            interfaceDataVersionDao.update(server.getInterfaceId(), date);
            aliasList = aliasVersionService.updateByServerList(this.getIfaceServerList(server), date);
        }
        return aliasList;
    }

    private InsServer getInsServer(Server server) {
    	InsServer insServer = new InsServer();
    	insServer.setInsKey(server.getInsKey());
    	insServer.setServerUniqkey(server.getUniqKey());
    	insServer.setCreateTime(server.getCreateTime());
    	return insServer;
    }

    /**
     * 更新srcType
     * @param queryServer
     * @param updateServer
     */
    private void resolveSrcType(Server queryServer, Server updateServer) {
    	if (queryServer.getStatus().intValue() == InstanceStatus.deleted.value().intValue() 
    			&& queryServer.getSrcType().intValue() == SourceType.manual.value() 
    			&& updateServer.isReReg() == false) {    //手动添加的节点被删除并重启后，将srcType改为1； updateServer.isReReg() == false  表示第一次注册
    		updateServer.setSrcType(SourceType.registry.value());
    	} else if (queryServer.getSrcType().intValue() == SourceType.manual.value()) {
    		updateServer.setSrcType(SourceType.manual.value());
    	} else {
    		updateServer.setSrcType(SourceType.registry.value());
    	}
    }

    /**
     * 比较server，如果一致，返回true，如果不一致返回false
     * @param srcServer
     * @param newServer
     * @return
     */
    private boolean compareServer(Server srcServer, Server newServer) {
    	//如果是重复注册，并且是手动添加节点，就不做任何操作. 如果是第一次注册，就继续往下判断
    	if (newServer.isReReg() == true && srcServer.getSrcType().intValue() == SourceType.manual.value()) {
    		return true;
    	}
    	if (srcServer.getPid() != newServer.getPid()) {
    		return false;
    	}
    	if (srcServer.getStatus().intValue() != newServer.getStatus().intValue()) {
    		return false;
    	}
    	if (srcServer.getRoom() != newServer.getRoom()) {
    		return false;
    	}
    	return true;
    }

    @Override
    public Map<Integer, List<Server>> getServersByIfaceIdAliasList(List<IfaceAliasVersion> ifaceIdAliasList) throws Exception {
    	//key:interfaceId, value:serverlist
    	Map<Integer, List<Server>> result = new HashMap<Integer, List<Server>>();
    	if (ifaceIdAliasList == null || ifaceIdAliasList.size() == 0) {
    		return result;
    	}
    	List<Server> servers = serverDao.getServersByIfaceIdAliasList(ifaceIdAliasList);
    	for (Server server : servers) {
    		if (result.get(server.getInterfaceId()) == null) {
    			result.put(server.getInterfaceId(), new ArrayList<Server>());
    		}
    		server.setAttrUrl(RegistryUtil.getAttrMap(server.getAttrUrl()).toString());
    		result.get(server.getInterfaceId()).add(server);
    	}
    	return result;
    }

    private IfaceServer getIfaceServer(Server server) {
    	IfaceServer ifaceServer = new IfaceServer();
    	ifaceServer.setAlias(server.getAlias());
    	ifaceServer.setInterfaceId(server.getInterfaceId());
    	ifaceServer.setUniqKey(server.getUniqKey());
    	return ifaceServer;
    }

    private List<IfaceServer> getIfaceServerList(List<Server> serverList) {
    	List<IfaceServer> list = new ArrayList<IfaceServer>();
    	for (Server server : serverList) {
    		list.add(getIfaceServer(server));
    	}
    	return list;
    }

    private List<IfaceServer> getIfaceServerList(Server server) {
    	ArrayList<Server> list = new ArrayList<Server>();
    	list.add(server);
    	return getIfaceServerList(list);
    }
}
