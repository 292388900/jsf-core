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
package com.ipd.jsf.worker.thread.syn.util;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.NoNodeException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.worker.common.PropertyFactory;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.ZkNodeInfo;
import com.ipd.jsf.worker.service.common.URL;

public class ZkHelper {
    /**
     * slf4j logger
     */
    private static final Logger logger = LoggerFactory.getLogger(ZkHelper.class);
    
    /**
     * 永久节点
     */
    private boolean persistentNode = true;
    /**
     * 父节点不存在时，是否强制创建父节点
     */
    private boolean createParent = true;
    private ZooKeeper zookeeper = null;
    private String connStr = "";
    private int sessionTimeout = 50000;

    public ZkHelper() {
        if (zookeeper == null) {
            try {
                connStr = (String) PropertyFactory.getProperty("jdZooKeeper.registry.address");
                zookeeper = new ZooKeeper(connStr, sessionTimeout,
                        new Watcher() {
                            public void process(WatchedEvent event) {
                                logger.debug("监控被触发的事件");
                            }
                        });
                waitUntilConnected(zookeeper);
            } catch (Exception e) {
                logger.error("初始化时异常：" + e.getMessage());
            }
        }
    }


    /**
     * 读取zookeeper的provider和consumer的cversion
     * 
     * @throws KeeperException
     * @throws InterruptedException
     */
    public List<InterfaceInfo> getInterfaceInfoList() {
        List<String> childList = null;
        try {
            childList = zookeeper.getChildren(SynUtil.SAF_SERVICE, false);  // saf_service的child节点
        } catch (KeeperException e) {
            logger.error("getInterfaceInfoList error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("getInterfaceInfoList error: " + e.getMessage(), e);
        }
        if (childList == null) return new ArrayList<InterfaceInfo>();

        ExecutorService executorService = Executors.newFixedThreadPool(5, new WorkerThreadFactory("getInterfaceInfoListPool"));
        final List<InterfaceInfo> interfaceInfoList = Collections.synchronizedList(new ArrayList<InterfaceInfo>(childList.size()));
        try {
            List<Future<Boolean>> futureList = new ArrayList<Future<Boolean>>();
            for (final String intf : childList) {
                futureList.add(
                    executorService.submit(new Callable<Boolean>() {
                        @Override
                        public Boolean call() {
                            try {
                                if (intf != null && !intf.equals("") && !intf.equals("/")) {
                                    InterfaceInfo interfaceInfo = new InterfaceInfo();
                                    interfaceInfo.setInterfaceName(intf);
                                    interfaceInfo.setZkNodeInfo(new ZkNodeInfo());
                                    getProviderVersion(interfaceInfo);
                                    getConsumerVersion(interfaceInfo);
                                    interfaceInfoList.add(interfaceInfo);
                                }
                            } catch (Exception e) {
                                logger.error("initZkNodes: ", e);
                                return false;
                            }
                            return true;
                        }
                    })
                );
            }
            for (Future<Boolean> f : futureList) {
                f.get();
            }
        } catch (Exception e) {
            logger.error("初始化zk节点时，产生异常：" + e.getMessage());
        } finally {
            try {
                if (executorService != null && !executorService.isShutdown()) {
                    executorService.shutdown();
                    executorService = null;
                }
            } catch (Exception e) {
                logger.error("destroy error: {}", e.getMessage());
                executorService = null;
            }
        }
        return interfaceInfoList;
    }

    private void getProviderVersion(InterfaceInfo interfaceInfo) {
        getZkVersion(interfaceInfo, true);
    }

    private void getConsumerVersion(InterfaceInfo interfaceInfo) {
        getZkVersion(interfaceInfo, false);
    }

    private void getZkVersion(InterfaceInfo interfaceInfo, boolean isProvider) {
        String root = null;
        int cversion = 0;
        int dataversion = 0;
        try {
            Stat stat = new Stat();
            String key = null;
            //判断是否provider
            key = isProvider == true ? SynUtil.PROVIDERS : SynUtil.CONSUMERS;
            root = new StringBuilder().append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(interfaceInfo.getInterfaceName()).append(key).toString();
            zookeeper.getData(root, false, stat);
            cversion = stat.getCversion();
            dataversion = stat.getVersion();
            if (isProvider) {
                interfaceInfo.getZkNodeInfo().setServerCversion(cversion);
                interfaceInfo.getZkNodeInfo().setServerDversion(dataversion);
            } else {
                interfaceInfo.getZkNodeInfo().setClientCversion(cversion);
                interfaceInfo.getZkNodeInfo().setClientDversion(dataversion);
            }
        } catch (KeeperException e) {
            logger.error("getProviderVersion is error:" + e.getMessage());
        } catch (InterruptedException e) {
            logger.error("getProviderVersion is error:" + e.getMessage());
        }
    }

    /**
     * 
     * @param intf
     * @param isProvider
     */
    public void initZkProviders(String intf, Map<String, byte[]> zkProviderMap, Map<String, byte[]> dbInZkProvider) throws Exception {
        String root = new StringBuilder().append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(intf).append(SynUtil.PROVIDERS).toString();
        List<String> nodeList = null;
        //获取子节点
        nodeList = zookeeper.getChildren(root, false);
        if (nodeList == null || nodeList.size() == 0) {
            return;
        }
        
        StringBuilder path = new StringBuilder();
        String urlString = null;
        byte[] data = null;
        for (String node : nodeList) {
            // 使用provider节点的url串作为key,需要URL.decode(provider)
            path.append(root).append(SynUtil.SPLITSTR_SLASH).append(node);
            data = zookeeper.getData(path.toString(), false, null);
            path.delete(0, path.length());  //清空
            if (data == null || data.length == 0) {
                continue;
            }
            // 不含source=db
            if (node.indexOf(SynUtil.FROM_DB_ENCODE) == -1) {
                if (SynUtil.isBestVersion(nodeList, node)) {// && online == data[0]
                    urlString = ConvertUtils.convertZKProvider2URL(intf, node, data[0]);
                    if (urlString != null) {
                        zkProviderMap.put(urlString, data);// 需要感知状态的变化
                    }
                }
            } else {
                urlString = ConvertUtils.convertZKProvider2URL(intf, node, data[0]);
                if (urlString != null) {
                    dbInZkProvider.put(urlString, data);// 从db同步来的节点
                }
            }
        }
    }

    public void initZkConsumers(String intf, Map<String, byte[]> zkConsumerMap, Map<String, byte[]> dbInZkConsumer) throws Exception {
        String root = new StringBuilder().append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(intf).append(SynUtil.CONSUMERS).toString();
        List<String> nodeList = null;
        //获取子节点
        nodeList = zookeeper.getChildren(root, false);
        if (nodeList == null || nodeList.size() == 0) {
            return;
        }
        StringBuilder path = new StringBuilder();
        String urlString = null;
        byte[] data = null;
        for (String node : nodeList) {
            // 使用provider节点的url串作为key,需要URL.decode(provider)
            path.append(root).append(SynUtil.SPLITSTR_SLASH).append(node);
            data = zookeeper.getData(path.toString(), false, null);
            path.delete(0, path.length());  //清空
            if (data == null || data.length == 0) {
                // 默认处理为1
                data = new byte[1];
                data[0] = (0x01);
            }
            // 不含source=db
            if (node.indexOf(SynUtil.FROM_DB_ENCODE) == -1) {
                urlString = ConvertUtils.convertZKConsumer2String(intf, node, data[0]);
                if (urlString != null) {
                    zkConsumerMap.put(urlString, data);
                }
            } else {
                dbInZkConsumer.put(URL.decode(node), data);
            }
            
        }
    }

    /**
     * 创建节点
     * @param path
     * @param data
     * @param isLeaf 是否为叶子节点
     * @throws InterruptedException
     */
    public void createNode(String path, byte[] data, boolean isLeaf) throws InterruptedException {
        try {
            zookeeper.create(path, data, Ids.OPEN_ACL_UNSAFE, 
                    this.persistentNode ? CreateMode.PERSISTENT : CreateMode.EPHEMERAL);
        } catch (NoNodeException e) {
            if(createParent) {//如果没有父节点，创建父节点
                String[] nodeArr = path.split(SynUtil.SPLITSTR_SLASH);
                String parent = SynUtil.SPLITSTR_SLASH + nodeArr[1];
                for(int i=2; i<nodeArr.length; i++) {
                    parent = parent  + SynUtil.SPLITSTR_SLASH + nodeArr[i];
                    isLeaf = (i == nodeArr.length - 1);
                    this.createNode(parent, isLeaf ? data : null, isLeaf);
                }
            }else {
                logger.warn("父节点不存在,不同步节点:" + e.getMessage());
            }
        } catch (KeeperException e) {
            logger.warn("创建节点异常,节点可能已经存在:" + e.getMessage());
        }
    }

    public List<String> getChildren(String path) {
        try {
            return zookeeper.getChildren(path, false);//saf_service的child节点
        } catch (KeeperException e) {
            logger.error("getServiceInfos error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("getServiceInfos error: " + e.getMessage(), e);
        }
        return new ArrayList<String>();
    }

    /**
     * 保存上下线记录到zk的接口providers目录上
     * @param iface
     * @param addSet
     * @param delSet
     */
    public void saveOfflineTag(String iface, Set<String> addSet, Set<String> delSet) {
        try {
            if ((addSet == null || addSet.isEmpty()) && (delSet == null || delSet.isEmpty())) {
                return;
            }
            String path = new StringBuilder().append(SynUtil.SAF_SERVICE).append(SynUtil.SPLITSTR_SLASH).append(iface).append(SynUtil.PROVIDERS).toString();
            Stat stat = new Stat();
            byte[] byteData = zookeeper.getData(path, false, stat);
            if (byteData == null) {
                byteData = new byte[0];
            }
            String strData = new String(byteData, "UTF-8");
            Set<String> setData = new HashSet<String>();
            setData.addAll(Arrays.asList(strData.split(";")));
            if (delSet != null) {
                setData.removeAll(delSet);
            }
            if (addSet != null) {
                setData.addAll(addSet);
            }
            zookeeper.setData(path, convertSetToString(setData).getBytes("UTF-8"), stat.getVersion());
        } catch (KeeperException e) {
            logger.error("saveOfflineTag error: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            logger.error("saveOfflineTag error: " + e.getMessage(), e);
        } catch (UnsupportedEncodingException e) {
            logger.error("saveOfflineTag error: " + e.getMessage(), e);
        }
    }

    private String convertSetToString(Set<String> setData) {
        String result = "";
        if (setData!=null) {
            for (String str : setData) {
                if (!"".equals(str)) {
                    result = result + str + ";";
                }
            }
        }
        return result;
    }
    
    /**
     * 删除节点
     * @param path
     * @throws InterruptedException
     * @throws KeeperException
     */
    public void delete(String path) throws InterruptedException, KeeperException {
        zookeeper.delete(path, -1);
    }

    public static void waitUntilConnected(ZooKeeper zooKeeper) {
        CountDownLatch connectedLatch = new CountDownLatch(1);
        Watcher watcher = new ConnectedWatcher(connectedLatch);
        zooKeeper.register(watcher);
        if (States.CONNECTING == zooKeeper.getState()) {
            try {
                connectedLatch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
    }
    
    public String getConnStr() {
        return connStr;
    }

    public void setConnStr(String connStr) {
        this.connStr = connStr;
    }

    public int getSessionTimeout() {
        return this.sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public static void main(String[] args) {
        String connStr = "192.168.108.115:2181,192.168.108.116:2181,192.168.151.144:2181,192.168.151.143:2181,192.168.151.139:2181";
        try {
            ZooKeeper zookeeper = new ZooKeeper(connStr, 100000,
                new Watcher() {
                    public void process(WatchedEvent event) {
                        logger.debug("监控被触发的事件");
                    }
                });
            Stat stat = new Stat();
            byte[] result = zookeeper.getData("/saf_service/com.ipd.testjsf.HelloBaontService/providers", false, stat);
            zookeeper.setData("/saf_service/com.ipd.testjsf.HelloBaontService/providers", result, stat.getVersion());
            System.out.println(result);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

class ConnectedWatcher implements Watcher {
    
    private CountDownLatch connectedLatch;

    ConnectedWatcher(CountDownLatch connectedLatch) {
        this.connectedLatch = connectedLatch;
    }

    @Override
    public void process(WatchedEvent event) {
        if (event.getState() == KeeperState.SyncConnected) {
            connectedLatch.countDown();
        }
    }
}