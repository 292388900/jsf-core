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
import com.ipd.jsf.registry.berkeley.domain.BdbClientList;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.ipd.jsf.registry.domain.Client;
import com.ipd.jsf.registry.domain.JsfIns;

@Repository
public class ClientListLocalDao {
	private final Logger logger = LoggerFactory.getLogger(ClientListLocalDao.class);
    //client 全局的计数器。最为berkeleydb的key，用来做先后循序的比对。
    private AtomicLong timeCount = new AtomicLong(System.nanoTime());

	@Autowired
	private BerkeleyDb berkeleyDb;

	private JSFMsgPack jmp = new JSFMsgPack();
	
	private static final String DBNAME_CLIENT = "clientlist";
	
	/**
	 * 注册服务，把client信息存储到本地DB
	 * @param clients
	 * @throws Exception
	 */
	public void registry(List<Client> clients, JsfIns ins) throws Exception {
		long updateTime = timeCount.getAndIncrement();
		BdbClientList bdbClients = new BdbClientList();
		bdbClients.setKey(RegistryConstants.PREFIX_REGISTRY + "_" + String.valueOf(updateTime));
		bdbClients.setClients(clients);
		bdbClients.setRegistry(true);
		bdbClients.setIns(ins);
		byte data[] = jmp.write(bdbClients);
        this.berkeleyDb.put(DBNAME_CLIENT, bdbClients.getKey(), data);
	}

	/**
	 * 注册服务，把client信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void registry(Client client, JsfIns ins) throws Exception {
		List<Client> list = new ArrayList<Client>();
		list.add(client);
		registry(list, ins);
	}

	/**
	 * 注销服务，把client信息存储到本地DB
	 * @param clients
	 * @throws Exception
	 */
	public void unRegistry(List<Client> clients, JsfIns ins) throws Exception{
		long updateTime = timeCount.getAndIncrement();
		BdbClientList bdbClients = new BdbClientList();
		bdbClients.setKey(RegistryConstants.PREFIX_UNREGISTRY + "_" + String.valueOf(updateTime));
		bdbClients.setClients(clients);
		bdbClients.setRegistry(false);
		bdbClients.setIns(ins);
		byte data[] = jmp.write(bdbClients);
		this.berkeleyDb.put(DBNAME_CLIENT, bdbClients.getKey(), data);
	}

	/**
	 * 注销服务，把client信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void unRegistry(Client client, JsfIns ins) throws Exception {
		List<Client> list = new ArrayList<Client>();
		list.add(client);
		unRegistry(list, ins);
	}

	/**
	 * 读取数据，从本地DB中取得当前所有client信息
	 * @return
	 * @throws Exception
	 */
	public List<BdbClientList> getAll() throws Exception {
		List<BerkeleyBean> dataList = berkeleyDb.get(DBNAME_CLIENT, null);
		if (dataList == null || dataList.isEmpty()) {
			return null;
		}
		List<BdbClientList> bdbClientListArray = new ArrayList<BdbClientList>(dataList.size());
		BdbClientList bdbClientList = null;
		List<String> removeKeys = new ArrayList<String>();
		for (BerkeleyBean bean : dataList) {
			try {
				bdbClientList = jmp.read(bean.getValue(), BdbClientList.class);
				bdbClientListArray.add(bdbClientList);
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
		return bdbClientListArray;
	}

	/**
	 * 根据key值，删除信息
	 * @param key
	 * @throws Exception
	 */
	public void delete(String key) throws Exception{
		this.berkeleyDb.delete(DBNAME_CLIENT, key);
	}

	public void flush(){
		this.berkeleyDb.flush();
	}

	public int getTotalCount() throws Exception {
        return this.berkeleyDb.getTotalCount(DBNAME_CLIENT);
	}
}
