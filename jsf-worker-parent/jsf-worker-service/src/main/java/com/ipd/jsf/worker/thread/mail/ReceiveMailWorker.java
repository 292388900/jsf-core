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
package com.ipd.jsf.worker.thread.mail;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Session;
import javax.mail.Store;

import com.ipd.jsf.worker.dao.JsfUserDao;
import com.ipd.jsf.worker.domain.JsfIfaceApply;
import com.ipd.jsf.worker.domain.User;
import com.ipd.jsf.worker.thread.audit.AbstractAuditor;
import com.ipd.jsf.worker.util.EmailUtil;
import com.ipd.jsf.worker.util.ReciveMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.ipd.jsf.common.enumtype.DataEnum.IfaceStatus;
import com.ipd.jsf.common.util.FileUtil;
import com.ipd.jsf.worker.common.SingleWorker;
import com.ipd.jsf.worker.service.JsfIfaceApplyService;
import com.ipd.jsf.worker.thread.audit.AuditEnum;
import com.ipd.jsf.worker.util.PropertyUtil;
import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Message;

public class ReceiveMailWorker extends SingleWorker {

    private static final Logger logger = LoggerFactory.getLogger(ReceiveMailWorker.class);

    private static final String HTMLTAG = "</?[^<]+>";

    @Autowired
    JsfIfaceApplyService jsfIfaceApplyService;

    @Autowired
    JsfUserDao jsfUserDao;

    private boolean doAudit(ReciveMail mail, String[] ifaces) throws Exception {
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
            ifaces = new String[infos.size()];
            for (int i = 0; i < infos.size(); i++) {
                ifaces[i] = infos.get(i).getInterfaceName();
            }

            Timestamp auditTime = new Timestamp(System.currentTimeMillis());

            jsfIfaceApplyService.batchAudit(ifaces, IfaceStatus.added.getValue(), uid, auditor, auditTime, null);
            String creator = infos.get(0).getCreator();
            User user = jsfUserDao.getUserByPin(creator);
            if (user != null && StringUtils.hasText(user.getMail())) {
                creatorMail = user.getMail();
            }
        } else {
            return true;
        }

        String text = buildAddOkText(infos, auditor);

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

    @Override
    public boolean run() {
        Store store = null;
        POP3Folder folder = null;
        try {
            Session session = EmailUtil.getSession();
            store = session.getStore(PropertyUtil.getProperties("mail.receive.protocol"));
            store.connect(PropertyUtil.getProperties("mail.receive.host"),
                    PropertyUtil.getProperties("mail.username"),
                    PropertyUtil.getProperties("mail.password"));

            folder = (POP3Folder) store.getFolder("INBOX");
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();
            logger.info("Mails' length: " + messages.length);
            for (int j = 0; j < messages.length; j++) {
                ReciveMail mail = new ReciveMail((POP3Message) messages[j]);
                try {
                    String subject = mail.getSubject().trim();

                    if (noRemove(subject)) {
                        if (subject.contains(EmailUtil.jsfPreSubject)) {
                            AbstractAuditor abstractAuditor = AbstractAuditor.getAuditAdapters().get(AuditEnum.INTERFACE_APPLY);
                            abstractAuditor.check(messages[j], mail);
                        } else if (subject.contains(EmailUtil.gatewayPreSubject)) {
                            AbstractAuditor abstractAuditor = AbstractAuditor.getAuditAdapters().get(AuditEnum.GATWAY_INVOKE_APPLY);
                            abstractAuditor.check(messages[j], mail);
                        } else {
                            logger.info("对邮件: {}不做处理", subject);
                        }
                    } else {
                        messages[j].setFlag(Flags.Flag.DELETED, true);
                        logger.info("主题不匹配, 删除邮件{}, {}", (j + 1), subject);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            return true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        } finally {
            try {
                folder.close(true);
            } catch (Exception e) {
            }

            try {
                store.close();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public String getWorkerType() {
        return "receiveMailWorker";
    }

    private boolean noRemove(String subject) {
        subject = subject.toUpperCase();
        List<String> noRemove = new ArrayList<String>(EmailUtil.noDelMails);
        for (String str : noRemove) {
            List<String> tmpList = Arrays.asList(str.split(","));
            for (String s1 : tmpList) {
                s1 = s1.trim().toUpperCase();
                if (subject.contains(s1)) {
                    return true;
                }
            }
        }
        return false;
    }


    private String buildAddOkText(List<JsfIfaceApply> bInfos, String auditor) {
        StringBuffer tableContent = new StringBuffer();
        for (int i = 0; i < bInfos.size(); i++) {
            JsfIfaceApply info = bInfos.get(i);
            tableContent.append("<DIV class=leftArea>")
                    .append("<TABLE>").append("<COLGROUP>")
                    .append("<COL width=\"20%\">")
                    .append("<COL width=\"80%\">").append("</COLGROUP>")
                    .append("<TBODY>").append("<TR>")
                    .append("<TH class=textR>接口名</TH><TD>")
                    .append(info.getInterfaceName()).append("</TD></TR>")
                    .append("<TR>").append("<TH class=textR>部门</TH><TD>")
                    .append(info.getDepartment()).append("</TD></TR>")
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

}
