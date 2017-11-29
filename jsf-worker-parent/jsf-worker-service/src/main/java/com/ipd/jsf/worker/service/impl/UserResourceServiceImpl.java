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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ipd.jsf.worker.service.UserResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.worker.common.utils.WorkerThreadFactory;
import com.ipd.jsf.worker.dao.InterfaceInfoDao;
import com.ipd.jsf.worker.dao.RoleDao;
import com.ipd.jsf.worker.dao.UserResourceDao;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.Role;
import com.ipd.jsf.worker.domain.UserResource;
import com.ipd.jsf.worker.util.PropertyUtil;

@Service
public class UserResourceServiceImpl implements UserResourceService {

    private static final Logger logger = LoggerFactory.getLogger(UserResourceServiceImpl.class);

    private String iface_server_manage_role = PropertyUtil.getProperties("iface.server.manage.role");

    @Autowired
    private UserResourceDao userResourceDao;

    @Autowired
    private InterfaceInfoDao interfaceInfoDao;

    @Autowired
    private RoleDao roleDao;

    private Integer roleId = 122;

    @Override
    public void check() {
        //获取所有有效接口
        List<InterfaceInfo> interfaceInfos = null;
        while (interfaceInfos == null) {
            try {
                interfaceInfos = interfaceInfoDao.getErps();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ie) {
                    logger.error(ie.getMessage(), ie);
                }
            }
        }

        //获取providerAdmin role Id
        Role role = new Role();
        role.setName(iface_server_manage_role);
        role.setType(DataEnum.RoleTypeEnum.RESOURCE.getValue());
        List<Role> roles = roleDao.findRoles(role);

        if(!CollectionUtils.isEmpty(roles)){
            roleId = roles.get(0).getId();
        }

        final Integer pcType = 1;

        ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 4, new WorkerThreadFactory("userResourceServicePool"));
        CompletionService<Boolean> service = new ExecutorCompletionService<Boolean>(pool);

        for (final InterfaceInfo interfaceInfo : interfaceInfos) {
            service.submit(new Callable<Boolean>() {

                @Override
                public Boolean call() {
                    String owners = interfaceInfo.getOwnerUser();
                    if (owners == null || owners.length() == 0) {
                        return false;
                    }
                    if (owners.contains(";")) {
                        logger.info("interface : {}, owners : {} exists ;", new Object[] {interfaceInfo.getInterfaceName(), owners});
                        owners = owners.replaceAll(";", ",");
                        owners = owners.replaceAll(",,", ",");
                        interfaceInfo.setOwnerUser(owners);
                        try {
                            interfaceInfoDao.update(interfaceInfo);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    String [] ownerArr = owners.split(",");
                    if (ownerArr == null || ownerArr.length == 0) {
                        return false;
                    }
                    List<UserResource> urs = new ArrayList<UserResource>();
                    for (String owner : ownerArr) {
                        if (owner != null && owner.length() > 0 && userResourceDao.isExists(owner,
                                interfaceInfo.getInterfaceId(),
                                DataEnum.ResourceTypeEnum.INTERFACE.getValue(),
                                roleId,
                                pcType) <= 0) {
                            logger.info("interface : {}, owner : {} not exists providerAdmin resource;", new Object[] {interfaceInfo.getInterfaceName(), owner});
                            UserResource ur = new UserResource();
                            ur.setPin(owner);
                            ur.setResType(DataEnum.ResourceTypeEnum.INTERFACE.getValue());
                            ur.setResId(interfaceInfo.getInterfaceId());
                            ur.setRoleId(roleId);
                            ur.setPcType(pcType);// provider
                            urs.add(ur);
                        }
                    }
                    if (urs.size() > 0) {
                        userResourceDao.batchInsert(urs);
                    }
                    return true;
                }

            });
            try {
                Thread.sleep(100);
            } catch (InterruptedException ie) {
                logger.error(ie.getMessage(), ie);
            }
        }
        for (final InterfaceInfo interfaceInfo : interfaceInfos) {
            try {
                Boolean result = service.take().get();
                logger.info("interface : {}, set owner not exists providerAdmin resource {};", new Object[]{interfaceInfo.getInterfaceName(), result});
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            } catch (ExecutionException e) {
                logger.error(e.getMessage(), e);
            }
        }
        pool.shutdown();
    }

}