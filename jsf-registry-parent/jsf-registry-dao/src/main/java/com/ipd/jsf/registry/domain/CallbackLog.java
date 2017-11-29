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

import java.util.Date;

public class CallbackLog {
    //其他
    public static final int LOGTYPE_OTHER = 0;
    //callback sub exception
    public static final int LOGTYPE_STUB_EXCEPTION = 1;
    //callback timeout exception
    public static final int LOGTYPE_TIMEOUT_EXCEPTION = 2;
    //callback exception
    public static final int LOGTYPE_EXCEPTION = 3;
    //服务列表通知
    public static final int LOGTYPE_PROVIDERLIST = 20;
    //配置信息通知
    public static final int LOGTYPE_CONFIG = 30;

    public static final String REGISTRY_CREATOR = "registry";
    public static final String EVENTWORKER_CREATOR = "event-worker";
    //id
    private long id;

    //ip
    private String ip;

    //接口名
    private String interfaceName;

    //别名
    private String alias;
    
    //版本号
    private Date dataVersion;

    //实例Key
    private String insKey;

    //通知类型
    private int notifyType;

    //日志
    private String logNote;

    //参数通知信息
    private String param;
    
    //日志类型
    private int logType;

    //注册中心ip
    private String regIp;

    //创建人
    private String creator;

    //创建时间
    private Date createTime;

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
     * @return the dataVersion
     */
    public Date getDataVersion() {
        return dataVersion;
    }

    /**
     * @param dataVersion the dataVersion to set
     */
    public void setDataVersion(Date dataVersion) {
        this.dataVersion = dataVersion;
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
     * @return the notifyType
     */
    public int getNotifyType() {
        return notifyType;
    }

    /**
     * @param notifyType the notifyType to set
     */
    public void setNotifyType(int notifyType) {
        this.notifyType = notifyType;
    }

    /**
     * @return the logNote
     */
    public String getLogNote() {
        return logNote;
    }

    /**
     * @param logNote the logNote to set
     */
    public void setLogNote(String logNote) {
        this.logNote = logNote;
    }

    /**
     * @return the param
     */
    public String getParam() {
        return param;
    }

    /**
     * @param param the param to set
     */
    public void setParam(String param) {
        this.param = param;
    }

    /**
     * @return the logType
     */
    public int getLogType() {
        return logType;
    }

    /**
     * @param logType the logType to set
     */
    public void setLogType(int logType) {
        this.logType = logType;
    }

    /**
     * @return the regIp
     */
    public String getRegIp() {
        return regIp;
    }

    /**
     * @param regIp the regIp to set
     */
    public void setRegIp(String regIp) {
        this.regIp = regIp;
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
    
}
