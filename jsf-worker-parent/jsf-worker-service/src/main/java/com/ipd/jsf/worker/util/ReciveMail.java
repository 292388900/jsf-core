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
package com.ipd.jsf.worker.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.mail.BodyPart;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeUtility;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.base.Joiner;
import com.sun.mail.pop3.POP3Message;

public class ReciveMail {   
	private final static Logger logger = LoggerFactory.getLogger(ReciveMail.class);
    private POP3Message mimeMessage = null;   
    private String saveAttachPath = ""; //附件下载后的存放目录   
    private StringBuffer bodytext = new StringBuffer();//存放邮件内容   
  
    public ReciveMail(POP3Message mimeMessage) {   
        this.mimeMessage = mimeMessage;   
    }   
  
    
    public void delete() throws Exception{
    	// 标记为已读  
    	if(!mimeMessage.getFolder().isOpen()) //判断是否open   
    		mimeMessage.getFolder().open(Folder.READ_WRITE); //如果close，就重新open  
//    	mimeMessage.setFlag(Flags.Flag.SEEN, true);  
    	// 删除邮件  
    	mimeMessage.setFlag(Flags.Flag.DELETED, true);  
    	
    	mimeMessage.getFolder().close(true);
    }
    
    /**  
     * 获得发件人的地址和姓名  
     */  
    public String getFrom() throws Exception {   
        InternetAddress address[] = (InternetAddress[]) mimeMessage.getFrom();
        String from = address[0].getAddress();   
        if (from == null)   
            from = "";   
//        String personal = address[0].getPersonal();   
//        if (personal == null)   
//            personal = "";   
//        String fromaddr = personal + "<" + from + ">";   
//        return fromaddr;   
        return from;
    }   
  
