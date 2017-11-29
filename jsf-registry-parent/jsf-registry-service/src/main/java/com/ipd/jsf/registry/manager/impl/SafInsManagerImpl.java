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

import com.ipd.jsf.common.enumtype.InstanceStatus;
import com.ipd.jsf.common.util.PropertyFactory;
import com.ipd.jsf.gd.util.StringUtils;
import com.ipd.jsf.registry.cache.AppCache;
import com.ipd.jsf.registry.cache.AppInsCache;
import com.ipd.jsf.registry.dao.JsfInsDao;
import com.ipd.jsf.registry.domain.App;
import com.ipd.jsf.registry.domain.AppIns;
import com.ipd.jsf.registry.domain.JsfIns;
import com.ipd.jsf.registry.manager.AppInsManager;
import com.ipd.jsf.registry.manager.AppManager;
import com.ipd.jsf.registry.manager.SafInsManager;
import com.ipd.jsf.registry.util.DBLog;
import com.ipd.jsf.registry.util.HttpClientUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.List;

@Service
public class SafInsManagerImpl implements SafInsManager {

    private static Logger logger = LoggerFactory.getLogger(SafInsManagerImpl.class);

    private volatile boolean isAppOpen = false;

    private volatile boolean isSaveAppIns = false;

    @Autowired
    private JsfInsDao jsfInsDao;

    @Autowired
    private AppManager appManagerImpl;

    @Autowired
    private AppInsManager appInsManagerImpl;

    @Autowired
    private AppCache appCache;

    @Autowired
    private AppInsCache appInsCache;

