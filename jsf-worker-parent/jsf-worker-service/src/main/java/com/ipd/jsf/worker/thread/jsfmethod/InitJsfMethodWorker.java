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
package com.ipd.jsf.worker.thread.jsfmethod;

import com.google.common.base.Joiner;
import com.ipd.jsf.common.util.SafTelnetClient;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.JsfIfaceServer;
import com.ipd.jsf.worker.domain.MonitorInterface;
import com.ipd.jsf.worker.service.JSFInterfaceMethodService;
import com.ipd.jsf.worker.service.JsfIfaceServerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class InitJsfMethodWorker extends SingleWorker {

    private Logger logger = LoggerFactory.getLogger(InitJsfMethodWorker.class);

    @Autowired
    private JsfIfaceServerService jsfIfaceServerService;

    @Autowired
    private JSFInterfaceMethodService jSFInterfaceMethodService;

    @Override
    public boolean run() {
        try {
            List<InterfaceInfo> interfaces = jsfIfaceServerService.listAllInterface();
            if (!CollectionUtils.isEmpty(interfaces)) {
                for (InterfaceInfo iface : interfaces) {
                    try {
                        //只取两个个是否合适？
                        List<JsfIfaceServer> servers = jsfIfaceServerService.getOneServer(iface.getInterfaceName());
                        if (servers != null && servers.size() > 0) {
                            for (JsfIfaceServer s : servers) {
                                SafTelnetClient client = new SafTelnetClient(s.getServerIp(), s.getServerPort(), 5000);
                                if(!client.isConnected()) {
                                    logger.warn("telnet not connected for {}, {}, {}", iface.getInterfaceName(), s.getServerIp(), s.getServerPort());
                                    continue;
                                }
                                UpdateMethodTask task = new UpdateMethodTask(client, iface.getInterfaceName(), s.getServerIp(), s.getServerPort());
                                MonitorThreadPoolExecutor.execute(task);
                                break;
                            }
                        }
                    } catch (Exception e) {
                        logger.warn("update {} method errror", iface, e);
                    }
                }

            } else {
              logger.info("update worker : interfaces table is empty");
            }

            return true;
        }catch (Exception e) {
            logger.error("update method worker error:"+ e.getMessage(), e);
        }
        return false;
    }

    @Override
    public String getWorkerType() {
        return "initJsfMethodWorker";
    }

    static class MonitorThreadPoolExecutor {
        private static ArrayBlockingQueue taskCatch = new ArrayBlockingQueue(50);
        private static RejectedExecutionHandler policy = new ThreadPoolExecutor.CallerRunsPolicy();
        private static ExecutorService executorService = new ThreadPoolExecutor(5, 50, 5000L,
                TimeUnit.SECONDS, taskCatch, new NamedThreadFactory("pool-method-updater-"), policy);

        public static void execute(Runnable task) {
            executorService.submit(task);
        }
    }


    class UpdateMethodTask implements Runnable{
        private SafTelnetClient client;
        private String iface ;
        private String ip;
        private int port ;
        public UpdateMethodTask(SafTelnetClient client, String ifaceName, String ip, int port ) {
            this.client = client;
            this.iface = ifaceName;
            this.ip = ip;
            this.port = port;
        }

        @Override
        public void run()  {
            try {
                List<String> methods = getMethod(client, "ls "+iface);
                if(!CollectionUtils.isEmpty(methods)){
                    MonitorInterface ifaceInfo = new MonitorInterface();
                    ifaceInfo.setInterfaceName(iface);
                    ifaceInfo.setMethod(Joiner.on(",").skipNulls().join(methods));
                    jSFInterfaceMethodService.saveOrUpdate(ifaceInfo);
                } else {
                    logger.warn("interface {} has no method", iface);
                }
            } catch (Exception e) {
                    Object[] objects = new Object[]{iface, ip, port};
                    logger.error("执行ls命令异常, 接口:{}, host：{}, port:{}", objects);
                    logger.error(e.getMessage(), e);
            }
        }

        private List<String> getMethod(SafTelnetClient client, String cmd) throws Exception {
            List<String> methods = new ArrayList<String>();
            String result = client.send(cmd);
            if(result.startsWith("No such service ")
                    || result.contains("Sorry! Not support telnet")
                    || result.contains("interface not found")){
                logger.error(result);
                throw new RuntimeException(result);
            }
            result = result.replace("dubbo>", "");
            result = result.replace("jsf>", "");
            result = result.replace(iface, "");
            result = result.replace("\r\n", ",");
            if(result.startsWith(",")){
                result = result.substring(1);
            }
            methods = Arrays.asList(result.split(","));
            Collections.sort(methods);
            return methods;
        }
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
