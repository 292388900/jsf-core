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
package com.ipd.jsf.worker.manager.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ipd.jsf.worker.dao.JsfInsDao;
import com.ipd.jsf.worker.dao.ScanStatusLogDao;
import com.ipd.jsf.worker.domain.*;
import com.ipd.jsf.worker.manager.InstanceManager;
import com.ipd.jsf.worker.util.DBLog;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;

@Service
public class InstanceManagerImpl implements InstanceManager {
    private int deleteLimit = 20;
    private static final Logger logger = LoggerFactory.getLogger(InstanceManagerImpl.class);
    private ExecutorService service = Executors.newFixedThreadPool(2, new WorkerThreadFactory("instanceManagerPool"));

    @Autowired
    private JsfInsDao jsfInsDao;

    @Autowired
    private ScanStatusLogDao scanStatusLogDao;

    @Override
    public int batchUpdateStatusOnline(List<JsfIns> list) throws Exception {
        if (list!= null && !list.isEmpty()) {
            return jsfInsDao.batchUpdateStatusOnline(list);
        }
        return 0;
    }

    @Override
    public int batchUpdateStatusOffline(List<JsfIns> list) throws Exception {
        if (list!= null && !list.isEmpty()) {
            return jsfInsDao.batchUpdateStatusOffline(list);
        }
        return 0;
    }

    @Transactional
    @Override
    public int batchUpdateDelYn(List<JsfIns> insList) throws Exception {
        int result = 0;
        if (insList != null && !insList.isEmpty()) {
            List<String> insKeyList = new ArrayList<String>();
            for (JsfIns ins : insList) {
                insKeyList.add(ins.getInsKey());
                if (insKeyList.size() > deleteLimit) {
                    result += jsfInsDao.batchUpdateDelYn(insKeyList);
                    DBLog.info("InstanceManagerImpl.batchUpdateDelYn, inskey:{}", insKeyList.toString());
                    insKeyList.clear();
                }
            }
            if (!insKeyList.isEmpty()) {
                result += jsfInsDao.batchUpdateDelYn(insKeyList);
                DBLog.info("InstanceManagerImpl.batchUpdateDelYn, inskey:{}", insKeyList.toString());
                insKeyList.clear();
            }
        }
        return result;
    }

    @Transactional
    @Override
    public int batchDeleteByInsKey(List<JsfIns> insList) throws Exception {
        ScanStatusLog log = null;
        Date createTime = new Date();
        int result = 0;
        if (insList != null && !insList.isEmpty()) {
            String creator = "scanstatus worker";
            List<String> insKeyList = new ArrayList<String>();
            for (JsfIns ins : insList) {
                insKeyList.add(ins.getInsKey());
                if (insKeyList.size() > deleteLimit) {
                    result += jsfInsDao.batchDeleteByInsKey(insKeyList);
                    DBLog.info("InstanceManagerImpl.batchDeleteByInsKey, inskey:{}", insKeyList.toString());
                    insKeyList.clear();
                }
            }
            if (!insKeyList.isEmpty()) {
                result += jsfInsDao.batchDeleteByInsKey(insKeyList);
                DBLog.info("InstanceManagerImpl.batchDeleteByInsKey, inskey:{}", insKeyList.toString());
                insKeyList.clear();
            }
            List<ScanStatusLog> logList = new ArrayList<ScanStatusLog>();
            for (JsfIns ins : insList) {
                log = new ScanStatusLog();
                log.setIp(ins.getIp());
                log.setPid(ins.getPid());
                log.setInsKey(ins.getInsKey());
                log.setType(DataEnum.ScanStatusLogType.instance.getValue());
                log.setDetailInfo("delete " + ins.toString());
                log.setCreateTime(createTime);
                log.setCreator(creator);
                log.setCreatorIp(WorkerUtil.getWorkerIP());
                logList.add(log);
                if (logList.size() > deleteLimit) {
                    scanStatusLogDao.create(logList);
                    logList.clear();
                }
            }
            if (!logList.isEmpty()) {
                scanStatusLogDao.create(logList);
                logList.clear();
            }
        }
        return result;
    }

