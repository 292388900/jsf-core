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
package com.ipd.jsf.zookeeper.cache;

 import com.ipd.jsf.zookeeper.ZkClient;

 import org.apache.zookeeper.data.Stat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;

 import java.util.Map.Entry;
 import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

 /**
  * Title: VersionChecker,同步全部PathCache的数据<br>
  *
  * Description: 方法基本上是friendly的，只能本包调用<br>
  *
  */
 public class VersionChecker {

     private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);

     private static volatile ScheduledExecutorService versionChecker = null;

     private static int version_check_delay = 5*60;//以六分钟为检查周期

     private static int update_top = 4*60;//五分钟无更新则自己取一次

     private static ZkClient client;
     private static volatile boolean isStarted = false;
     /**
      * 保留了订阅的path 和对应pathcache的关系
      */
     private static ConcurrentHashMap<PathCache, String> paths = new ConcurrentHashMap<PathCache, String>();

     /**
      * 增加订阅
      *
      * @param pathCache
      *            对应pathcache
      */
     static void addVerionCheck(PathCache pathCache) {
         paths.putIfAbsent(pathCache, pathCache.path);
     }

     /**
      * 删除订阅
      *
      * @param pathCache
      *            订阅的path
      */
     static void removeVerionCheck(PathCache pathCache) {
         paths.remove(pathCache);
     }

     /**
      * 启动版本检查定时器
      *
      * @param zkClient
      *            zk连接
      */
     public static synchronized void start(ZkClient zkClient){
         if (isStarted || versionChecker != null) {
             return;
         }
         client = zkClient;
         try {
             /*
              * 定时取回本路径的版本--数据版本 字节点版本
              */
             versionChecker = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("PathCache-versionChecker"));
             versionChecker.scheduleWithFixedDelay(new Runnable(){

                 @Override
                 public void run() {
                     if (client == null || !client.isAvailable()) {
                         logger.error("version check aborted because of zkClient is null or unavailable");
                         return;
                     }
                     // 在此检查是否需要更新？
                     Long now = System.currentTimeMillis();
                     for (Entry<PathCache, String> pathEntry : paths.entrySet()) {
                         PathCache pathCache = pathEntry.getKey();
                         String path = pathEntry.getValue();
                         if((now-pathCache.updateTime)/1000 < update_top){ //最近时间内有更新
                             logger.info("Path {} have update in near {} seconds, schedule version check return now..",path,update_top);
                             continue;
                         }
                         try {
                             if(!client.exists(path)){
                                 logger.warn("zookeeper Path {} not exist anymore!",path);
                                 continue;
                             }
                             Stat stat = client.getVersion(path);
                             Integer dataVersion = stat.getVersion();
                             Integer childVersion = stat.getCversion();
                             Boolean needUpdate = false;
                             if(!dataVersion.equals(pathCache.pathDataVersion)){
                                 logger.info("Need sync data for Path:{} .",path);
                                 Event event = new Event();
                                 event.setEventType(EventType.dataChanged);
                                 //eventQueue.add(event);
                                 pathCache.tagNotify(event);
                                 needUpdate = true;
                             }
                             else if(!childVersion.equals(pathCache.pathChildVersion)){
                                 logger.info("Need sync child for Path:{} .",path);
                                 Event event = new Event();
                                 event.setEventType(EventType.childrenChanged);
                                 event.setChildrens(pathCache.getData());
                                 //eventQueue.add(event);
                                 pathCache.tagNotify(event);
                                 needUpdate = true;

                             }
                             if(needUpdate) pathCache.checkNeedReconnect();
                         } catch (Throwable e) {
                             logger.error("Catch error when check version of path:"
                                     + path, e);
                         }
                     }
                 }

             }, version_check_delay, version_check_delay, TimeUnit.SECONDS);
             logger.info("start version checker success !");

             isStarted = true;
         } catch (Exception e) {
             logger.error("start version checker error !", e);
         }
     }

     /**
      * 关闭检测器
      */
     static void close() {
         logger.info("close version checker...");
         if (versionChecker != null) {
             versionChecker.shutdown();
         }
     }

     /**
      * @param newclient the client to set
      */
     public static void setClient(ZkClient newclient) {
         client = newclient;
     }
 }

 class NamedThreadFactory implements ThreadFactory{

     private static final AtomicInteger POOL_SEQ = new AtomicInteger(1);

     private final AtomicInteger mThreadNum = new AtomicInteger(1);

     private final String mPrefix;

     private final boolean mDaemo;

     private final ThreadGroup mGroup;

     public NamedThreadFactory()
     {
         this("pool-" + POOL_SEQ.getAndIncrement(),false);
     }

     public NamedThreadFactory(String prefix)
     {
         this(prefix,false);
     }

     public NamedThreadFactory(String prefix,boolean daemo)
     {
         mPrefix = prefix + "-thread-";
         mDaemo = daemo;
         SecurityManager s = System.getSecurityManager();
         mGroup = ( s == null ) ? Thread.currentThread().getThreadGroup() : s.getThreadGroup();
     }

     public Thread newThread(Runnable runnable)
     {
         String name = mPrefix + mThreadNum.getAndIncrement();
         Thread ret = new Thread(mGroup,runnable,name,0);
         ret.setDaemon(mDaemo);
         return ret;
     }

     public ThreadGroup getThreadGroup()
     {
         return mGroup;
     }
 }
