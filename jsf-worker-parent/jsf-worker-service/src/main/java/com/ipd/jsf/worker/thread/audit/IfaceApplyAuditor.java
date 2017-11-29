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
package com.ipd.jsf.worker.thread.audit;

import com.ipd.jsf.common.enumtype.DataEnum.IfaceStatus;
import com.ipd.jsf.common.util.FileUtil;
import com.ipd.jsf.worker.dao.JsfUserDao;
import com.ipd.jsf.worker.domain.JsfIfaceApply;
import com.ipd.jsf.worker.domain.User;
import com.ipd.jsf.worker.service.JsfIfaceApplyService;
import com.ipd.jsf.worker.util.EmailUtil;
import com.ipd.jsf.worker.util.ReciveMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class IfaceApplyAuditor extends AbstractAuditor {

    private final static Logger logger = LoggerFactory.getLogger(IfaceApplyAuditor.class);

    @Autowired
    JsfIfaceApplyService jsfIfaceApplyService;

    @Autowired
    JsfUserDao jsfUserDao;


    @PostConstruct
    public void init() {
        getAuditAdapters().put(AuditEnum.INTERFACE_APPLY, this);
    }

    @Override
    public boolean doAudit(ReciveMail mail, String rejectReason) throws Exception {
        String creatorMail = "";
        String subject = mail.getSubject();
        String uid = subject.substring(subject.indexOf(EmailUtil.subjectSplit) + EmailUtil.subjectSplit.length());
        List<JsfIfaceApply> infos = jsfIfaceApplyService.getTobeAudit(uid);
        logger.info("根据{}获取待审核接口数量为:{}", uid, infos.size());

        String from = mail.getFrom();
        String auditor = null;
        try {
            User user = jsfUserDao.getByMail(from);
            if (user != null && StringUtils.hasText(user.getPin())) {
                auditor = user.getPin();
            } else {
                auditor = from;
            }
        } catch (Exception e) {
            // TODO ignore
            auditor = from;
        }

        if (!CollectionUtils.isEmpty(infos)) {
            String[] ifaces = new String[infos.size()];
            for (int i = 0; i < infos.size(); i++) {
                ifaces[i] = infos.get(i).getInterfaceName();
            }

            Timestamp auditTime = new Timestamp(System.currentTimeMillis());

            if (rejectReason == null) {
                jsfIfaceApplyService.batchAudit(ifaces, IfaceStatus.added.getValue(), uid, auditor, auditTime, rejectReason);
            } else {
                jsfIfaceApplyService.batchAudit(ifaces, IfaceStatus.reject.getValue(), uid, auditor, auditTime, rejectReason);
            }

            String creator = infos.get(0).getCreator();
            User user = jsfUserDao.getUserByPin(creator);
            if (user != null && StringUtils.hasText(user.getMail())) {
                creatorMail = user.getMail();
            }
        } else {
            return true;
        }

        String text = "";
        if (rejectReason == null) {
            text = buildAddOkText(infos, auditor);
        } else {
            text = buildRejectText(infos, auditor, rejectReason);
        }

        String[] ownerMail = getOwnerMails(infos);
        String[] cc = mail.getMailAddress(RecipientType.CC);

        if (!StringUtils.hasText(creatorMail)) {
            creatorMail = EmailUtil.adminMail;
        }

        if (cc != null && cc.length != 0) {
            List<String> mailCC = new ArrayList<String>(Arrays.asList(cc));

            if (mailCC.contains(creatorMail)) {
                mailCC.remove(creatorMail);
            }
            if (!mailCC.contains(EmailUtil.adminMail)) {
                mailCC.add(EmailUtil.adminMail);
            }

            if (mailCC.size() == 0) {
                cc = null;
            } else {
                if (mailCC.contains("")) {
                    mailCC.remove("");
                }
                cc = new String[mailCC.size()];
                for (int i = 0; i < mailCC.size(); i++) {
                    cc[i] = mailCC.get(i);
                }
            }
            logger.info("抄送: {}", mailCC.toString());
        } else {
            cc = null;
        }

        logger.info("审核通过, 发送邮件: {}, 内容: {}", subject, "新建接口成功通知");
        EmailUtil.sendEmail(text, ownerMail, cc, null, "新建接口成功通知");

        return true;
    }

    private String[] getOwnerMails(List<JsfIfaceApply> bInfos) {
        String ownerUser = bInfos.get(0).getOwnerUser();
        ownerUser = ownerUser.replace(",", ";");
        List<String> erps = new ArrayList<String>(Arrays.asList(ownerUser.split(";")));
        erps.add(bInfos.get(0).getCreator());
        String[] accountList = new String[erps.size()];
        erps.toArray(accountList);
        List<User> uList = jsfUserDao.getUserByPins(accountList);
        String[] result = new String[uList.size()];
        for (int i = 0; i < uList.size(); i++) {
            result[i] = uList.get(i).getMail();
        }
        return result;
    }

    private String buildAddOkText(List<JsfIfaceApply> bInfos, String auditor) {
        StringBuffer tableContent = new StringBuffer();
        for (int i = 0; i < bInfos.size(); i++) {
            JsfIfaceApply info = bInfos.get(i);

            //department full name
            String departmentFullName = info.getDepartment();


            tableContent.append("<DIV class=leftArea>")
                    .append("<TABLE>").append("<COLGROUP>")
                    .append("<COL width=\"20%\">")
                    .append("<COL width=\"80%\">").append("</COLGROUP>")
                    .append("<TBODY>").append("<TR>")
                    .append("<TH class=textR>接口名</TH><TD>")
                    .append(info.getInterfaceName()).append("</TD></TR>")
                    .append("<TR>").append("<TH class=textR>部门</TH><TD>")
                    .append(departmentFullName).append("</TD></TR>")
                    .append("<TR>").append("<TH class=textR>负责人</TH><TD>")
                    .append(info.getOwnerUser()).append("</TD></TR>")
                    .append("<TR><TH class=textR>接口描述</TH>").append("<TD>")
                    .append(info.getRemark() == null ? "" : info.getRemark()).append("</TD></TR>")
                    .append("<TR><TH class=textR>状态</TH>").append("<TD>")
                    .append("<font color=green>通过</font></TD></TR>")
                    .append("<TR><TH class=textR>审核人</TH>").append("<TD>")
                    .append(auditor).append("</TD></TR>")
                    .append("<TR><TH class=textR>审核时间</TH>").append("<TD>")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())).append("</TD></TR>");
            tableContent.append("</TBODY>").append("</TABLE>");
            if (i == bInfos.size() - 1) {
                tableContent.append("<DIV class=space> 请登录<a href=\"http://jsf.ipd.com\">JSF</a>管理端验证</DIV>");
            } else {
                tableContent.append("<DIV class=space></DIV>");
            }
            tableContent.append("<DIV class=\"rightArea textR\"></DIV>");
            tableContent.append("<DIV class=clearfix></DIV>").append("</DIV>");
        }
        String result = tableContent.toString();
        String okMailTemplate = FileUtil.file2String(FileUtil.getFileByClasspath("/addOkMail.html"));
        okMailTemplate = okMailTemplate.replace("#{tableContent}", result);
        return okMailTemplate;
    }

    public String buildRejectText(List<JsfIfaceApply> bInfos, String auditor, String rejectReason) {
        String erp = auditor;
        StringBuffer tableContent = new StringBuffer();
        for(int i = 0; i < bInfos.size(); i++){
            JsfIfaceApply info = bInfos.get(i);

            //department full name
            String departmentFullName = info.getDepartment();

            tableContent.append("<DIV class=leftArea>")
                    .append("<TABLE>").append("<COLGROUP>")
                    .append("<COL width=\"20%\">")
                    .append("<COL width=\"80%\">").append("</COLGROUP>")
                    .append("<TBODY>").append("<TR>")
                    .append("<TH class=textR>接口名</TH><TD>")
                    .append(info.getInterfaceName()).append("</TD></TR>")
                    .append("<TR>").append("<TH class=textR>部门</TH><TD>")
                    .append(departmentFullName).append("</TD></TR>")
                    .append("<TR>").append("<TH class=textR>负责人</TH><TD>")
                    .append(info.getOwnerUser()).append("</TD></TR>")
                    .append("<TR><TH class=textR>接口描述</TH>").append("<TD>")
                    .append(info.getRemark() == null?"":info.getRemark()).append("</TD></TR>")
                    .append("<TR><TH class=textR>状态</TH>").append("<TD>")
                    .append("<font color=red>未通过, 驳回理由：").append(rejectReason).append("</font></TD></TR>")
                    .append("<TR><TH class=textR>审核人</TH>").append("<TD>")
                    .append(erp).append("</TD></TR>")
                    .append("<TR><TH class=textR>审核时间</TH>").append("<TD>")
                    .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(System.currentTimeMillis())).append("</TD></TR>");
            tableContent.append("</TBODY>").append("</TABLE>");
            tableContent.append("<DIV class=space></DIV>");
            tableContent.append("<DIV class=\"rightArea textR\"></DIV>");
            tableContent.append("<DIV class=clearfix></DIV>").append("</DIV>");
        }
        String result = tableContent.toString();

        String rejectMailTemplate = FileUtil.file2String(FileUtil.getFileByClasspath("/rejectMail.html"));
        rejectMailTemplate = rejectMailTemplate.replace("#{tableContent}", result);
        return rejectMailTemplate;
    }

}