    @Override
    public List<JsfIns> getOnlineInsBeforeTime(Date time, List<String> registryList) throws Exception {
        return jsfInsDao.getOnlineInsBeforeTime(time, registryList);
    }

    @Override
    public List<JsfIns> getOfflineInsAfterTime(Date time) throws Exception {
        return jsfInsDao.getOfflineInsAfterTime(time);
    }

    @Override
    public List<JsfIns> getOfflineInsBeforeTime(Date time, List<String> registryList) throws Exception {
        return jsfInsDao.getOfflineInsBeforeTime(time, registryList);
    }

    @Override
    public List<JsfIns> getDelYnInsBeforeTime(Date time) throws Exception {
        return jsfInsDao.getDelYnInsBeforeTime(time);
    }

    @Override
    public List<JsfIns> getInsListByKey(List<String> insKeyList) throws Exception {
        if (insKeyList != null && !insKeyList.isEmpty()) {
            return jsfInsDao.getInsListByKey(insKeyList);
        }
        return null;
    }

    @Override
    public Map<String, Instance> getInstanceInterfaceMap() throws Exception {
        final List<InstanceInterface> insIfaceList = Collections.synchronizedList(new ArrayList<InstanceInterface>());
        //每次取50000条
        final int limit = 50000;
        int start = 0;
        int page = 0;
        int totalCount = jsfInsDao.getInsIfaceListCount();
        int totalPage = 0;
        if (totalCount % limit == 0) {
            totalPage = totalCount / limit;
        } else {
            totalPage = totalCount / limit + 1;
        }
        final CountDownLatch latch = new CountDownLatch(totalPage);
        //分页获取实例与接口信息
        while (page < totalPage) {
            start = page * limit;
            page ++;
            final int startIndex = start;
            service.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        long startTime = System.currentTimeMillis();
                        List<InstanceInterface> list = jsfInsDao.getInsIfaceList(startIndex, limit);
                        if (list != null && !list.isEmpty()) {
	                        logger.info("elapse time:{}ms, startIndex:{}, count:{}, limit:{}", (System.currentTimeMillis() - startTime), startIndex, list.size(), limit);
	                        for (InstanceInterface insIface : list) {
	                        	if (insIface.getInsKey() == null
	            						|| insIface.getInsKey().isEmpty()
	            						|| insIface.getServerStatus() == InstanceStatus.unreg.value().intValue()
	            						|| insIface.getServerStatus() == InstanceStatus.deleted.value().intValue()) {
	            					continue;
	            				}
	                        	insIfaceList.add(insIface);
	                        }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    latch.countDown();
                }
            });
        }
        latch.await();
        Map<String, Instance> result = new HashMap<String, Instance>();
        if (!insIfaceList.isEmpty()) {
            for (InstanceInterface insIface : insIfaceList) {
            	if (insIface.getInsKey() == null || insIface.getInsKey().isEmpty()) {
            		continue;
            	}
                if (result.get(insIface.getInsKey()) == null) {
                    Instance ins = new Instance();
                    ins.setInterfaceIdSet(new HashSet<Integer>());
                    result.put(insIface.getInsKey(), ins);
                }
                result.get(insIface.getInsKey()).getInterfaceIdSet().add(insIface.getInterfaceId());
            }
        }
        return result;
    }

	@Override
	public JsfIns getJsfInsByInskey(String insKey) {
		return jsfInsDao.getJsfInsByInsKey(insKey);
	}
	
	@Override
	public Integer getAppIdByInsKey(String insKey) {
		return jsfInsDao.getAppIdByInsKey(insKey);
	}
	
	@Override
	public JsfApp getAppByInsKey(String insKey) {
		return jsfInsDao.getAppByInsKey(insKey);
	}

    @Override
    public List<LogicDelAlarmInfo> countLogicDelInsNodes(Map<String, Object> params) {
        return jsfInsDao.countLogicDelInsNodes(params);
    }

    @Override
    public List<LogicDelAlarmInfo> countInsNodes(Map<String, Object> params) {
        return jsfInsDao.countInsNodes(params);
    }

}
