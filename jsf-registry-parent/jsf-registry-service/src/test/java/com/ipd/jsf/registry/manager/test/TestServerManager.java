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
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.manager.InterfaceManager;
import com.ipd.jsf.registry.manager.ServerManager;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;
import com.ipd.jsf.registry.util.RegistryUtil;

public class TestServerManager extends ServiceBaseTest {

    static int serverId = 26;
    static String ip = "127.0.2." + String.valueOf((new Random().nextInt(255)));
    static int port = (new Random().nextInt(10000));

    @Autowired
    ServerManager serverManager;
    
    @Autowired
    InterfaceManager interfaceInfoManager;

    ExecutorService threadPool = Executors.newFixedThreadPool(50);

    @Test
    public void registerServer() {
        try {
            	List<InterfaceInfo> ifaces = interfaceInfoManager.getListForProvider();
            	List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();
            	for (InterfaceInfo iface : ifaces) {
            		for (int i = 0; i < 1; i++) {
            			ip = "127.0."+String.valueOf((new Random().nextInt(255)))+"." + String.valueOf((new Random().nextInt(255)));
            			port = (new Random().nextInt(10000));
            			final Server server = new Server();
            			server.setInterfaceId(iface.getInterfaceId());
            			server.setInterfaceName(iface.getInterfaceName());
            			server.setIp(ip);
            			server.setPort(port);
            			server.setPid(12345);
            			server.setAlias("saf");
            			server.setStatus(InstanceStatus.offline.value());
            			server.setRoom(ComputerRoom.YiZhuang.value());
            			server.setTimeout(3000);
            			server.setWeight(2);
             	        server.setAppPath("/abc/zxy555555555555");
             	        server.setProtocol(ProtocolType.dubbo.value());
             	        server.setContextPath("/asdf/asdf/asdf");
             	        server.setSafVer(210);
             	        server.setRandom(false);
             	        server.setSrcType(SourceType.registry.value());
             	        server.setStartTime(RegistryUtil.getSystemCurrentTime());

             	        String appPath = "";
             	        for (int m = 0; m < 200; m++) {
             	           appPath += "12345";
             	        }
             	        server.setAppPath(appPath);

             	        String attr = "";
             	        for (int m = 0; m < 200; m++) {
             	            attr += "12345";
             	        }
             	        server.setAttrUrl(attr);

             	        String urlDesc = "";
             	        for (int m = 0; m < 200; m++) {
             	            urlDesc += "12345";
             	        }
             	        server.setUrlDesc(urlDesc);

         	        	Future<Boolean> f = threadPool.submit(new Callable<Boolean>() {
    
        					@Override
        					public Boolean call() throws Exception {
        						try {
        							List<Server> serverlist = new ArrayList<Server>();
        							serverlist.add(server);
        							List<Integer> list = new ArrayList<Integer>();
        							list.add(1);
        							serverManager.registerServer(serverlist, list);
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
