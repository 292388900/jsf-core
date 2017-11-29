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
package com.ipd.jsf.registry.manager.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ipd.jsf.registry.dao.CallbackLogDao;
import com.ipd.jsf.registry.domain.CallbackLog;
import com.ipd.jsf.registry.manager.CallbackLogManager;

@Service
public class CallbackLogManagerImpl implements CallbackLogManager {

    @Autowired
    private CallbackLogDao callbackLogDao;

    /* (non-Javadoc)
     * @see com.ipd.jsf.registry.manager.CallbackLogManager#create()
     */
    @Override
    public int create(List<CallbackLog> logList) throws Exception {
        if (logList != null && !logList.isEmpty()) {
            return callbackLogDao.create(logList);
        }
        return 0;
    }

}
