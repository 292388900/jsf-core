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
package com.ipd.jsf.worker.service;

import com.ipd.jsf.worker.service.vo.CheckHistory;

import java.util.List;

public interface JsfCheckHistoryService {

    /**
     * 保存历史
     *
     * @param history 历史对象
     * @return
     */
    public String saveHistory(CheckHistory history);

    /**
     * 批量保存历史
     *
     * @param histories 历史对象集合
     * @return
     */
    public String saveHistories(List<CheckHistory> histories);
}
