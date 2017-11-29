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
package com.ipd.jsf.worker.thread.jsfins;

import java.util.List;

import com.ipd.jsf.worker.manager.InterfaceInfoManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.worker.common.SingleWorker;

 /**
 * JSF定时检查跨语言
 */
public class JsfCrossLangCheckWorker extends SingleWorker {

	private final static Logger logger = LoggerFactory.getLogger(JsfCrossLangCheckWorker.class);

    @Autowired
    private InterfaceInfoManager interfaceInfoManager;

    @Override
    public boolean run() {
        try {
        	long start = System.currentTimeMillis();
            logger.info("检查接口的跨语言worker开始运行。。。");
            List<Integer> ids = interfaceInfoManager.getCrossLangInterfaceIds();
            if (!CollectionUtils.isEmpty(ids)) {
                for (Integer id : ids) {
                    interfaceInfoManager.updateCrossLang(id);
                }
            }
            logger.info("检查接口的跨语言worker结束，耗时：{}ms", System.currentTimeMillis() - start);
            return true;
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String getWorkerType() {
        return "jsfCrossLangCheckWorker";
    }

}
