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
import com.ipd.jsf.registry.berkeley.domain.BdbServerList;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.domain.Server;

@Repository
public class ServerListLocalDao {
    private final Logger logger = LoggerFactory.getLogger(ServerListLocalDao.class);
    //server 全局的计数器。最为berkeleydb的key，用来做先后循序的比对。
    private AtomicLong timeCount = new AtomicLong(System.nanoTime());

    @Autowired
    private BerkeleyDb berkeleyDb;

    private JSFMsgPack jmp = new JSFMsgPack();

    private static final String DBNAME_SERVER = "serverlist";

    public void setBerkeleyDb(BerkeleyDb berkeleyDb) {
        this.berkeleyDb = berkeleyDb;
    }

	/**
	 * 注册服务，把server信息存储到本地DB
	 * @param server
	 * @throws Exception
	 */
	public void registry(List<Server> servers, JsfIns ins) throws Exception{
		long updateTime = timeCount.getAndIncrement();
		BdbServerList bdbServers = new BdbServerList();
		bdbServers.setKey(RegistryConstants.PREFIX_REGISTRY + "_" + String.valueOf(updateTime));
		bdbServers.setServers(servers);
		bdbServers.setRegistry(true);
		bdbServers.setIns(ins);
		byte data[] = jmp.write(bdbServers);
//		long start = System.currentTimeMillis();
        this.berkeleyDb.put(DBNAME_SERVER, bdbServers.getKey(), data);
//        logger.info(RegistryConstants.PREFIX_REGISTRY + "_" + updateTime + ", servers uniqkey:" + getUniqKeys(servers) + ", bdb put total elapse:" + (System.currentTimeMillis() - start) + "ms");
	}

	/**
	 * 注册服务，把server信息存储到本地DB
	 * @param server
	 * @throws Exception
	 */
	public void registry(Server server, JsfIns ins) throws Exception {
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server);
		registry(serverList, ins);
	}

	/**
	 * 注销服务，把server信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void unRegistry(List<Server> servers, JsfIns ins) throws Exception{
		long updateTime = timeCount.getAndIncrement();
		BdbServerList bdbServers = new BdbServerList();
		bdbServers.setKey(RegistryConstants.PREFIX_UNREGISTRY + "_" + String.valueOf(updateTime));
		bdbServers.setServers(servers);
		bdbServers.setRegistry(false);
		bdbServers.setIns(ins);
		byte data[] = jmp.write(bdbServers);
        this.berkeleyDb.put(DBNAME_SERVER, bdbServers.getKey(), data);
//        logger.info(RegistryConstants.PREFIX_UNREGISTRY + "_" + updateTime + ", servers uniqkey" + getUniqKeys(servers));
	}

	/**
	 * 注册服务，把server信息存储到本地DB
	 * @param server
	 * @throws Exception
	 */
	public void unRegistry(Server server, JsfIns ins) throws Exception{
		List<Server> serverList = new ArrayList<Server>();
		serverList.add(server);
		unRegistry(serverList, ins);
	}

	/**
	 * 读取数据，从本地DB中取得当前所有server信息
	 * @return
	 * @throws Exception
	 */
	public List<BdbServerList> getAll() throws Exception {
		List<BerkeleyBean> dataList = berkeleyDb.get(DBNAME_SERVER, null);
		if (dataList == null || dataList.isEmpty()) {
			return null;
		}
		List<BdbServerList> bdbServerListArray = new ArrayList<BdbServerList>(dataList.size());
		BdbServerList bdbServerList = null;
		List<String> removeKeys = new ArrayList<String>();
		for (BerkeleyBean bean : dataList) {
			try {
				bdbServerList = jmp.read(bean.getValue(), BdbServerList.class);
				bdbServerListArray.add(bdbServerList);
			} catch (Exception e) {
				//序列化有异常就记录下来,然后从bdb删除key
				removeKeys.add(bean.getKey());
				logger.error(e.getMessage(), e);
			}
		}
		if (!removeKeys.isEmpty()) {
			try {
				for (String key : removeKeys) {
					delete(key);
				}
				flush();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return bdbServerListArray;
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
//
//	private String getUniqKeys(List<Server> servers) {
//		StringBuilder sb = new StringBuilder();
//		if (!CollectionUtils.isEmpty(servers)) {
//			for (Server server : servers) {
//				sb.append(server.getUniqKey()).append(",");
//			}
//		}
//		return sb.toString();
//	}
}
