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

import java.util.Date;

public class ScanStatusLog {
    //id
    private long id;

    //ip
    private String ip;

    //pid
    private int pid;

    //实例key
    private String insKey;

    //接口
    private String interfaceName;
    
    //类型：1-server表，2-实例表
    private byte type;
    
    //操作信息
    private String detailInfo;

    //创建人
    private String creator;

    //创建人ip
    private String creatorIp;

    //创建时间
    private Date createTime;

    //删除类型：4-反注册，5-逻辑删, 9-物理删
    private int delType;
    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(long id) {
        this.id = id;
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
     * @return the type
     */
    public byte getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(byte type) {
        this.type = type;
    }

    /**
     * @return the detailInfo
     */
    public String getDetailInfo() {
        return detailInfo;
    }

    /**
     * @param detailInfo the detailInfo to set
     */
    public void setDetailInfo(String detailInfo) {
        this.detailInfo = detailInfo;
    }

    /**
     * @return the creator
     */
    public String getCreator() {
        return creator;
    }

    /**
     * @param creator the creator to set
     */
    public void setCreator(String creator) {
        this.creator = creator;
    }

    /**
     * @return the creatorIp
     */
    public String getCreatorIp() {
        return creatorIp;
    }

    /**
     * @param creatorIp the creatorIp to set
     */
    public void setCreatorIp(String creatorIp) {
        this.creatorIp = creatorIp;
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
	 * @return the delType
	 */
	public int getDelType() {
		return delType;
	}

	/**
	 * @param delType the delType to set
	 */
	public void setDelType(int delType) {
		this.delType = delType;
	}
    
}
