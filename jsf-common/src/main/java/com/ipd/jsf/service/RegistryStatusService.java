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

package com.ipd.jsf.service;

public interface RegistryStatusService {
    /**
     * 查看注册中心实例的配置信息
     * @return
     */
    public String conf() throws Exception;

    /**
     * 查看注册中心实例的机器环境，JVM运行情况
     * @return
     */
    public String envi() throws Exception;

    /**
     * 查看注册中心实例下的统计信息
     * @return
     */
    public String stat() throws Exception;

    /**
     * 查看注册中心实例下的长连接明细
     * @return
     */
    public String cons() throws Exception;

    /**
     * 查看注册中心实例下的订阅明细
     * @return
     */
    public String wchs() throws Exception;

    /**
     * 查看注册中心sql语句使用情况
     * @return
     */
    public String dbex() throws Exception;

    /**
     * 查看注册中心实例下的订阅明细
     * @return
     */
    public String wtch(String interfaceName) throws Exception;

}
