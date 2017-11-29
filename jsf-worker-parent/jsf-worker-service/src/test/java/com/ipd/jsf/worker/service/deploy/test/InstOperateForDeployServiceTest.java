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
package com.ipd.jsf.worker.service.deploy.test;

import org.junit.Test;

import com.ipd.jsf.deploy.app.InstOperateForDeployService;
import com.ipd.jsf.deploy.app.domain.DeployRequest;
import com.ipd.jsf.gd.config.ConsumerConfig;

public class InstOperateForDeployServiceTest {
	int pid = 15736;
	@Test
	private void testOnOffline() {
		try {

	        ConsumerConfig<InstOperateForDeployService> consumerConfig = new ConsumerConfig<InstOperateForDeployService>();
	        consumerConfig.setInterfaceId(InstOperateForDeployService.class.getName());
	        consumerConfig.setProtocol("jsf");
	        consumerConfig.setAlias("jsf-deploy");
	        consumerConfig.setRegister(false);
	        consumerConfig.setUrl("jsf://10.12.166.26:22000");
//	        consumerConfig.setUrl("jsf://192.168.150.121:22001");
	        consumerConfig.setParameter(".token", "deploy_call");
	        InstOperateForDeployService service = consumerConfig.refer();

	        DeployRequest request = new DeployRequest();
	        request.setAppId(600140);
	        request.setAppInsId("610141");
	        request.setPid(pid);
	        request.setToken("1234567");

	        service.doInsOffline(request);
	        Thread.sleep(10000);
	        service.doInsOnline(request);
	        System.out.println("下线完成！");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	private void testCheckInvokeTimes() {
		try {
	        ConsumerConfig<InstOperateForDeployService> consumerConfig = new ConsumerConfig<InstOperateForDeployService>();
	        consumerConfig.setInterfaceId(InstOperateForDeployService.class.getName());
	        consumerConfig.setProtocol("jsf");
	        consumerConfig.setAlias("jsf-deploy");
	        consumerConfig.setRegister(false);
	        consumerConfig.setUrl("jsf://10.12.166.26:22000");
	        consumerConfig.setParameter(".token", "deploy_call");
	        InstOperateForDeployService service = consumerConfig.refer();

	        DeployRequest request = new DeployRequest();
	        request.setAppId(600140);
	        request.setAppInsId("610141");
	        request.setPid(pid);
	        request.setToken("1234567");
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        service.doInsOnline(request);
	        service.doInsOffline(request);
	        System.out.println("上线完成！");
	        Thread.sleep(100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	private void testCheckToken() {
		try {
	        ConsumerConfig<InstOperateForDeployService> consumerConfig = new ConsumerConfig<InstOperateForDeployService>();
	        consumerConfig.setInterfaceId(InstOperateForDeployService.class.getName());
	        consumerConfig.setProtocol("jsf");
	        consumerConfig.setAlias("jsf-deploy");
	        consumerConfig.setRegister(false);
	        consumerConfig.setUrl("jsf://10.12.166.26:22000");
	        consumerConfig.setParameter(".token", "deploy_call");
	        InstOperateForDeployService service = consumerConfig.refer();

	        DeployRequest request = new DeployRequest();
	        request.setAppId(600140);
	        request.setAppInsId("610141");
	        request.setPid(pid);
	        request.setToken("11111");
	        service.doInsOnline(request);
	        System.out.println("上线完成！");
	        Thread.sleep(100000);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		InstOperateForDeployServiceTest test = new InstOperateForDeployServiceTest();
		test.testOnOffline();
	}

}
