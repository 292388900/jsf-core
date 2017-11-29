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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ipd.jsf.gd.util.ConcurrentHashSet;
import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.registry.common.RegistryConstants;
import com.ipd.jsf.registry.domain.InterfaceInfo;
import com.ipd.jsf.registry.domain.Server;

/**
 * InterfaceVo缓存对象
 */
public class InterfaceVo {
    private float loadFactor = 0.75f;
    //接口id
    private int interfaceId;

    //接口名
    private String interfaceName;

    //接口创建时间
    private long createTime;

    //  provider 更新版本号
    private long updateTime;

    //  配置更新版本号
    private long configUpdateTime;

    //同机房优先策略, 0-无策略，1-只传同机房provider，2-同机房放大权重
    public byte roomStrategy = DataEnum.RoomStrategyType.noStrategy.getValue();

    //上次加载server数据的时间
    public transient volatile long serverLoadLastTime = RegistryConstants.ORIGINAL_TIME;

    //推送provider列表数量限制
    public volatile int providerLimit = -1;

    //server缓存列表, key:serverId, value:server
    public ConcurrentHashMap<Integer, Server> serverMap = new ConcurrentHashMap<Integer, Server>(4, loadFactor, 4);

    //分组缓存map，存储每个组的额外关联server  key:alias, value: serverId list
    public ConcurrentHashMap<String, List<Integer>> aliasServerMap = new ConcurrentHashMap<String, List<Integer>>(1, loadFactor, 1);

    //接口对应的insKey列表
    private ConcurrentHashSet<String> insKeySet = new ConcurrentHashSet<String>();

    //接口配置信息缓存   key: 参数key,  value: 参数value
    public ConcurrentHashMap<String, String> propertyMap = new ConcurrentHashMap<String, String>(1, loadFactor, 1);

    //ip路由规则关系映射 map :{client ip expression or paramter:server id expression }
    private ConcurrentHashMap<String, List<String>> ipRouterMap = null;
    //参数路由规则关系映射 map :{client ip expression or paramter:server id expression }
    private ConcurrentHashMap<String, List<String>> paramRouterMap = null;
    //mock参数map, key1:ip, key2:参数名
    private ConcurrentHashMap<String, Map<String, String>> mockMap = null;

    public InterfaceVo() {
    }

    public InterfaceVo(InterfaceInfo iface) {
        setIface(iface);
    }

    public void setIface(InterfaceInfo iface) {
        if (iface != null) {
            this.interfaceId = iface.getInterfaceId();
            this.interfaceName = iface.getInterfaceName();
            if (iface.getCreateTime() != null) {
                this.createTime = iface.getCreateTime().getTime();
            }
            if (iface.getUpdateTime() != null) {
                this.updateTime = iface.getUpdateTime().getTime();
            }
            if (iface.getConfigUpdateTime() != null) {
                this.configUpdateTime = iface.getConfigUpdateTime().getTime();
            }
        }
    }

    /**
     * @return the interfaceId
     */
    public int getInterfaceId() {
        return interfaceId;
    }

    /**
     * @param interfaceId the interfaceId to set
     */
    public void setInterfaceId(int interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * @return the interfaceName
     */
    public String getInterfaceName() {
        return interfaceName;
    }

    /**
     * @param interfaceName the interfaceName to set
     */
    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    /**
     * @return the createTime
     */
    public long getCreateTime() {
        return createTime;
    }

    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    /**
     * @return the updateTime
     */
    public long getUpdateTime() {
        return updateTime;
    }

    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * @return the configUpdateTime
     */
    public long getConfigUpdateTime() {
        return configUpdateTime;
    }

    /**
     * @param configUpdateTime the configUpdateTime to set
     */
    public void setConfigUpdateTime(long configUpdateTime) {
        this.configUpdateTime = configUpdateTime;
    }

    /**
     * @return the insKeySet
     */
    public ConcurrentHashSet<String> getInsKeySet() {
        return insKeySet;
    }

    /**
     * @param insKeySet the insKeySet to set
     */
    public void setInsKeySet(ConcurrentHashSet<String> insKeySet) {
        this.insKeySet = insKeySet;
    }

    /**
     * clone ipRouterMap
     * @return
     */
    public Map<String, List<String>> cloneIpRouterMap() {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (ipRouterMap != null) {
            map.putAll(ipRouterMap);
        }
        return map;
    }

    /**
     * 赋值
     * @param map
     */
    public void putAllIpRouterMap(Map<String, List<String>> map) {
        if (map != null && !map.isEmpty()) {
            if (ipRouterMap == null) {
                ipRouterMap = new ConcurrentHashMap<String, List<String>>(1, loadFactor, 1);
            }
            ipRouterMap.putAll(map);
        }
    }

    /**
     * 清空
     */
    public void clearIpRouterMap() {
        if (ipRouterMap != null) {
            ipRouterMap.clear();
        }
    }

    /**
     * 销毁
     */
    public void destroyIpRouterMap() {
        ipRouterMap = null;
    }


    /**
     * clone paramRouterMap
     * @return
     */
    public Map<String, List<String>> cloneParamRouterMap() {
        Map<String, List<String>> map = new HashMap<String, List<String>>();
        if (paramRouterMap != null) {
            map.putAll(paramRouterMap);
        }
        return map;
    }

    /**
     * 赋值
     * @param map
     */
    public void putAllParamRouterMap(Map<String, List<String>> map) {
        if (map != null && !map.isEmpty()) {
            if (paramRouterMap == null) {
                paramRouterMap = new ConcurrentHashMap<String, List<String>>(1, loadFactor, 1);
            }
            paramRouterMap.putAll(map);
        }
    }

    /**
     * 清空
     */
    public void clearParamRouterMap() {
        if (paramRouterMap != null) {
            paramRouterMap.clear();
        }
    }

    /**
     * 销毁
     */
    public void destroyParamRouterMap() {
        paramRouterMap = null;
    }

    /**
     * clone paramRouterMap
     * @return
     */
    public Map<String, Map<String, String>> cloneMockMap() {
        Map<String, Map<String, String>> map = new HashMap<String, Map<String, String>>();
        if (mockMap != null) {
            map.putAll(mockMap);
        }
        return map;
    }

    /**
     * 赋值
     * @param map
     */
    public void putAllMockMap(Map<String, Map<String, String>> map) {
        if (map != null && !map.isEmpty()) {
            if (mockMap == null) {
                mockMap = new ConcurrentHashMap<String, Map<String, String>>(1, loadFactor, 1);
            }
            mockMap.putAll(map);
        }
    }

    /**
     * 清空
     */
    public void clearMockMap() {
        if (mockMap != null) {
            mockMap.clear();
        }
    }

    /**
     * 销毁
     */
    public void destroyMockMap() {
        mockMap = null;
    }

}
