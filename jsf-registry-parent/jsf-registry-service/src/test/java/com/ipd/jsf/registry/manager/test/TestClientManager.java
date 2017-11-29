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
package com.ipd.jsf.registry.manager.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.manager.ClientManager;
import com.ipd.jsf.registry.manager.InterfaceManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;
import com.ipd.jsf.registry.util.RegistryUtil;

public class TestClientManager extends ServiceBaseTest {
    private static String ip = "127.0.0.1";
    private static String appPath = "/abc/zxyb111111" + System.currentTimeMillis();

    @Autowired
    ClientManager clientManager;
    
    @Autowired
    InterfaceManager interfaceInfoManager;

    private ExecutorService threadPool = Executors.newFixedThreadPool(50);

    @Test
    public void newClient() throws Exception {
        try {
            List<InterfaceInfo> ifaces = interfaceInfoManager.getListForProvider();
            List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
            for (InterfaceInfo iface : ifaces) {
                for (int i = 0; i < 1; i++) {
//                    ip = "127.0."+String.valueOf((new Random().nextInt(255)))+"." + String.valueOf((new Random().nextInt(255)));
                    ip = "127.0.149.32";
                    final Client client = new Client();
                    client.setAppPath(appPath);
                    client.setIp(ip);
                    client.setSafVer(200);
                    client.setPid(621);
                    client.setStatus(InstanceStatus.onlineButNotWork.value());
                    client.setInterfaceId(iface.getInterfaceId());
                    client.setInterfaceName(iface.getInterfaceName());
                    client.setSrcType(SourceType.zookeeper.value());
                    client.setStartTime(RegistryUtil.getSystemCurrentTime());
                    client.setAlias("saf");
                    
                    String appPath = "";
                    for (int m = 0; m < 200; m++) {
                       appPath += "12345";
                    }
                    client.setAppPath(appPath);
                    
                    String urlDesc = "";
                    for (int m = 0; m < 200; m++) {
                        urlDesc += "12345";
                    }
                    client.setUrlDesc(urlDesc);
                    Future<Boolean> f = threadPool.submit(new Callable<Boolean>() {
                        
                        @Override
                        public Boolean call() throws Exception {
                            try {
                            	List<Client> list = new ArrayList<Client>();
                            	list.add(client);
                                clientManager.registerClient(list);
                                 } catch(Exception e) {
                                    e.printStackTrace();
                                }
                            return true;
                        }
                    });
                    tasks.add(f);
                }
                break;
            }
            for (Future<Boolean> future : tasks) {
                try {
                    future.get();
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
