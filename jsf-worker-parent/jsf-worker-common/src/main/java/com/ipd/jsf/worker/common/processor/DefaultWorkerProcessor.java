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
package com.ipd.jsf.worker.common.processor;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import com.ipd.jsf.worker.common.domain.WorkerAlarm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.common.enumtype.AlarmType;
import com.ipd.jsf.worker.common.AbstractWorker;
import com.ipd.jsf.worker.common.DistributeWorker;
import com.ipd.jsf.worker.common.ScheduleDataManager;
import com.ipd.jsf.worker.common.ScheduleServer;
import com.ipd.jsf.worker.common.ScheduleServerInfo;
import com.ipd.jsf.worker.common.ServerInfoAware;
import com.ipd.jsf.worker.common.Worker;
import com.ipd.jsf.worker.common.WorkerManager;
import com.ipd.jsf.worker.common.WorkerProcessor;

public class DefaultWorkerProcessor implements WorkerProcessor {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Worker worker;

    //停止处理worker
    private volatile boolean stoppedProcess = true;

    private ExecutorService processExecutor;

    private ScheduleDataManager scheduleDataManager;

    private ScheduleServer currentServer;

    private WorkerManager workerManager;

    public DefaultWorkerProcessor(ScheduleDataManager scheduleDataManager,Worker worker,ScheduleServer currentServer,WorkerManager workerManager) {
        this.worker = worker;
        this.scheduleDataManager = scheduleDataManager;
        this.currentServer = currentServer;
        this.workerManager = workerManager;
        if ( worker.isImmediate() ){
            processExecutor = Executors.newFixedThreadPool(5,new WorkerProcessorThreadFactory(worker.getWorkerType()));
        }
    }


    @Override
    public void startProcess() {
        stoppedProcess = false;
    }

    @Override
    public void stopProcess(boolean isExecuteWorkerDestroy) {
        stoppedProcess = true;
        if ( isExecuteWorkerDestroy ){
            worker.destroy();
        }
    }


    @Override
    public void  process() {
        if ( stoppedProcess ){
            stoppedProcess = false;
        }
        if ( worker.isImmediate() ){
            if ( processExecutor == null ){
                //TODO 线程支持可配置
                processExecutor = Executors.newFixedThreadPool(5,new WorkerProcessorThreadFactory(worker.getWorkerType()));
            }
            processExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    execute();
                }
            });
        } else {
            execute();
        }

    }


    @Override
    public boolean isProcessing() {
        return !stoppedProcess;
    }

    public void execute() {
            if ( stoppedProcess ){
                logger.info("process is stop, worker name:{}", worker.getWorkerType());
                return;
            }
            try {
                if ( worker instanceof ServerInfoAware){
                    ScheduleServerInfo serverInfo = scheduleDataManager.loadScheduleServerInfo(currentServer);
                    ((ServerInfoAware) worker).setRunningServerInfo(serverInfo);
                }
                worker.init();
                if ( worker instanceof DistributeWorker){
                    distributeWorkerRun((DistributeWorker) worker);
                } else {
                    Date current = new Date();
                    boolean rs = worker.run();
                    logger.info(" worker {} start on :{},elapsed :{} ",worker.getWorkerType(),current,System.currentTimeMillis() - current.getTime());
                    if ( !rs ){//worker return false
                        if ( worker instanceof AbstractWorker && ((AbstractWorker) worker).isErrorAlert()){
                            workerAlarm();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("execute {} worker error",worker.getWorkerType(),e);
            }
    }


    /**
     * 分布式worker的执行
     *
     * @param worker
     * @throws Exception
     */
    private void distributeWorkerRun(DistributeWorker worker) throws Exception {
        String task = null;
        do {
            task = scheduleDataManager.getTask(worker.getWorkerType(),currentServer.getId());
            //领取任务
            if ( task == null ){
                logger.debug("{} worker didn't run ,because of no task", worker.getWorkerType(), task);
                break;
            } else {
                String[] taskInfos = task.split("#");
                if ( taskInfos == null || taskInfos.length != 4){
                    logger.error("{} worker didn't run ,because of task [{}] is invalid", worker.getWorkerType(), task);
                    break;
                }
                int serverNum ;
                int index;
                try {
                     serverNum = Integer.valueOf(taskInfos[1]);
                     index = Integer.valueOf(taskInfos[2]);
                } catch (NumberFormatException e) {
                    logger.error("{} worker didn't run ,because of task [{}] is invalid.server num and index are integer.", worker.getWorkerType(), task, e);
                    scheduleDataManager.removeTask(worker.getWorkerType(),task);
                    break;
                }
                logger.debug("{} server get {} task {}",currentServer.getId(),worker.getWorkerType(),task);
                Date current = new Date();
                boolean rs = worker.run(serverNum, index);
                logger.info(" worker {} start on :{},elapsed :{} ",worker.getWorkerType(),current,System.currentTimeMillis() - current.getTime());
                if ( !rs && worker.isErrorAlert()){
                    workerAlarm();
                }
                //任务完成remove
                scheduleDataManager.removeTask(worker.getWorkerType(), task);
                logger.debug("{} server remove {} task {}",currentServer.getId(),worker.getWorkerType(),task);
            }
        } while ( task != null  );
    }


    /**
     * 发送worker报警信息
     */
    private void workerAlarm(){
        WorkerAlarm alarm = new WorkerAlarm();
        alarm.setContent(String.format("worker [%s] on schedule server [%s] executed failed",worker.getWorkerType(),currentServer.getId()));
        alarm.setAlarmKey(WorkerAlarm.WORKER_ALARM_UMP_KEY);
        alarm.setAlarmType(AlarmType.WORKEREXECUTEFAILED.getValue());
        alarm.setCreateTime(new Date());
        if ( worker instanceof AbstractWorker ){
            alarm.setErps(((AbstractWorker) worker).getErps() != null ? ((AbstractWorker) worker).getErps():"zhouzhichao");
        }
        alarm.setInterfaceName(worker.getClass().getName());
        alarm.setMethodName("run");
        alarm.setAlarmed(0);//未报警状态
        logger.info("start send worker alarm for worker {}", worker.getWorkerType());
        workerManager.sendWorkerAlarm(alarm);
        logger.info("end send worker alarm for worker {}", worker.getWorkerType());
    }


    class WorkerProcessorThreadFactory implements ThreadFactory{

        private  final AtomicInteger poolNumber = new AtomicInteger(1);

        private String workerType ;

        WorkerProcessorThreadFactory(String workerType) {
            this.workerType = workerType;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(String.format("[%s]-worker-processor-%s-thread",workerType,poolNumber.getAndIncrement()));
            return thread;
        }
    }
}
