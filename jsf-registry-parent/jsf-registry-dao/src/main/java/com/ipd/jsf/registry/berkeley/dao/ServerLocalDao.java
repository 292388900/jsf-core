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
package com.ipd.jsf.registry.berkeley.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.gd.codec.msgpack.JSFMsgPack;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.BdbServer;
import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.ipd.jsf.registry.domain.Server;

@Repository
public class ServerLocalDao {
    private static final Logger logger = LoggerFactory.getLogger(ServerLocalDao.class);
    //server 全局的计数器。最为berkeleydb的key，用来做先后循序的比对。
    private AtomicLong timeCount = new AtomicLong(System.nanoTime());

    @Autowired
    private BerkeleyDb berkeleyDb;

    private JSFMsgPack jmp = new JSFMsgPack();

    private static final String DBNAME_SERVER = "server";

    public void setBerkeleyDb(BerkeleyDb berkeleyDb) {
        this.berkeleyDb = berkeleyDb;
    }

	/**
	 * 注册服务，把server信息存储到本地DB
	 * @param server
	 * @throws Exception
	 */
	public void registry(Server server) throws Exception{
//		String key = UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId());
		byte data[] = jmp.write(server);
		long updateTime = timeCount.getAndIncrement();
		long start = System.currentTimeMillis();
        this.berkeleyDb.put(DBNAME_SERVER, RegistryConstants.PREFIX_REGISTRY + "_" + String.valueOf(updateTime), data);
        logger.info(RegistryConstants.PREFIX_REGISTRY + "_" + updateTime + ", server uniqkey:" + server.getUniqKey() + ", bdb put total elapse:" + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * 注销服务，把server信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void unRegistry(Server server) throws Exception{
//		String key = UniqkeyUtil.getServerUniqueKey(server.getIp(), server.getPort(), server.getAlias(), server.getProtocol(), server.getInterfaceId());
		byte data[] = jmp.write(server);
		long updateTime = timeCount.getAndIncrement();
		logger.info(RegistryConstants.PREFIX_UNREGISTRY + "_" + updateTime + ", server uniqkey" + server.getUniqKey());
        this.berkeleyDb.put(DBNAME_SERVER, RegistryConstants.PREFIX_UNREGISTRY + "_" + String.valueOf(updateTime), data);
	}

	/**
	 * 读取数据，从本地DB中取得当前所有server信息
	 * @return
	 * @throws Exception
	 */
	public List<BdbServer> getAll() throws Exception{
		List<BerkeleyBean> dataList = berkeleyDb.get(DBNAME_SERVER, null);
        if (dataList == null || dataList.isEmpty()) {
			return null;
		}
		List<BdbServer> serverList = new ArrayList<BdbServer>(dataList.size());
		Server server = null;
		BdbServer bdbServer = null;
		for(BerkeleyBean bean : dataList){
			server = jmp.read(bean.getValue(), Server.class);
			bdbServer = new BdbServer();
			bdbServer.setKey(bean.getKey());
			bdbServer.setServer(server);
			if (bean.getKey().startsWith(RegistryConstants.PREFIX_REGISTRY)) {
			    bdbServer.setRegistry(true);
			} else {
			    bdbServer.setRegistry(false);
			}
			serverList.add(bdbServer);
		}
		return serverList;
	}
	
	/**
	 * 根据key值，删除信息
	 * @param key
	 * @throws Exception
	 */
	public void delete(String key) throws Exception{
		this.berkeleyDb.delete(DBNAME_SERVER, key);
	}

    public int getTotalCount() throws Exception {
        return this.berkeleyDb.getTotalCount(DBNAME_SERVER);
    }

	public void flush(){
		this.berkeleyDb.flush();
	}

}
