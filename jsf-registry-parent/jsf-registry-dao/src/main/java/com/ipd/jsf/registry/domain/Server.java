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
package com.ipd.jsf.registry.domain;

import java.io.Serializable;
import java.util.Date;

public class Server implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private int id;
	private String uniqKey;
	private int interfaceId;      //接口id
	private String interfaceName;     //接口名称
	private String ip;
	private int port;
	private int pid;
	private String alias;             //服务别名
	private Integer status;    //服务状态
	private int room;        //所属机房
	private int timeout;
	private int weight;
	private String appPath;
	private int protocol;      //协议名称
	private String contextPath;  //用于rest方式，或者http协议时，包含context参数
	private int safVer;
	private boolean isRandom;
	private Integer srcType;    //来源类型:1-registry, 2-manual, 3-zookeeper
	private String insKey;
	private String attrUrl;
	private String urlDesc;
	private long startTime;
	private Date createTime;
	private Date updateTime;
	private Integer optType;     //0-下线，1-上线
	private boolean reReg;
    /**
     * @return the id
     */
    public int getId() {
        return id;
    }
    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }
    /**
     * @return the uniqKey
     */
    public String getUniqKey() {
        return uniqKey;
    }
    /**
     * @param uniqKey the uniqKey to set
     */
    public void setUniqKey(String uniqKey) {
        this.uniqKey = uniqKey;
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
     * @return the ip
     */
    public String getIp() {
        return ip;
    }
    /**
     * @param ip the ip to set
     */
    public void setIp(String ip) {
        this.ip = ip;
    }
    /**
     * @return the port
     */
    public int getPort() {
        return port;
    }
    /**
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }
    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
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
     * @return the status
     */
    public Integer getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
    /**
     * @return the room
     */
    public int getRoom() {
        return room;
    }
    /**
     * @param room the room to set
     */
    public void setRoom(int room) {
        this.room = room;
    }
    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout the timeout to set
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }
    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }
    /**
     * @return the appPath
     */
    public String getAppPath() {
        return appPath;
    }
    /**
     * @param appPath the appPath to set
     */
    public void setAppPath(String appPath) {
        this.appPath = appPath;
    }
    /**
     * @return the protocol
     */
    public int getProtocol() {
        return protocol;
    }
    /**
     * @param protocol the protocol to set
     */
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }
    /**
     * @return the contextPath
     */
    public String getContextPath() {
        return contextPath;
    }
    /**
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }
    /**
     * @return the safVer
     */
    public int getSafVer() {
        return safVer;
    }
    /**
     * @param safVer the safVer to set
     */
    public void setSafVer(int safVer) {
        this.safVer = safVer;
    }
    /**
     * @return the isRandom
     */
    public boolean isRandom() {
        return isRandom;
    }
    /**
     * @param isRandom the isRandom to set
     */
    public void setRandom(boolean isRandom) {
        this.isRandom = isRandom;
    }
    /**
     * @return the srcType
     */
    public Integer getSrcType() {
        return srcType;
    }
    /**
     * @param srcType the srcType to set
     */
    public void setSrcType(Integer srcType) {
        this.srcType = srcType;
    }
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
     * @return the attrUrl
     */
    public String getAttrUrl() {
        return attrUrl;
    }
    /**
     * @param attrUrl the attrUrl to set
     */
    public void setAttrUrl(String attrUrl) {
        this.attrUrl = attrUrl;
    }
    /**
     * @return the urlDesc
     */
    public String getUrlDesc() {
        return urlDesc;
    }
    /**
     * @param urlDesc the urlDesc to set
     */
    public void setUrlDesc(String urlDesc) {
        this.urlDesc = urlDesc;
    }
    /**
     * @return the startTime
     */
    public long getStartTime() {
        return startTime;
    }
    /**
     * @param startTime the startTime to set
     */
    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    /**
     * @return the createTime
     */
    public Date getCreateTime() {
        return createTime;
    }
    /**
     * @param createTime the createTime to set
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    /**
     * @return the updateTime
     */
    public Date getUpdateTime() {
        return updateTime;
    }
    /**
     * @param updateTime the updateTime to set
     */
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
	/**
	 * @return the optType
	 */
	public Integer getOptType() {
		return optType;
	}
	/**
	 * @param optType the optType to set
	 */
	public void setOptType(Integer optType) {
		this.optType = optType;
	}
	
	/**
	 * @return the reReg
	 */
	public boolean isReReg() {
		return reReg;
	}
	/**
	 * @param reReg the reReg to set
	 */
	public void setReReg(boolean reReg) {
		this.reReg = reReg;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof Server)) {
			return false;
		}
        Server server = (Server)obj;
        if (this.uniqKey != null && this.uniqKey.equals(server.uniqKey)) {
        	return true;
        }
        return false;
	}
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Server(");
        if (id > 0) sb.append("id:").append(this.id);
        sb.append(",uniqKey:").append(this.uniqKey);
        sb.append(",ifaceId:").append(this.interfaceId);
        sb.append(",ifaceName:").append(this.interfaceName);
        sb.append(",ip:").append(this.ip);
        sb.append(",port:").append(this.port);
        sb.append(",pid:").append(this.pid);
        sb.append(",alias:").append(this.alias);
        if (status != null) sb.append(",status:").append(this.status);
        sb.append(",room:").append(this.room);
        sb.append(",timeout:").append(this.timeout);
        sb.append(",weight:").append(this.weight);
        if (appPath != null) sb.append(",appPath:").append(this.appPath);
        sb.append(",protocol:").append(this.protocol);
        if (contextPath != null) sb.append(",contextPath:").append(this.contextPath);
        sb.append(",safVer:").append(this.safVer);
        sb.append(",random:").append(this.isRandom);
        if (srcType != null) sb.append(",srcType:").append(this.srcType);
        if (insKey != null) sb.append(",insKey:").append(this.insKey);
        if (attrUrl != null) sb.append(",attrUrl:").append(this.attrUrl);
        if (urlDesc != null) sb.append(",url:").append(this.urlDesc);
        if (startTime > 0) sb.append(",startTime:").append(this.startTime);
        sb.append(",createTime:").append(this.createTime);
        sb.append(",updateTime:").append(this.updateTime);
        sb.append(",reReg:").append(this.reReg);
        sb.append(")");
        return sb.toString();
    }
}