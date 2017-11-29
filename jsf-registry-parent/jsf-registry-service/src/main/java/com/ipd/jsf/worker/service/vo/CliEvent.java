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
package com.ipd.jsf.worker.service.vo;

import java.util.List;
import java.util.Map;

public class CliEvent {

    private String host;

    private Integer port;//如果有的话..

    private int pid;

    private long startTime;//client 启动时间

    private String alias;

    private Integer protocol;
    
    //true是provider，false是consumer
    private boolean isProvider;

    private short safVer;

    private String registryId; //谁发出的事件   registryIp:port

    private Status status;

    private Cause causeby;

    private long eventTimes;

    private String remark;

    //实例中的接口id
    private List<Integer> interfaceIds;

    private NotifyType notify = NotifyType.all;

    private int interfaceId;
    
    private long aliasVersion;
    
    private Map<String, Long> aliasVersionMap;
    
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    /**
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * @param alias the alias to set
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * @return the protocol
     */
    public Integer getProtocol() {
        return protocol;
    }

    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    /**
     * @return the isProvider
     */
    public boolean isProvider() {
        return isProvider;
    }

    /**
     * @param isProvider the isProvider to set
     */
    public void setProvider(boolean isProvider) {
        this.isProvider = isProvider;
    }

    /**
     * @return the safVer
     */
    public short getSafVer() {
        return safVer;
    }

    /**
     * @param safVer the safVer to set
     */
    public void setSafVer(short safVer) {
        this.safVer = safVer;
    }

    public String getRegistryId() {
        return registryId;
    }

    public void setRegistryId(String registryId) {
        this.registryId = registryId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Cause getCauseby() {
        return causeby;
    }

    public void setCauseby(Cause causeby) {
        this.causeby = causeby;
    }

    public long getEventTimes() {
        return eventTimes;
    }

    public void setEventTimes(long eventTimes) {
        this.eventTimes = eventTimes;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * @return the interfaceIds
     */
    public List<Integer> getInterfaceIds() {
        return interfaceIds;
    }

	/**
	 * @return the notify
	 */
	public NotifyType getNotify() {
		return notify;
	}

	/**
	 * @param notify the notify to set
	 */
	public void setNotify(NotifyType notify) {
		this.notify = notify;
	}

	/**
     * @param interfaceIds the interfaceIds to set
     */
    public void setInterfaceIds(List<Integer> interfaceIds) {
        this.interfaceIds = interfaceIds;
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
	 * @return the aliasVersion
	 */
	public long getAliasVersion() {
		return aliasVersion;
	}

	/**
	 * @param aliasVersion the aliasVersion to set
	 */
	public void setAliasVersion(long aliasVersion) {
		this.aliasVersion = aliasVersion;
	}

	/**
	 * @return the aliasVersionMap
	 */
	public Map<String, Long> getAliasVersionMap() {
		return aliasVersionMap;
	}

	/**
	 * @param aliasVersionMap the aliasVersionMap to set
	 */
	public void setAliasVersionMap(Map<String, Long> aliasVersionMap) {
		this.aliasVersionMap = aliasVersionMap;
	}

	public int hashCode() {
    	int hc = 13;
    	hc = hc*31 + this.interfaceId;
//    	hc = hc*31 + this.alias.hashCode();
    	return hc;
    }

    public boolean equals(Object obj){
    	if(this == obj){
    		return true;
    	}
    	if(!(obj instanceof CliEvent)){
    		return false;
    	}
    	CliEvent event = (CliEvent)obj;
//    	if((alias == null && event.alias != null) || (alias != null && event.alias == null) ||
//    			!alias.equals(event.alias)){
//    		return false;
//    	}
    	if(interfaceId != event.interfaceId){
    		return false;
    	}
    	return true;
    }

    public enum Cause {
        timeout(1),
        connBreak(2),
        unregister(3),
        register(4),
        connCreate(5);
        int num;

        Cause(int i) {
            this.num = i;
        }
    }

    public enum Status {
        online(1),
        offline(0);

        int num;

        Status(int i) {
            this.num = i;
        }
    }

    public enum NotifyType {
    	all(1),
    	onlyNotify(2),
    	onlyRecord(3);
    	int num;
    	NotifyType(int i) {
    		this.num = i;
    	}
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CliEvent(");
        if (this.host != null) sb.append("host:").append(this.host);
        if (this.port != null) sb.append(",port:").append(this.port);
        if (this.pid != 0) sb.append(",pid:").append(this.pid);
        if (this.startTime != 0) sb.append(",startTime:").append(this.startTime);
        if (this.alias != null) sb.append(",alias:").append(this.alias);
        if (this.protocol != null) sb.append(",protocol:").append(this.protocol);
        sb.append(",isProvider:").append(this.isProvider);
        if (this.safVer != 0) sb.append(",safVer:").append(this.safVer);
        if (this.registryId != null) sb.append(",registryId:").append(this.registryId);
        if (this.status != null) {
            sb.append(",status:").append(this.status.name());
        }
        if (this.causeby != null) {
            sb.append(",causeby:").append(this.causeby.name());
        }
        sb.append(",eventTimes:").append(this.eventTimes);
        if (this.remark != null) sb.append(",remark:").append(this.remark);
        if (this.interfaceIds != null) sb.append(",interfaceIds:").append(this.interfaceIds);
        if (this.interfaceId != 0)sb.append(",interfaceId:").append(this.interfaceId);
        if (this.aliasVersion != 0)sb.append(",version:").append(this.aliasVersion);
        if (this.aliasVersionMap != null)sb.append(",version:").append(this.aliasVersionMap);
        sb.append(",notify:").append(this.notify);
        sb.append(")");
        return sb.toString();
    }
}
