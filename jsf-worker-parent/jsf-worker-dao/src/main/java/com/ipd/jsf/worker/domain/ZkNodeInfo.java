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

public class ZkNodeInfo {
    private int serverCversion;  // 用于数据同步使用，记录zookeeper的provider路径的cversion

    private int clientCversion;  // 用于数据同步使用，记录zookeeper的consumer路径的cversion

    private int serverDversion;  // 用于数据同步使用，记录zookeeper的provider路径的dataversion

    private int clientDversion;  // 用于数据同步使用，记录zookeeper的consumer路径的dataversion

    public int getServerCversion() {
        return serverCversion;
    }

    public void setServerCversion(int serverCversion) {
        this.serverCversion = serverCversion;
    }

    public int getClientCversion() {
        return clientCversion;
    }

    public void setClientCversion(int clientCversion) {
        this.clientCversion = clientCversion;
    }

    public int getServerDversion() {
        return serverDversion;
    }

    public void setServerDversion(int serverDversion) {
        this.serverDversion = serverDversion;
    }

    public int getClientDversion() {
        return clientDversion;
    }

    public void setClientDversion(int clientDversion) {
        this.clientDversion = clientDversion;
    }

}
