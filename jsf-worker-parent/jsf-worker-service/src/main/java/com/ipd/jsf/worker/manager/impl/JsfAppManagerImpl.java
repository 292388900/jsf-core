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
package com.ipd.jsf.worker.manager.impl;

import com.ipd.jsf.worker.dao.JsfAppDAO;
import com.ipd.jsf.worker.domain.JsfApp;
import com.ipd.jsf.worker.manager.JsfAppManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class JsfAppManagerImpl implements JsfAppManager {

    @Autowired
    private JsfAppDAO jsfAppDAO;

    @Override
    public List<JsfApp> getAllApp() {
        return jsfAppDAO.getAllApp();
    }

    @Override
    public List<JsfApp> getAppByIp(String ip) {
        return jsfAppDAO.getAppByIp(ip);
    }

    @Override
    public JsfApp getByAppid(Integer appId) {
        return jsfAppDAO.getByAppid(appId);
    }

    @Override
    public void update(JsfApp jsfApp) {
        jsfAppDAO.update(jsfApp);
    }

	@Override
	public List<JsfApp> getAppListWithToken() {
		return jsfAppDAO.getAppListWithToken();
	}
}
