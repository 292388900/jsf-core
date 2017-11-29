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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Flags;
import javax.mail.Message;

import com.ipd.jsf.worker.util.EmailUtil;
import com.ipd.jsf.worker.util.ReciveMail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.ipd.jsf.worker.util.PropertyUtil;

public abstract class AbstractAuditor {

    private final static Logger logger = LoggerFactory.getLogger(AbstractAuditor.class);

    private static Map<AuditEnum, AbstractAuditor> auditAdapters = new HashMap<AuditEnum, AbstractAuditor>();

    private static final String HTMLTAG = "</?[^<]+>";

    public AbstractAuditor() {
    }

    public void check(Message message, ReciveMail mail) throws Exception {
        String from = mail.getFrom().trim();
        String subject = mail.getSubject().trim();
        List<String> allowMails = Arrays.asList(PropertyUtil.getProperties("audit.allow.mail").split(","));
        if (!from.equals(EmailUtil.sender) && !allowMails.contains(from)) {
            logger.warn("{}没有审批权限", from);
            logger.info("审核人无权限, 删除邮件: {}", subject);
            message.setFlag(Flags.Flag.DELETED, true);
            return;
        }

        String content = mail.getBodyText();
        if (content != null) {
            content = content.replaceAll(HTMLTAG, "");
            String[] lines = content.split("\r\n");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i];
                if (StringUtils.hasText(line)) {
                    line = line.trim();
                    if (line.equals("同意") || line.toUpperCase().equals("OK")
                            || line.substring(0, 2).equals("同意")
                            || line.toUpperCase().substring(0, 2).equals("OK")
                            ) {
                        if (doAudit(mail, null)) {
                            logger.info("审核完毕, 删除邮件 {}", subject);
                            message.setFlag(Flags.Flag.DELETED, true);
                        }
                    } else if (line.equals("不同意") || line.toUpperCase().equals("NO")
                            || line.substring(0, 3).equals("不同意")
                            || line.toUpperCase().substring(0, 2).equals("NO")
                            ) {
                        String [] msgs = line.split(":");
                        String rejectReason = "";
                        if (msgs != null && msgs.length >= 2) {
                            rejectReason = msgs[1];
                            rejectReason = com.ipd.jsf.worker.util.StringUtils.filterString(rejectReason);
                        }
                        if (rejectReason == null) {
                            rejectReason = "";
                        }
                        if (doAudit(mail, rejectReason)) {
                            logger.info("驳回完毕, 删除邮件 {}", subject);
                            message.setFlag(Flags.Flag.DELETED, true);
                        }
                    } else {
                        logger.warn("邮件: {}的审批内容:[{}]格式错误, 请回复'同意'或者'OK'", subject, line);
                        logger.info("删除邮件: {}", subject);
                        message.setFlag(Flags.Flag.DELETED, true);
                    }

                    break;
                }
            }
        }
    }

    public abstract boolean doAudit(ReciveMail mail, String rejectReason) throws Exception;

    public static Map<AuditEnum, AbstractAuditor> getAuditAdapters() {
        return auditAdapters;
    }

    public static void setAuditAdapters(Map<AuditEnum, AbstractAuditor> auditAdapters) {
        AbstractAuditor.auditAdapters = auditAdapters;
    }

}