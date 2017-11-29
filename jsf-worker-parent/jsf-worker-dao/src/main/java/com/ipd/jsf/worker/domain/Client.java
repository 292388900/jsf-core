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
package com.ipd.jsf.worker.domain;

import java.io.Serializable;
import java.util.Date;

public class Client implements Serializable{

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 8002109993785815003L;

    private int id;
    private String uniqKey;
    private Integer interfaceId;    // 接口id
    private String interfaceName;   // 接口名称
    private String ip;
    private int pid;
    private String alias;           //服务别名
    private int protocol;           //协议
    private String version;         //版本号，兼容saf1.0
    private int status;
    private int safVer;
    private String insKey;
    private int srcType;     //来源类型:1-registry, 2-manual, 3-zookeeper
    private long startTime;
    private String token;
    private String appPath;
    private Date createTime;
    private Date updateTime;
    private String urlDesc;
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
    public Integer getInterfaceId() {
        return interfaceId;
    }
    /**
     * @param interfaceId the interfaceId to set
     */
    public void setInterfaceId(Integer interfaceId) {
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
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    /**
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    /**
     * @return the status
     */
    public int getStatus() {
        return status;
    }
    /**
     * @param status the status to set
     */
    public void setStatus(int status) {
        this.status = status;
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
     * @return the srcType
     */
    public int getSrcType() {
        return srcType;
    }
    /**
     * @param srcType the srcType to set
     */
    public void setSrcType(int srcType) {
        this.srcType = srcType;
    }
    
    public long getStartTime() {
		return startTime;
	}
	public void setStartTime(long startTime) {
		this.startTime = startTime;
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
     * @return the token
     */
    public String getToken() {
        return token;
    }
    /**
     * @param token the token to set
     */
    public void setToken(String token) {
        this.token = token;
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
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Client(");
        sb.append("id:").append(this.id);
        sb.append(",uniqKey:").append(this.uniqKey);
        sb.append(",interfaceId:").append(this.interfaceId);
        sb.append(",interfaceName:").append(this.interfaceName);
        sb.append(",ip:").append(this.ip);
        sb.append(",pid:").append(this.pid);
        sb.append(",alias:").append(this.alias);
        sb.append(",protocol:").append(this.protocol);
        sb.append(",version:").append(this.version);
        sb.append(",status:").append(this.status);
        sb.append(",safVer:").append(this.safVer);
        sb.append(",srcType:").append(this.srcType);
        sb.append(",startTime:").append(this.startTime);
        if (this.token!=null) sb.append(",token:").append(this.token);
        if (this.appPath!=null) sb.append(",appPath:").append(this.appPath);
        if (this.createTime!=null) sb.append(",createTime:").append(this.createTime);
        if (this.updateTime!=null) sb.append(",updateTime:").append(this.updateTime);
        sb.append(")");
        return sb.toString();
    }
}
