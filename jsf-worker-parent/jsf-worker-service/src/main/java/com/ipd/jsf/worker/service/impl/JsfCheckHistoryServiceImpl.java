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
package com.ipd.jsf.worker.service.impl;

import com.ipd.jsf.worker.dao.JsfCheckHistoryDao;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.worker.domain.JsfCheckHistory;
import com.ipd.jsf.worker.service.JsfCheckHistoryService;
import com.ipd.jsf.worker.service.vo.CheckHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class JsfCheckHistoryServiceImpl implements JsfCheckHistoryService {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(JsfCheckHistoryServiceImpl.class);


    @Autowired
    private JsfCheckHistoryDao jsfCheckHistoryDao;

    @Autowired
    private IPRoomCache ipRoomCache;

    @Override
    public String saveHistory(CheckHistory history) {
        JsfCheckHistory checkHistory = historyVo2Domain(history);
        try {
            jsfCheckHistoryDao.insert(checkHistory);
        } catch (Exception e) {
            LOGGER.error("Error when save check history, cause by" + e.getMessage(), e);
            throw new RuntimeException("Error when save check history, cause by" + e.getMessage());
        }
        return "Save success!";
    }

    @Override
    public String saveHistories(List<CheckHistory> histories) {
        List<JsfCheckHistory> checkHistories = historyVo2Domains(histories);
        try {
            if (CommonUtils.isNotEmpty(checkHistories)) {
                jsfCheckHistoryDao.batchInsert(checkHistories);
            }
        } catch (Exception e) {
            LOGGER.error("Error when save check histories, cause by" + e.getMessage(), e);
            throw new RuntimeException("Error when save check histories, cause by" + e.getMessage());
        }
        return "Save success!";
    }

    private JsfCheckHistory historyVo2Domain(CheckHistory vo) {
        JsfCheckHistory checkHistory = new JsfCheckHistory();
        checkHistory.setSrcIP(vo.getSrcIP());
        checkHistory.setSrcRoom(ipRoomCache.getRoomByIp(vo.getSrcIP()));
        checkHistory.setDstIP(vo.getDstIP());
        checkHistory.setDstPort(vo.getDstPort());
        checkHistory.setDstRoom(ipRoomCache.getRoomByIp(vo.getDstIP()));
        checkHistory.setResultCode(vo.getResultCode());
        checkHistory.setResultMessage(checkLength(vo.getResultMessage(), 4096));
        checkHistory.setCheckTime(vo.getCheckTime());
        checkHistory.setCreateTime(new Date());

        return checkHistory;
    }

    private String checkLength(String message, int length) {
        if (message.length() > length) {
            return message.substring(0, length - 1);
        }
        return message;
    }

    private List<JsfCheckHistory> historyVo2Domains(List<CheckHistory> vos) {
        if (CommonUtils.isEmpty(vos)) {
            return null;
        }
        List<JsfCheckHistory> dos = new ArrayList<JsfCheckHistory>();
        for (CheckHistory vo : vos) {
            dos.add(historyVo2Domain(vo));
        }
        return dos;
    }
}
