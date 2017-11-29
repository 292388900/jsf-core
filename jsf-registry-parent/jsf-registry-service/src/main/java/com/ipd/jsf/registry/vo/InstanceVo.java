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
package com.ipd.jsf.registry.vo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.registry.callback.SubscribeCallback;
import com.ipd.jsf.registry.domain.AliasProtocolVo;
import com.ipd.jsf.registry.util.RegistryUtil;
import com.ipd.jsf.vo.SubscribeUrl;

public class InstanceVo {
    private float loadFactor = 0.75f;
    /*实例key*/
    private String insKey;

    //appId
    private int appId;

    /*callback回调对象*/
    private SubscribeCallback<SubscribeUrl> callback;

    /*保存接口对应的分组和协议  key= interfaceName, value= AliasProtocolVo*/
    private ConcurrentHashMap<String, Set<AliasProtocolVo>> ifaceAliasProtocolMap = new ConcurrentHashMap<String, Set<AliasProtocolVo>>(1, loadFactor, 1);

    /**
     * @return the insKey
     */
    public String getInsKey() {
        return insKey;
    }

    /**
     * @param insKey the insKey to set
     */
    public void setInsKey(String insKey) {
        this.insKey = insKey;
    }

    /**
     * @return the appId
     */
    public int getAppId() {
        return appId;
    }

    /**
     * @param appId the appId to set
     */
    public void setAppId(int appId) {
        this.appId = appId;
    }

    /**
     */
    public SubscribeCallback<SubscribeUrl> getCallback() {
        return callback;
    }

    /**
     * @param callback the callback to set
     */
    public void setCallback(SubscribeCallback<SubscribeUrl> callback) {
        if (this.callback != callback) {
            this.callback = callback;
        }
    }

    /**
     * @return the ifaceAliasProtocolMap
     */
    public ConcurrentHashMap<String, Set<AliasProtocolVo>> getIfaceAliasProtocolMap() {
        return ifaceAliasProtocolMap;
    }

    /**
     * @param ifaceAliasProtocolMap the ifaceAliasProtocolMap to set
     */
    public void setIfaceAliasProtocolMap(ConcurrentHashMap<String, Set<AliasProtocolVo>> ifaceAliasProtocolMap) {
        this.ifaceAliasProtocolMap = ifaceAliasProtocolMap;
    }

    /**
     * 将AliasProtocolVo转为string
     * @return
     */
    public Map<String, Set<String>> getIfaceAliasProtocolStringMap() {
        Map<String, Set<String>> result = new HashMap<String, Set<String>>();
        if (!ifaceAliasProtocolMap.isEmpty()) {
            for (Entry<String, Set<AliasProtocolVo>> entry : ifaceAliasProtocolMap.entrySet()) {
                if (result.get(entry.getKey()) == null) {
                    result.put(entry.getKey(), new HashSet<String>());
                }
                for (AliasProtocolVo vo : entry.getValue()) {
                    result.get(entry.getKey()).add(RegistryUtil.getAliasProtocolKey(vo.getAlias(), vo.getProtocol()));
                }
            }
        }
        
        return result;
    }
}
