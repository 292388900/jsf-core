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
package com.ipd.jsf.worker.thread.jsfins;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.ipd.jsf.worker.domain.Client;
import com.ipd.jsf.worker.domain.IfaceInsStat;
import com.ipd.jsf.worker.domain.Server;
import com.ipd.jsf.worker.manager.ClientManager;
import com.ipd.jsf.worker.manager.IfaceInsStatManager;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import com.ipd.jsf.worker.manager.ServerManager;
import com.ipd.jsf.worker.util.WorkerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.gd.util.NamedThreadFactory;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.domain.InterfaceInfo;

public class JsfIfaceInsStatWorker extends SingleWorker{
	
	private static final Logger logger = LoggerFactory.getLogger(JsfIfaceInsStatWorker.class);
	
	private ExecutorService executorService = null;
	
	@Autowired
    IfaceInsStatManager ifaceInsStatManager;
	
	@Autowired
    InterfaceInfoManager infoManager;
	
	@Autowired
    ClientManager clientManager;
	
	@Autowired
    ServerManager serverManager;

	@Override
	public boolean run() {
		try {
			executorService = Executors.newFixedThreadPool(10, new NamedThreadFactory("IfaceInsStat"));
			Calendar calendar = Calendar.getInstance();
			final int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
			final int newYear = calendar.getWeekYear();
			List<InterfaceInfo> infos = infoManager.getAllWithJsfclient(null);
			
			List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
			
			if (!CollectionUtils.isEmpty(infos)) {
				for (final InterfaceInfo node : infos) {
					Future<Boolean> task = executorService.submit(new Callable<Boolean>() {
						
						@Override
						public Boolean call() throws Exception {
							try {
								
								IfaceInsStat vo = new IfaceInsStat();
								vo.setInterfaceName(node.getInterfaceName());
								vo.setWeek(weekOfYear);
								vo.setCreateTime(WorkerUtil.getStartTimeOfWeekNo(newYear, weekOfYear));
								boolean exists = ifaceInsStatManager.exists(vo);
								if(exists){
									logger.info("接口{}对应第{}周的数据已经存在", vo.getInterfaceName(), vo.getWeek());
								}else {
									doStat(node, vo, newYear);
								}
								
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								Thread.currentThread().interrupt();
							}
							return true;
						}
					});
					
					tasks.add(task);
				}
			}
			
			for(Future<Boolean> task : tasks){
				try {
					task.get();
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
			if(executorService != null){
				executorService.shutdown();
				executorService = null;
			}
			
		}
		
		return false;
	}
	
	
	private void doStat(InterfaceInfo node, IfaceInsStat vo, int newYear) throws Exception{
		int weekOfYear = vo.getWeek();
		long cInstanceNum = 0;
		long pInstanceNum = 0;
		Set<String> cIPsSet = new HashSet<String>();
		Set<String> pIPsSet = new HashSet<String>();
		Set<String> totalIPsSet = new HashSet<String>();
		
		List<Server> providers = serverManager.getJsfServers(node.getInterfaceName());

		List<Client> consumers = clientManager.getJsfClients(node.getInterfaceName());
		// 按实例
		if (consumers != null) {
			cInstanceNum += consumers.size();
		}
		
		// 对provider, 只记录存活的节点
		if (providers != null) {
			pInstanceNum += providers.size();
		}

		for (Server key : providers) {
			pIPsSet.add(key.getIp());
			totalIPsSet.add(key.getIp());
		}
		
		for (Client key : consumers) {
			cIPsSet.add(key.getIp());
			totalIPsSet.add(key.getIp());
		}
		int preYear = newYear;
		int preWeek = weekOfYear - 1;
		if (preWeek == 0) {
			preWeek = 52;
			preYear = newYear - 1;
		}
		logger.info("获取接口{}第{}周的数据", node.getInterfaceName(), preWeek);
		IfaceInsStat params = new IfaceInsStat();
		params.setWeek(preWeek);
		params.setCreateTime(WorkerUtil.getStartTimeOfWeekNo(preYear, preWeek));
		IfaceInsStat lastWeekStat = ifaceInsStatManager.getLastWeek(params);
		
		IfaceInsStat newVo = new IfaceInsStat();
		
		if(lastWeekStat == null){
			lastWeekStat = new IfaceInsStat();
		}

		newVo.setInterfaceName(node.getInterfaceName());
		newVo.setCinsNum(cInstanceNum);
		newVo.setCipNum(cIPsSet.size());
		newVo.setCreateTime(new Date());
		newVo.setInsTotalNum(pInstanceNum + cInstanceNum);
		newVo.setIpTotalNum(totalIPsSet.size());
		newVo.setPinsNum(pInstanceNum);
		newVo.setPipNum(pIPsSet.size());
		newVo.setWeek(weekOfYear);
		
		newVo.setCinsAdd(cInstanceNum - lastWeekStat.getCinsNum());
		newVo.setCipAdd(cIPsSet.size() - lastWeekStat.getCipNum());
		newVo.setInsTotalAdd(pInstanceNum + cInstanceNum - lastWeekStat.getInsTotalNum());
		newVo.setIpTotalAdd(totalIPsSet.size() - lastWeekStat.getIpTotalNum());
		newVo.setPinsAdd(pInstanceNum - lastWeekStat.getPinsNum());
		newVo.setPipAdd(pIPsSet.size() - lastWeekStat.getPipNum());
		
		logger.info("接口{}对应第{}周的数据开始入库", newVo.getInterfaceName(), newVo.getWeek());
		ifaceInsStatManager.insert(newVo);
	}

	@Override
	public String getWorkerType() {
		return "safIfaceInsStatWorker";
	}

//	@Override
//	public String cronExpression() {
//		// 每周四到周六的下午六点执行
//		return "0 0/10 * * * ?";
//		return "0 0 18 ? * THU-SAT";
//	}

}
