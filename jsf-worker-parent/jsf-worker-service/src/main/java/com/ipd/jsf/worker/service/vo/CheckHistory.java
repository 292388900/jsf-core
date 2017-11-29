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

import java.io.Serializable;
import java.util.Date;

public class CheckHistory implements Serializable{

    private static final long serialVersionUID = 3355190008566871031L;

    private String srcIP; //  请求源地址',
    private String dstIP; //     目标IP（例如注册中心IP）',
    private int dstPort; //        目标端口（例如注册中心端口）',
    private int resultCode; //      检查结果 0正常 1连不通 2连通业务异常',
    private String resultMessage; //  检查结果明细',
    private Date checkTime; //  `检查时间',

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public String getDstIP() {
        return dstIP;
    }

    public void setDstIP(String dstIP) {
        this.dstIP = dstIP;
    }

    public int getDstPort() {
        return dstPort;
    }

    public void setDstPort(int dstPort) {
        this.dstPort = dstPort;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public Date getCheckTime() {
        return checkTime;
    }

    public void setCheckTime(Date checkTime) {
        this.checkTime = checkTime;
    }

    @Override
    public String toString() {
        return "CheckHistory{" +
                "srcIP='" + srcIP + '\'' +
                ", dstIP='" + dstIP + '\'' +
                ", dstPort=" + dstPort +
                ", resultCode=" + resultCode +
                ", resultMessage='" + resultMessage + '\'' +
                ", checkTime=" + checkTime +
                '}';
    }
}
