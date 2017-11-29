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
package com.ipd.jsf.worker.thread.stat;

import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Title: <br>
 * <p/>
 * Description: 定时更新接口的Provider和Consumer数<br>
 * <p/>
 */
public class IfaceProviderAndConsumerSumWorker extends SingleWorker {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    private InterfaceInfoManager interfaceInfoManager;

    @Override
    public boolean run() {
        LOGGER.info("===================== now IfaceProviderAndConsumerSumWorker start ========================");

        try {
            interfaceInfoManager.sumProviderAndConsumer();
        } catch (Exception e) {
            LOGGER.error("===================== now IfaceProviderAndConsumerSumWorker Error ========================", e.getMessage());
            interfaceInfoManager.sumProviderAndConsumer();
        }

        LOGGER.info("===================== now IfaceProviderAndConsumerSumWorker stop ========================");
        return true;
    }

    @Override
    public String getWorkerType() {
        return "ifaceProviderAndConsumerSumWorker";
    }

}