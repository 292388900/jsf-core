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

import com.sun.mail.pop3.POP3Folder;
import com.sun.mail.pop3.POP3Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;
import java.util.*;

/**
 * 该类继承邮件报警功能所有工具方法 Title: <br>
 *
 */
public class EmailUtil {
	private static Logger logger = LoggerFactory.getLogger(EmailUtil.class);
	public final static String MediaType_Plain = "text/plain;charset=UTF-8";
	public final static String MediaType_Html = "text/html;charset=UTF-8";
	
	public final static String sender = PropertyUtil.getProperties("mail.sender.address");
	public final static String adminMail = PropertyUtil.getProperties("receiver.mail.address");
	public final static String safPreSubject = PropertyUtil.getProperties("mail.saf.Subject");
	public final static String jsfPreSubject = PropertyUtil.getProperties("mail.jsf.Subject");
	public final static String gatewayPreSubject = PropertyUtil.getProperties("mail.gateway.Subject");
	public final static String subjectSplit = " -- ";
	public static List<String> noDelMails = new ArrayList<String>();
	public final static String javaCmdPre = PropertyUtil.getProperties("java.pre.cmd");
	private EmailUtil() {
	}

	private static Properties p;// 构建发送邮件相关属性
	static {
		p = new Properties();
		p.put("mail.smtp.auth", PropertyUtil.getProperties("mail.smtp.auth"));
		p.put("mail.transport.protocol", PropertyUtil.getProperties("mail.transport.protocol"));
		p.put("mail.smtp.host", PropertyUtil.getProperties("mail.smtp.host"));
		p.put("mail.smtp.port", PropertyUtil.getProperties("mail.smtp.port"));
		
		noDelMails = Arrays.asList(PropertyUtil.getProperties("mail.nodel").split(","));
	}
	
	public static Session getSession(){
		return Session.getInstance(p);
	}
	
	
	public static List<ReciveMail> receiveMail() {
		Store store = null;
		POP3Folder folder = null;
		List<ReciveMail> result = new ArrayList<ReciveMail>();
		try {
//			Session session = getSession();
			Session session = Session.getDefaultInstance(new Properties());
			
			URLName urln = new URLName(
					PropertyUtil.getProperties("mail.receive.protocol"),
					PropertyUtil.getProperties("mail.receive.host"),
					Integer.parseInt(PropertyUtil.getProperties("mail.receive.port")),   
					null,
					PropertyUtil.getProperties("mail.username"),
					PropertyUtil.getProperties("mail.password"));
			store = session.getStore(urln);   
	        store.connect();   

			folder = (POP3Folder) store.getFolder("INBOX");
			folder.open(Folder.READ_WRITE);

			Message[] message = folder.getMessages();
			logger.info("Mails' length: " + message.length);

			for (int i = message.length - 1; i >= 0; i--) {
				ReciveMail pmm = new ReciveMail((POP3Message) message[i]);
				 logger.info("Message " + i + " to: "+ pmm.getMailAddress(RecipientType.TO));
				result.add(pmm);
			}

			return result;
		} catch (NoSuchProviderException e) {
			logger.error("接收邮件异常: " + e.getMessage(), e);
		} catch (MessagingException e) {
			logger.error("接收邮件异常: " + e.getMessage(), e);
		} catch (Exception e) {
			logger.error("接收邮件异常: " + e.getMessage(), e);
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
		
		return null;
	}
	
	/**
	 * 发送邮件
	 * @param text 邮件内容
	 * @param to 邮箱地址 多个邮箱地址
	 * @param cc 抄送地址
	 * @param bcc 密送地址
	 * @param subject 邮件主题
	 */
	public static void sendEmail(String text, String[] to, String[] cc, String[] bcc, String subject) {
		if(EnvironmentHelper.isJcloudEnvironment()){
			return;
		}
		sendEmail(text, to, cc, bcc, subject, MediaType_Html);
	}
	
	public static void sendEmail(String text, String[] to, String[] cc, String[] bcc, String subject, String mediaType) {
		if(EnvironmentHelper.isJcloudEnvironment()){
			return;
		}
		Transport tran = null;
		try {
			logger.info("建立邮件会话..");
			// 建立会话
			Session session = getSession();
			Message msg = new MimeMessage(session); // 建立信息
			msg.setFrom(new InternetAddress(sender)); // 发件人
			
			if(to != null && to.length != 0){
				InternetAddress[] tos = new InternetAddress[to.length];
				for (int i = 0; i < to.length; i++) {
					tos[i] = new InternetAddress(to[i]);
				}
				msg.setRecipients(RecipientType.TO, tos); // 收件人
			}
			
			if(cc != null && cc.length != 0){
				InternetAddress[] ccs = new InternetAddress[cc.length];
				for (int i = 0; i < cc.length; i++) {
					ccs[i] = new InternetAddress(cc[i]);
				}
				msg.setRecipients(RecipientType.CC, ccs); // 抄送
			}
			
			if(bcc != null && bcc.length != 0){
				InternetAddress[] bccs = new InternetAddress[bcc.length];
				for (int i = 0; i < bcc.length; i++) {
					bccs[i] = new InternetAddress(bcc[i]);
				}
				msg.setRecipients(RecipientType.BCC, bccs); // 密送
			}
			
			msg.setSentDate(new Date()); // 发送日期
			msg.setSubject(subject); // 主题
			//msg.setText(text); 
			msg.setContent(text, mediaType);
			logger.info("连接邮件服务器..");
			// 邮件服务器进行验证
			tran = session.getTransport(PropertyUtil.getProperties("mail.transport.protocol"));
			tran.connect(PropertyUtil.getProperties("mail.smtp.host"),
					PropertyUtil.getProperties("mail.username"),
					PropertyUtil.getProperties("mail.password"));
			
			logger.info("发送邮件: {}, content: {}", subject, text);
			tran.sendMessage(msg, msg.getAllRecipients()); // 发送
			logger.info("邮件发送成功");
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} finally {
			try {
				if(tran != null){
					tran.close();
				}
			} catch (MessagingException e) {
			}
		}
	}


}