    @PostConstruct
    public void init() {
        try {
            Object tmp = PropertyFactory.getProperty("deploy.app.open.flag");
            isAppOpen = (tmp == null) ? false : Boolean.parseBoolean(String.valueOf(tmp));
            logger.info("deploy app is : {}", isAppOpen);

            Object objSaveIns = PropertyFactory.getProperty("deploy.app.saveins");
            isSaveAppIns = (objSaveIns == null) ? false : Boolean.parseBoolean(String.valueOf(objSaveIns));
            logger.info("register well save appins is : {}", objSaveIns);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * 新增/修改心跳，用ON DUPLICATE KEY UPDATE
     */
    @Override
    public int register(JsfIns ins) throws Exception {
        if (ins != null) {
            try {
                String insKey = jsfInsDao.getInsKeyByInsKey(ins.getInsKey());
                //如果实例不存在，或者实例状态为死亡，或者被逻辑删除，就需要新建或更新下实例
                if (insKey == null || insKey.isEmpty()) {
                    if (isSaveAppIns) {
                        int jsfAppInsId = getJsfAppInsIdAndSaveAppIns(ins);
                        ins.setJsfAppInsId(jsfAppInsId);
                    }else {
                        saveAppByJsfIns(ins);
                    }
                    int result = jsfInsDao.create(ins, InstanceStatus.online.value());
                    DBLog.info("add instance:{}", ins);
                    return result;
                } else {
                    DBLog.info("add instance is cancel, instance is exists : {}", ins);
                }
            } catch (DataIntegrityViolationException e) {
                logger.error("instance key:{}, error:{}", ins.getInsKey(), e.getMessage());
            }
        }
        return 0;
    }

    @Override
    public int saveHb(List<String> insKeyList, Date hbTime, String regIp) throws Exception {
        return jsfInsDao.batchUpdateHb(insKeyList, hbTime, regIp);
    }

    @Override
    public List<String> getInsKeyListByInsKey(List<String> insKeyList) throws Exception {
        return jsfInsDao.getInsKeyListByInsKey(insKeyList);
    }

    /**
     * 保存app并获取appIns表中的id
     *
     * @param ins
     * @return
     */
    private int getJsfAppInsIdAndSaveAppIns(JsfIns ins) {
        if (ins == null || ins.getAppId() == 0 || ins.getAppInsId() == null || ins.getAppInsId().isEmpty()) return 0;
        int jsfAppInsId = appInsCache.getJsfAppInsId(ins.getAppId(), ins.getAppInsId());
        try {
            AppIns appIns = appInsManagerImpl.convertAppInsFromJsfIns(ins);
            if (jsfAppInsId == 0) {
                try {
                    //获取主键，然后写入应用实例表中
                    int jsfAppId = getJsfAppIdAndSaveApp(ins);
                    //如果有jsfAppId才保存，没有jsfAppId就不保存
                    if (jsfAppId > 0) {
                        //写入应用实例表
                        appIns.setJsfAppId(jsfAppId);
                        appInsManagerImpl.create(appIns);
                        jsfAppInsId = appIns.getJsfAppInsId();
                        if (jsfAppInsId == 0) {
                            jsfAppInsId = appInsCache.getJsfAppInsId(ins.getAppId(), ins.getAppInsId());
                        }
                    } else {
                        logger.warn("cannot create appIns, cause: jsfAppId is 0. appId:{}, appInsId:{}, insKey:{}", appIns.getAppId(), appIns.getAppInsId(), appIns.getInsKey());
                    }
                } catch (Exception e) {
                    logger.error("create appIns appId:" + appIns.getAppId() + ", appInsId:" + appIns.getAppInsId() + ", insKey:" + appIns.getInsKey() + " error:" + e.getMessage(), e);
                }
            } else {
                try {
                    //获取主键，然后写入应用实例表中
                    int jsfAppId = getJsfAppIdAndSaveApp(ins);
                    if (jsfAppId > 0) {
                        //写入应用实例表
                        appIns.setJsfAppId(jsfAppId);
                        appInsManagerImpl.update(appIns);
                    }
                } catch (Exception e) {
                    logger.error("update appIns appId:" + appIns.getAppId() + ", appInsId:" + appIns.getAppInsId() + ", insKey:" + appIns.getInsKey() + " error:" + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error("get id and save app error:" + e.getMessage(), e);
        }
        return jsfAppInsId;
    }

    private void saveAppByJsfIns(JsfIns ins) {
        try {
            int jsfAppId = appCache.getJsfAppId(ins.getAppId());
            if (jsfAppId == 0 && ins.getAppId() != 0 && !StringUtils.isEmpty(ins.getAppName())) {
                App app = appManagerImpl.getAppFromJsfIns(ins);
                appManagerImpl.create(app);
                logger.info("add app:{}", app);
            }
        } catch (Exception e) {
            logger.error("add app error, ins:{}, error:{}", ins, e.getMessage(), e);
        }
    }

    //保存app并获取jsfAppId
    private int getJsfAppIdAndSaveApp(JsfIns ins) {
        if (ins == null || ins.getAppId() == 0) return 0;
        //获取主键，然后写入应用实例表中
        int jsfAppId = appCache.getJsfAppId(ins.getAppId());
        App app = appManagerImpl.getAppFromJsfIns(ins);
        if (jsfAppId == 0) {
            if (isAppOpen == true && ins.getAppId() > 0 && (ins.getAppName() == null || ins.getAppName().isEmpty())) {
                //如果appName为空，且appId不为0，就到自动部署提供的api里获取appName
                ins.setAppName(HttpClientUtil.getAppNameByAppId(ins.getAppId()));
            }
            if (ins.getAppName() != null && !ins.getAppName().isEmpty()) {
                try {
                    //创建app
                    appManagerImpl.create(app);
                } catch (Exception e) {
                    logger.error("create App appId:" + ins.getAppId() + ", appInsId:" + ins.getAppInsId() + ", insKey:" + ins.getInsKey() + " error:" + e.getMessage(), e);
                }
            }
            jsfAppId = app.getJsfAppId();
            if (jsfAppId == 0) {  //如果内存里没有，就到数据库里取
                jsfAppId = appCache.getJsfAppId(app.getAppId());
            }
        }
//        else {
//        	if (ins.getAppName() != null && !ins.getAppName().isEmpty()) {
//	            try {  //如果
//	                appManagerImpl.update(app);
//	            } catch (Exception e) {
//	                logger.error("update App appId:" + ins.getAppId() + ", appInsId:" + ins.getAppInsId() + ", insKey:" + ins.getInsKey() + " error:" + e.getMessage(), e);
//	            }
//        	}
//        }
        return jsfAppId;
    }

}
