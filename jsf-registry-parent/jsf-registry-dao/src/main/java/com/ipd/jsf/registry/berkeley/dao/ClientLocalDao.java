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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ipd.jsf.gd.codec.msgpack.JSFMsgPack;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.BdbClient;
import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.ipd.jsf.registry.domain.Client;

@Repository
public class ClientLocalDao {
    //client 全局的计数器。最为berkeleydb的key，用来做先后循序的比对。
    private AtomicLong timeCount = new AtomicLong(System.nanoTime());

	@Autowired
	private BerkeleyDb berkeleyDb;
	
	private JSFMsgPack jmp = new JSFMsgPack();
	
	private static final String DBNAME_CLIENT = "client";
	
	/**
	 * 注册服务，把client信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void registry(Client client) throws Exception{
//		String key = UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), client.getAlias(), client.getProtocol(), client.getInterfaceId());
		byte data[] = jmp.write(client);
		long updateTime = timeCount.getAndIncrement();
        this.berkeleyDb.put(DBNAME_CLIENT, RegistryConstants.PREFIX_REGISTRY + "_" + String.valueOf(updateTime), data);
	}

	/**
	 * 注销服务，把client信息存储到本地DB
	 * @param client
	 * @throws Exception
	 */
	public void unRegistry(Client client) throws Exception{
//		String key = UniqkeyUtil.getClientUniqueKey(client.getIp(), client.getPid(), client.getAlias(), client.getProtocol(), client.getInterfaceId());
		byte data[] = jmp.write(client);
		long updateTime = timeCount.getAndIncrement();
		this.berkeleyDb.put(DBNAME_CLIENT, RegistryConstants.PREFIX_UNREGISTRY + "_" + String.valueOf(updateTime), data);
	}

	/**
	 * 读取数据，从本地DB中取得当前所有client信息
	 * @return
	 * @throws Exception
	 */
	public List<BdbClient> getAll() throws Exception{
		List<BerkeleyBean> dataList = berkeleyDb.get(DBNAME_CLIENT, null);
		if(dataList == null || dataList.isEmpty()){
			return null;
		}
		List<BdbClient> clientList = new ArrayList<BdbClient>(dataList.size());
		Client client = null;
		for(BerkeleyBean bean : dataList){
			client = jmp.read(bean.getValue(), Client.class);
			BdbClient bdbClient = new BdbClient();
            bdbClient.setKey(bean.getKey());
            bdbClient.setClient(client);
            if (bean.getKey().startsWith(RegistryConstants.PREFIX_REGISTRY)) {
                bdbClient.setRegistry(true);
            } else {
                bdbClient.setRegistry(false);
            }
            clientList.add(bdbClient);
		}
		return clientList;
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
