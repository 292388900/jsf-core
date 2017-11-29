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

public class JsfCheckHistory implements Serializable {

    private static final long serialVersionUID = -7609121721579063685L;

    private int id; //
    private String srcIP; //  请求源地址',
    private String srcRoom; // 请求机房',
    private String dstIP; //     目标IP（例如注册中心IP）',
    private int dstPort; //        目标端口（例如注册中心端口）',
    private String dstRoom; //        目标机房',
    private int resultCode; //      检查结果 0正常 1连不通 2连通业务异常',
    private String resultMessage; //  检查结果明细',
    private Date checkTime; //  `检查时间',
    private Date createTime; //

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSrcIP() {
        return srcIP;
    }

    public void setSrcIP(String srcIP) {
        this.srcIP = srcIP;
    }

    public String getSrcRoom() {
        return srcRoom;
    }

    public void setSrcRoom(String srcRoom) {
        this.srcRoom = srcRoom;
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

    public String getDstRoom() {
        return dstRoom;
    }

    public void setDstRoom(String dstRoom) {
        this.dstRoom = dstRoom;
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

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
