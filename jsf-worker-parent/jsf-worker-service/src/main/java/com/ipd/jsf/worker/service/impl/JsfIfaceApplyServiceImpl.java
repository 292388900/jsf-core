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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.ipd.jsf.common.enumtype.DataEnum;
import com.ipd.jsf.common.enumtype.DataEnum.ResourceTypeEnum;
import com.ipd.jsf.common.enumtype.DataEnum.RoleTypeEnum;
import com.ipd.jsf.worker.dao.JsfIfaceApplyDao;
import com.ipd.jsf.worker.dao.UserResourceDao;
import com.ipd.jsf.worker.domain.JsfIfaceApply;
import com.ipd.jsf.worker.domain.UserResource;
import com.ipd.jsf.worker.manager.InterfaceInfoManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.ipd.jsf.worker.dao.RoleDao;
import com.ipd.jsf.worker.domain.InterfaceInfo;
import com.ipd.jsf.worker.domain.Role;
import com.ipd.jsf.worker.service.JsfIfaceApplyService;
import com.ipd.jsf.worker.util.PropertyUtil;

@Service
public class JsfIfaceApplyServiceImpl implements JsfIfaceApplyService {

    String iface_server_manage_role = PropertyUtil.getProperties("iface.server.manage.role");

    @Autowired
    JsfIfaceApplyDao jsfIfaceApplyDao;

    @Autowired
    InterfaceInfoManager interfaceInfoManager;

    @Autowired
    UserResourceDao userResourceDao;

    @Autowired
    RoleDao roleDao;

    @Override
    public void batchAudit(String[] ifaces, int status, String uid,
                           String auditor, Timestamp auditTime, String rejectReason) throws Exception {
        List<JsfIfaceApply> applies = jsfIfaceApplyDao.batchGetByname(ifaces);
        if (applies.size() > 0) {

            if (status == DataEnum.IfaceStatus.added.getValue()) {
                List<InterfaceInfo> ifaceInfos = getIfacesByApply(applies);
                Role role = new Role();
                role.setName(iface_server_manage_role);
                role.setType(RoleTypeEnum.RESOURCE.getValue());
                List<Role> roles = roleDao.findRoles(role);
                System.out.println(role.getName());
                Integer roleId = null;
                if (!CollectionUtils.isEmpty(roles)) {
                    roleId = roles.get(0).getId();
                }

                for (InterfaceInfo info : ifaceInfos) {
                    InterfaceInfo isExistInfo = interfaceInfoManager.getByInterfaceName(info.getInterfaceName());
                    if (isExistInfo == null) {
                        interfaceInfoManager.create(info);

                        String pins = info.getOwnerUser();
                        String[] ps = pins.split(",");
                        List<UserResource> urs = new ArrayList<UserResource>();
                        for (String pin : ps) {
                            UserResource ur = new UserResource();
                            ur.setPin(pin);
                            ur.setResType(ResourceTypeEnum.INTERFACE.getValue());
                            ur.setResId(info.getInterfaceId());
                            ur.setRoleId(roleId);
                            ur.setPcType(1);// provider
                            urs.add(ur);
                        }
                        if (urs.size() > 0) {
                            userResourceDao.batchInsert(urs);
                        }
                    } else {
                        if (isExistInfo.getValid() == 0) {//之前删除过
                            //fixed
                            if (isExistInfo.getOwnerUser() != null) {
                                isExistInfo.setOwnerUser(isExistInfo.getOwnerUser().replaceAll(";", ","));
                            }
                            String owner = info.getOwnerUser() + "," + isExistInfo.getOwnerUser();
                            owner = Joiner.on(",").skipNulls().join(new HashSet<String>(Arrays.asList(owner.split(","))));
                            info.setOwnerUser(owner);
                            interfaceInfoManager.deleteToSave(info);
                        } else {
                            //fixed
                            if (isExistInfo.getOwnerUser() != null) {
                                isExistInfo.setOwnerUser(isExistInfo.getOwnerUser().replaceAll(";", ","));
                            }
                            String owner = info.getOwnerUser() + "," + isExistInfo.getOwnerUser();
                            owner = Joiner.on(",").skipNulls().join(new HashSet<String>(Arrays.asList(owner.split(","))));
                            isExistInfo.setOwnerUser(owner);
                            interfaceInfoManager.updateByName(isExistInfo);
                        }

                        userResourceRelate(isExistInfo, roleId);
                    }
                }
            }

            jsfIfaceApplyDao.batchAudit(ifaces, status, uid, auditor, auditTime, rejectReason);
        }
    }

    private void userResourceRelate(InterfaceInfo ifaceInfo, Integer roleId) throws Exception {
        String pins = ifaceInfo.getOwnerUser();
        pins = pins.replace(";", ",");
        String[] ps = pins.split(",");
        List<String> erps = userResourceDao.findAuthErps(ifaceInfo.getInterfaceId(), ResourceTypeEnum.INTERFACE.getValue());
        List<UserResource> urs = new ArrayList<UserResource>();

        for (String pin : ps) {
            if (erps.contains(pin) || !StringUtils.hasText(pin)) {
                continue;
            }
            UserResource ur = new UserResource();
            ur.setPin(pin);
            ur.setResType(ResourceTypeEnum.INTERFACE.getValue());
            ur.setResId(ifaceInfo.getInterfaceId());
            ur.setPcType(1);
            ur.setRoleId(roleId);
            urs.add(ur);
        }
        if (urs.size() > 0) {
            userResourceDao.batchInsert(urs);
        }
    }

    @Override
    public List<JsfIfaceApply> getTobeAudit(String uid) {
        return jsfIfaceApplyDao.getTobeAudit(uid);
    }

    private List<InterfaceInfo> getIfacesByApply(List<JsfIfaceApply> applies) {
        List<InterfaceInfo> result = new ArrayList<InterfaceInfo>();
        for (JsfIfaceApply apply : applies) {
            InterfaceInfo info = getJsfInfo(apply);

            result.add(info);
        }
        return result;
    }

    private InterfaceInfo getJsfInfo(JsfIfaceApply apply) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        InterfaceInfo info = new InterfaceInfo();
        info.setCreateTime(timestamp);
        info.setCreator(apply.getCreator());
        info.setDepartment(apply.getDepartment());
        info.setDepartmentCode(apply.getDepartmentCode());
        info.setInterfaceName(apply.getInterfaceName());
        info.setUpdateTime(timestamp);
        info.setModifier(apply.getCreator());
        info.setOwnerUser(apply.getOwnerUser());
        info.setRemark(apply.getRemark());
        info.setSource((byte) 2);// jsf
        info.setValid(1);
        info.setHasJsfClient(1);
        return info;
    }

}
