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
package com.ipd.jsf.monitor.service.vo;

import java.util.List;

public class RegistryData {
    //ip的数据列表
    private List<IpData> dataList;
    //注册中心ip
    private String regIp;
    //注册中心port
    private int regPort;
    //发送时间
    private long time;
    /**
     * @return the dataList
     */
    public List<IpData> getDataList() {
        return dataList;
    }
    /**
     * @param dataList the dataList to set
     */
    public void setDataList(List<IpData> dataList) {
        this.dataList = dataList;
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
     * @return the regPort
     */
    public int getRegPort() {
        return regPort;
    }
    /**
     * @param regPort the regPort to set
     */
    public void setRegPort(int regPort) {
        this.regPort = regPort;
    }
    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }
    /**
     * @param time the time to set
     */
    public void setTime(long time) {
        this.time = time;
    }
}
