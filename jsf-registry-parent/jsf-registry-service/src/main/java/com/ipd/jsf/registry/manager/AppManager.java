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
package com.ipd.jsf.registry.manager;

import java.util.List;

import com.ipd.jsf.registry.domain.App;
import com.ipd.jsf.registry.domain.JsfIns;

public interface AppManager {
    public int create(App app) throws Exception;
    public int update(App app) throws Exception;
    public App get(int appId) throws Exception;
    public List<App> getList() throws Exception;
    public App getAppFromJsfIns(JsfIns ins);
}