    /**  
     * 获得邮件的收件人，抄送，和密送的地址和姓名，根据所传递的参数的不同 "to"----收件人 "cc"---抄送人地址 "bcc"---密送人地址  
     */  
    public String[] getMailAddress(RecipientType type) throws Exception {   
        String mailaddr = "";   
        InternetAddress[] address = null;   
        if (type.equals(RecipientType.TO) || type.equals(RecipientType.CC)|| type.equals(RecipientType.BCC)) {   
            if (type.equals(RecipientType.TO)) {   
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.TO);   
            } else if (type.equals(RecipientType.CC)) {   
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.CC);   
            } else {   
                address = (InternetAddress[]) mimeMessage.getRecipients(Message.RecipientType.BCC);   
            }   
            if (address != null) {   
                for (int i = 0; i < address.length; i++) {   
                    String email = address[i].getAddress();   
                    if (email == null)   
                        email = "";   
                    else {   
                        email = MimeUtility.decodeText(email);   
                        if(StringUtils.hasText(email)){
                        	 mailaddr += email +",";
                        }
                       
                    }   
//                    String personal = address[i].getPersonal();   
//                    if (personal == null)   
//                        personal = "";   
//                    else {   
//                        personal = MimeUtility.decodeText(personal);   
//                    }   
//                    String compositeto = personal + "<" + email + ">";   
//                    mailaddr += "," + compositeto;   
                }   
            }   
        } else {   
            throw new Exception("Error email address type!");   
        }   
        if(mailaddr.endsWith(",")){
        	mailaddr = mailaddr.substring(0, mailaddr.length()-1);
        }
        return Joiner.on(",").skipNulls().join(mailaddr.split(",")).split(",");   
    }   
  
    /**  
     * 获得邮件主题  
     */  
    public String getSubject() throws MessagingException {   
        String subject = "";   
        try {   
        	if(!mimeMessage.getFolder().isOpen()) //判断是否open   
        		mimeMessage.getFolder().open(Folder.READ_WRITE); //如果close，就重新open  
            subject = MimeUtility.decodeText(mimeMessage.getSubject());   
            if (subject == null)   
                subject = "";   
        } catch (Exception exce) {
        	logger.error(exce.getMessage(), exce);
        }   
        return subject;   
    }   
  
    /**  
     * 获得邮件发送日期  
     */  
    public String getSentDate() throws Exception {   
        Date sentdate = mimeMessage.getSentDate();   
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");   
        return format.format(sentdate);   
    }   
  
    /**  
     * 获得邮件正文内容  
     */  
    public String getBodyText() {   
    	try {
			setMailContent((Part)mimeMessage);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
        return bodytext.toString();   
    }   
  
    /**  
     * 解析邮件，把得到的邮件内容保存到一个StringBuffer对象中，解析邮件 主要是根据MimeType类型的不同执行不同的操作，一步一步的解析  
     */  
    public void setMailContent(Part part) throws Exception {   
        String contenttype = part.getContentType();   
        int nameindex = contenttype.indexOf("name");   
        boolean conname = false;   
        if (nameindex != -1)   
            conname = true;   
//        logger.info("CONTENTTYPE: " + contenttype);   
        if (part.isMimeType("text/plain") && !conname) {   
            bodytext.append((String) part.getContent());   
        } else if (part.isMimeType("text/html") && !conname) {   
            bodytext.append((String) part.getContent());   
        } else if (part.isMimeType("multipart/*")) {   
            Multipart multipart = (Multipart) part.getContent();   
            int counts = multipart.getCount();   
            for (int i = 0; i < counts; i++) {   
                setMailContent(multipart.getBodyPart(i));   
            }   
        } else if (part.isMimeType("message/rfc822")) {   
            setMailContent((Part) part.getContent());   
        } else {}   
    }   
  
    /**   
     * 判断此邮件是否需要回执，如果需要回执返回"true",否则返回"false"  
     */   
    public boolean getReplySign() throws MessagingException {   
        boolean replysign = false;   
        String needreply[] = mimeMessage   
                .getHeader("Disposition-Notification-To");   
        if (needreply != null) {   
            replysign = true;   
        }   
        return replysign;   
    }   
  
    /**  
     * 获得此邮件的Message-ID  
     */  
    public String getMessageId() throws MessagingException {   
        return mimeMessage.getMessageID();   
    }   
  
    /**  
     * 【判断此邮件是否已读，如果未读返回返回false,反之返回true】  
     */  
    public boolean isNew() throws MessagingException {   
        boolean isnew = false;   
        Flags flags = ((Message) mimeMessage).getFlags();   
        Flags.Flag[] flag = flags.getSystemFlags();   
        logger.info("flags's length: " + flag.length);   
        for (int i = 0; i < flag.length; i++) {   
            if (flag[i] == Flags.Flag.SEEN) {   
                isnew = true;   
                logger.info("seen Message.......");   
                break;   
            }   
        }   
        return isnew;   
    }   
  
    /**  
     * 判断此邮件是否包含附件  
     */  
    public boolean isContainAttach(Part part) throws Exception {   
        boolean attachflag = false;   
        String contentType = part.getContentType();   
        if (part.isMimeType("multipart/*")) {   
            Multipart mp = (Multipart) part.getContent();   
            for (int i = 0; i < mp.getCount(); i++) {   
                BodyPart mpart = mp.getBodyPart(i);   
                String disposition = mpart.getDisposition();   
                if ((disposition != null)   
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition   
                                .equals(Part.INLINE))))   
                    attachflag = true;   
                else if (mpart.isMimeType("multipart/*")) {   
                    attachflag = isContainAttach((Part) mpart);   
                } else {   
                    String contype = mpart.getContentType();   
                    if (contype.toLowerCase().indexOf("application") != -1)   
                        attachflag = true;   
                    if (contype.toLowerCase().indexOf("name") != -1)   
                        attachflag = true;   
                }   
            }   
        } else if (part.isMimeType("message/rfc822")) {   
            attachflag = isContainAttach((Part) part.getContent());   
        }   
        return attachflag;   
    }   
  
    /**   
     * 【保存附件】   
     */   
    public void saveAttachMent(Part part) throws Exception {   
        String fileName = "";   
        if (part.isMimeType("multipart/*")) {   
            Multipart mp = (Multipart) part.getContent();   
            for (int i = 0; i < mp.getCount(); i++) {   
                BodyPart mpart = mp.getBodyPart(i);   
                String disposition = mpart.getDisposition();   
                if ((disposition != null)   
                        && ((disposition.equals(Part.ATTACHMENT)) || (disposition   
                                .equals(Part.INLINE)))) {   
                    fileName = mpart.getFileName();   
                    if (fileName.toLowerCase().indexOf("gb2312") != -1) {   
                        fileName = MimeUtility.decodeText(fileName);   
                    }   
                    saveFile(fileName, mpart.getInputStream());   
                } else if (mpart.isMimeType("multipart/*")) {   
                    saveAttachMent(mpart);   
                } else {   
                    fileName = mpart.getFileName();   
                    if ((fileName != null)   
                            && (fileName.toLowerCase().indexOf("GB2312") != -1)) {   
                        fileName = MimeUtility.decodeText(fileName);   
                        saveFile(fileName, mpart.getInputStream());   
                    }   
                }   
            }   
        } else if (part.isMimeType("message/rfc822")) {   
            saveAttachMent((Part) part.getContent());   
        }   
    }   
  
    /**   
     * 【设置附件存放路径】   
     */   
  
    public void setAttachPath(String attachpath) {   
        this.saveAttachPath = attachpath;   
    }   
  
  
    /**  
     * 【获得附件存放路径】  
     */  
    public String getAttachPath() {   
        return saveAttachPath;   
    }   
  
    /**  
     * 【真正的保存附件到指定目录里】  
     */  
    private void saveFile(String fileName, InputStream in) throws Exception {   
        String osName = System.getProperty("os.name");   
        String storedir = getAttachPath();   
        String separator = "";   
        if (osName == null)   
            osName = "";   
        if (osName.toLowerCase().indexOf("win") != -1) {   
            separator = "\\";  
            if (storedir == null || storedir.equals(""))  
                storedir = "c:\\tmp";  
        } else {  
            separator = "/";  
            storedir = "/tmp";  
        }  
        File storefile = new File(storedir + separator + fileName);  
        logger.info("storefile's path: " + storefile.toString());  
        // for(int i=0;storefile.exists();i++){  
        // storefile = new File(storedir+separator+fileName+i);  
        // }  
        BufferedOutputStream bos = null;  
        BufferedInputStream bis = null;  
        try {  
            bos = new BufferedOutputStream(new FileOutputStream(storefile));  
            bis = new BufferedInputStream(in);  
            int c;  
            while ((c = bis.read()) != -1) {  
                bos.write(c);  
                bos.flush();  
            }  
        } catch (Exception exception) {  
            exception.printStackTrace();  
            throw new Exception("文件保存失败!");  
        } finally {  
            bos.close();  
            bis.close();  
        }  
    }  
    
    public void setMimeMessage(POP3Message mimeMessage) {   
        this.mimeMessage = mimeMessage;   
    }   
 
}