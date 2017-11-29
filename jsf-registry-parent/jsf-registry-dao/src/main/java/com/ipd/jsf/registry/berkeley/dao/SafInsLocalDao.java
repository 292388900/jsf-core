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
import com.ipd.jsf.registry.berkeley.domain.BdbJsfIns;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.BerkeleyBean;
import com.ipd.jsf.registry.domain.JsfIns;

@Repository
public class SafInsLocalDao {
	private final Logger logger = LoggerFactory.getLogger(SafInsLocalDao.class);
    //instance 全局的计数器。最为berkeleydb的key，用来做先后循序的比对。
    private AtomicLong timeCount = new AtomicLong(System.nanoTime());

	@Autowired
	private BerkeleyDb berkeleyDb;
	
	private JSFMsgPack jmp = new JSFMsgPack();
	
	private static final String DBNAME_SAFINS = "jsfIns";

	public void setBerkeleyDb(BerkeleyDb berkeleyDb) {
		this.berkeleyDb = berkeleyDb;
	}

	/**
	 * 注册服务，把jsfIns实例信息存储到本地DB
	 * @param jsfIns
	 * @throws Exception
	 */
	public void registry(JsfIns jsfIns) throws Exception{
//		String key = UniqkeyUtil.getInsKey(jsfIns.getIp(), jsfIns.getPid(), jsfIns.getStartTime());
//		long updateTime = timeCount.getAndIncrement();
		BdbJsfIns bdbJsfIns = new BdbJsfIns();
		bdbJsfIns.setIns(jsfIns);
//		bdbJsfIns.setKey(RegistryConstants.PREFIX_REGISTRY + "_" + String.valueOf(updateTime));
		bdbJsfIns.setKey(RegistryConstants.PREFIX_REGISTRY + "_" + jsfIns.getInsKey());
		bdbJsfIns.setRegistry(true);
		byte data[] = jmp.write(bdbJsfIns);
        this.berkeleyDb.put(DBNAME_SAFINS, bdbJsfIns.getKey(), data);
	}

	/**
	 * 读取数据，从本地DB中取得当前所有server实例信息
	 * @return
	 * @throws Exception
	 */
    public List<BdbJsfIns> getAll() throws Exception {
        List<BerkeleyBean> dataList = berkeleyDb.get(DBNAME_SAFINS, null);
        if (dataList == null || dataList.isEmpty()) {
            return null;
        }
        List<BdbJsfIns> bdbJsfInsList = new ArrayList<BdbJsfIns>(dataList.size());
        BdbJsfIns bdbSafIns = null;
        List<String> removeKeys = new ArrayList<String>();
        for (BerkeleyBean bean : dataList) {
        	try {
        		bdbSafIns = jmp.read(bean.getValue(), BdbJsfIns.class);
        		bdbJsfInsList.add(bdbSafIns);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				removeKeys.add(bean.getKey());
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
        return bdbJsfInsList;
    }

	/**
	 * 根据key值，删除信息
	 * @param key
	 * @throws Exception
	 */
	public void delete(String key) throws Exception {
		this.berkeleyDb.delete(DBNAME_SAFINS, key);
	}

    public int getTotalCount() throws Exception {
        return this.berkeleyDb.getTotalCount(DBNAME_SAFINS);
    }

	public void flush(){
		this.berkeleyDb.flush();
	}

}
