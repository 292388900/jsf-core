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
package com.ipd.jsf.registry.dao.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.alibaba.fastjson.JSON;
import com.ipd.jsf.common.enumtype.ComputerRoom;
import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.enumtype.SourceType;
import com.ipd.jsf.common.util.UniqkeyUtil;
import com.ipd.jsf.gd.util.Constants.ProtocolType;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.dao.ServerAliasDao;
import com.ipd.jsf.registry.dao.ServerDao;
import com.ipd.jsf.registry.domain.Server;
import com.ipd.jsf.registry.domain.ServerAlias;
import com.ipd.jsf.registry.service.test.ServiceBaseTest;
import com.ipd.jsf.registry.util.RegistryUtil;

public class TestServerDao extends ServiceBaseTest {

    @Autowired
    private ServerDao serverDao;
    
    @Autowired
    private ServerAliasDao serverAliasDao;

//    @Test
    public void create() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RegistryConstants.LANGUAGE, "java");
        map.put(RegistryConstants.SAFVERSION, "210");
        map.put(RegistryConstants.WEIGHT, "300");
        final Server server = new Server();
        server.setInterfaceId(2073);
        server.setInterfaceName("com.ipd.saf.service.RegistryService");
        server.setIp("10.12.107.84");
        server.setPort(40990);
        server.setPid(5555);
        server.setAlias("test");
        server.setStatus(InstanceStatus.online.value());
        server.setRoom(ComputerRoom.YiZhuang.value());
        server.setTimeout(1000);
        server.setWeight(100);
        server.setAppPath("/abc/zxy");
        server.setProtocol(ProtocolType.jsf.value());
        server.setContextPath("/asdf/asdf/asdf");
        server.setSafVer(210);
        server.setRandom(false);
        server.setSrcType(SourceType.registry.value());
        server.setStartTime(RegistryUtil.getSystemCurrentTime());
        server.setUniqKey(UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId()));
        server.setAttrUrl(JSON.toJSONString(RegistryUtil.copyEntries(
                map,
                false,
                Arrays.asList(new String[] {
                        RegistryConstants.APPPATH,
                        RegistryConstants.SAFVERSION,
                        RegistryConstants.WEIGHT }))));
        try {
            serverDao.create(server);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    

//    @Test
    public void create1() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(RegistryConstants.LANGUAGE, "java");
        map.put(RegistryConstants.SAFVERSION, "210");
        map.put(RegistryConstants.WEIGHT, "300");
        final Server server = new Server();
        server.setInterfaceId(2073);
        server.setInterfaceName("com.ipd.saf.service.RegistryService");
        server.setIp("10.12.107.84");
        server.setPort(40990);
        server.setPid(5555);
        server.setAlias("test");
        server.setStatus(InstanceStatus.online.value());
        server.setRoom(ComputerRoom.YiZhuang.value());
        server.setTimeout(1000);
        server.setWeight(100);
        server.setAppPath("/abc/zxy");
        server.setProtocol(ProtocolType.jsf.value());
        server.setContextPath("/asdf/asdf/asdf");
        server.setSafVer(210);
        server.setRandom(false);
        server.setSrcType(SourceType.registry.value());
        server.setStartTime(RegistryUtil.getSystemCurrentTime());
        server.setUniqKey(UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId()));
        String attr = "";
        for (int i = 0; i < 200; i++) {
            attr += "12345";
        }
        server.setAttrUrl(attr);
        String urlDesc = "";
        for (int i = 0; i < 200; i++) {
            urlDesc += "12345";
        }
        server.setUrlDesc(urlDesc);
        try {
            serverDao.create(server);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void getAliasServer() {
        List<ServerAlias> list;
        try {
            List<Integer> interfaceIdList = new ArrayList<Integer>();
            interfaceIdList.add(1025);
            list = serverAliasDao.getAliasServerByInterfaceIdList(interfaceIdList);
            for (ServerAlias server : list) {
                System.out.println("alias:" + server.getTargetAlias() + ", server id: " + server.getId());
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
