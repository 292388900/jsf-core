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
package com.ipd.jsf.worker.thread.registry;

import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import com.ipd.jsf.worker.domain.JsfRegAddr;
import com.ipd.jsf.worker.manager.AlarmManager;
import com.ipd.jsf.worker.manager.SysParamManager;
import com.ipd.jsf.worker.service.JsfRegAddrService;
import com.ipd.jsf.worker.service.JsfRegistryService;
import com.ipd.jsf.worker.util.PropertyUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.gd.GenericService;
import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.util.Constants;
import com.ipd.jsf.gd.util.RpcContext;
import com.ipd.jsf.common.util.SafTelnetClient;
import com.ipd.jsf.service.RegistryStatusService;
import com.ipd.jsf.worker.common.SingleWorker;

public class RegistryStatusWorker extends SingleWorker{
	
	private final static Logger logger = LoggerFactory.getLogger(RegistryStatusWorker.class);
	
	private static int error = 1;
	
	@Autowired
    JsfRegAddrService safRegAddrService;
	
	@Autowired
    SysParamManager sysParamManager;
	
	@Autowired
    AlarmManager alarmManager;
	
	@Autowired
    JsfRegistryService safRegistryService;
	
	private static ConsumerConfig<GenericService> consumerConfig = null;

	private final static String serverProtocol = PropertyUtil.getProperties("jsf.server.protocol");
	private final static String alias = PropertyUtil.getProperties("jsf.server.alias");

	private GenericService genericService;

	private static String directUrl = null;
	
	@PostConstruct
	public void init(){
		if(consumerConfig == null){
			directUrl = safRegAddrService.getAllUrl();
			consumerConfig = new ConsumerConfig<GenericService>();
			consumerConfig.setInterfaceId(RegistryStatusService.class.getCanonicalName());
			consumerConfig.setProtocol(serverProtocol);
			consumerConfig.setAlias(alias);
			consumerConfig.setGeneric(true);
			consumerConfig.setUrl(directUrl);
			consumerConfig.setCluster(Constants.CLUSTER_TRANSPORT_PINPOINT);
			consumerConfig.setCheck(false);
			consumerConfig.setTimeout(7000);
			logger.info("实例SAFServiceConfig");
			genericService = consumerConfig.refer();
		}
	}
	
	private GenericService reRefer(){
		consumerConfig.unrefer();
		consumerConfig.setUrl(directUrl);
		genericService = consumerConfig.refer();
		return genericService;
	}

	@Override
	public boolean run() {
		try {
			Date date = new Date();
			List<JsfRegAddr> addrs = safRegAddrService.listAll();
			String newUrl = safRegAddrService.getAllUrl();
			if(!newUrl.equals(directUrl)){
				directUrl = newUrl;
				reRefer();
			}
			for(JsfRegAddr addr : addrs){
				try {
					if(isAlive(addr)){
						addr.setState(1);
						RpcContext.getContext().setAttachment(Constants.HIDDEN_KEY_PINPOINT, addr.getIp()+":"+addr.getPort());
						String result = (String) genericService.$invoke("stat", null, null);
						safRegistryService.saveRegistryMonit(result, addr, date);
					}else {
						addr.setState(0);
						safRegAddrService.updateStatus(addr);
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
			
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}
		
	}
	
	private boolean isAlive(JsfRegAddr addr){
		boolean alive = false;
		try {
			alive = doCheck(addr.getIp(), addr.getPort());
			if(!alive){
				for(; error < 4; ){
					alive = doCheck(addr.getIp(), addr.getPort());
					if(alive){
						return true;
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
					}
				}
			}
			return alive;
		}finally{
			error = 1;
		}
	}
	
	private boolean doCheck(String ip, int port){
        SafTelnetClient client = null;
        try {
            client = new SafTelnetClient(ip, port, 5000, 5000);
            if (client.isConnected()) {
                String string = client.send("ls");
                // TODO
                return true;
            } else {
                Object[] objects = new Object[] { error, ip, port };
                logger.error("第{}次连接{}:{}不通!", objects);
                error++;
                return false;
            }
        } catch (Exception e) {
            Object[] objects = new Object[] { error, ip, port };
            logger.error("第{}次连接{}:{}不通!", objects);
            error++;
            return false;
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (Exception e) {
                }
            }
        }
	}

	@Override
	public String getWorkerType() {
		return "registryStatusWorker";
	}

}
