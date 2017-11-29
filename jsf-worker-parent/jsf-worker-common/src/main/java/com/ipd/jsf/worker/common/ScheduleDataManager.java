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
package com.ipd.jsf.worker.common;

import com.alibaba.fastjson.JSONObject;
import org.apache.zookeeper.data.Stat;

import java.util.List;

/**
 * 调度相关数据管理
 *
 */
public interface ScheduleDataManager {


    /**
     * 注册调度服务器
     *
     * @param server
     * @throws Exception
     */
    void registerScheduleServer(ScheduleServer server) throws Exception;

    /**
     *
     * @param workerType work 对应路径（标识）
     * @return 返回该work对应路径下的所有ScheduleServer ID集合
     * @throws Exception
     */
    List<String> loadScheduleServerIds(String workerType) throws Exception;


    /**
     *
     * @param workerType
     * @return 返回该work对应路径下的所有状态为start为true的ScheduleServer ID集合
     * @throws Exception
     */
    List<String> loadRunningScheduleServerIds(String workerType) throws Exception;

    /**
     *
     * @param workerType work 对应路径（标识）
     * @return 返回该work对应路径下的所有ScheduleServer 对象集合
     * @throws Exception
     */
    List<ScheduleServer> loadScheduleServers(String workerType) throws Exception;


    /**
     *
     * @param workerType work 对应路径（标识）
     * @return 返回该work对应路径下的所有start状态true 的ScheduleServer对象集合
     * @throws Exception
     */
    List<ScheduleServer> loadRunningScheduleServers(String workerType) throws Exception;



    /**
     * 争夺master
     *<br/>
     * <ul>
     *  <li>初始状态无master结点时,直接作为master</li>
     *  <li>master结点下线时，其他结点争夺</li>
     *  <li>下线master重新上线时</li>
     * <ul/>
     * @param workerType
     */
    boolean contendMaster(String workerType,ScheduleServer currentServer) ;

    /**
     * 初始化工作
     * 如结点的创建
     *
     * @param workerType
     * @param isNeeded 是否对一些数据需要进行初始化(个性化，比如只有分布式worker才创建task目录)
     */
    void initialData(String workerType,boolean isNeeded);


    /**
     * 取得Master 的server id
     *
     * @param workerType
     * @return
     */
    String getMaster(String workerType);

    /**
     * 是否是Master 结点
     *
     * @param serverID
     * @param workerType
     * @return
     */
    boolean isMaster(String serverID,String workerType);


    /**
     * load 指定ScheduleServer 的相关信息
     * @param server
     * @return
     */
    ScheduleServerInfo loadScheduleServerInfo(ScheduleServer server);


    /**
     * 设置为固定的master结点，其他结点不能争夺
     * @param workerType
     * @param serverID
     * @return
     */
    boolean fixMaster(String workerType,String serverID);


    /**
     * 存储/更新worker 参数
     * @param parameters
     */
    void saveWorkerParameters(ScheduleServer currentServer,JSONObject parameters);


    /**
     *
     * @param serverID 当前schedule server
     * @param workerType worker workerType
     * @return
     */
    JSONObject getWorkerParameters(String workerType,String serverID);


    /**
     * master结点创建任务
     * task 由 server id#服务总结点数#当前服务index构成#序号
     * @param workerType
     * @throws Exception
     */
    void createTask(String workerType) throws Exception;

    /**
     * 创建指定task
     *
     * @param workerType
     * @param task
     * @throws Exception
     */
    void createTask(String workerType,String task) throws Exception;


    /**
     * 为 workerType 创建任务,排除指定schedule server，即这些server不分配任务
     *
     * @param workerType
     * @param excludeServerIDs schedule server id
     * @throws Exception
     */
    void createTask(String workerType,List<String> excludeServerIDs) throws Exception;

    /**
     * 删除任务
     *
     * @param workerType
     * @param task
     * @throws Exception
     */
    void removeTask(String workerType,String task) throws Exception;


    /**
     * 标记任务状态
     *
     * @param task
     * @throws Exception
     */
    void updateTaskStat(String workerType,String task,TaskStat stat,Stat expectedTaskVersion) throws Exception;


    /**
     * load 改workerType下created的任务
     *
     * @param workerType
     * @return
     * @throws Exception
     */
    List<String> loadCreatedTasks(String workerType) throws Exception;/**


     * load 改workerType下的任务
     *
     * @param workerType
     * @return
     * @throws Exception
     */
    List<String> loadTasks(String workerType) throws Exception;


    /**
     * load 改workerType下未完成的任务:
     * 1、未领取的任务
     * 2、领取了但未完成的任务（这部分任务会重新分配被执行）
     *
     * @param workerType
     * @return
     * @throws Exception
     */
    List<String> loadNotCompletedTasks(String workerType) throws Exception;




    /**
     * 领取属于自己的任务
     *
     * @param workerType
     * @param serverID
     * @return
     * @throws Exception
     */
    String getTask(String workerType,String serverID) throws Exception;



}
