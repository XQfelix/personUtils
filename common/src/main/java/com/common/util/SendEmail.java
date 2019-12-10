package com.common.util;

import com.sun.mail.util.MailSSLSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Properties;

/**
 * @Title smtp协议方式发送邮件
 * @author GQ_Yin
 * */
public class SendEmail {
	private static Logger logger = LoggerFactory.getLogger(SendEmail.class);
    private String protocol;// 协议
    private String host;// 地址"smtp.exmail.qq.com"
    private String port;// 端口 = "465"
    private String account;// 用户名 = "yinguoqiang@uinnova.com";
    private String pass;// 发件人密码 = "123456";
    private String personal;// 发件人别名，不需要设为空串或null
    
    /**
     * 初始化方法
     * @param protocol 邮件发送协议
     * @param host    邮件发送的smtp地址
     * @param port    smtp服务器端口号
     * @param account    发件人邮箱用户名
     * @param pass      发件人邮箱密码
     * @param personal  发件人别名,不需要设为空串或者null
     */
    public SendEmail(String protocol, String host, String port, String account, String pass, String personal) {
    	this.protocol = protocol;
    	this.host = host;
    	this.port = port;
    	this.account = account;
    	this.pass = pass;
    	this.personal = personal;
	}
    
 // 权限认证
    static class MyAuthenricator extends Authenticator {
        String u = null;
        String p = null;
 
        public MyAuthenricator(String u, String p) {
            this.u = u;
            this.p = p;
        }
        @Override
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(u, p);
        }
    }
    
    
    /**
     * 发送邮件 工具 方法
     *
     * @param recipients 收件人(多个用英文逗号隔开)
     * @param subject    主题
     * @param emailMessage 内容
     * @param fileStr    附件路径,无附件时传null
     * @param sslFlag    是否进行SSL验证,参数true/false
     * @return true/false 发送成功
     */
    public boolean sendEmail(String recipients, String subject, String emailMessage, String fileStr, String sslFlag) {
        Properties prop = new Properties();
        //协议
        prop.setProperty("mail.transport.protocol", protocol);
        //服务器
        prop.setProperty("mail.smtp.host", host);
        //端口
        prop.setProperty("mail.smtp.port", port);
        //使用smtp身份验证
        prop.setProperty("mail.smtp.auth", "true");
        //是否开启安全协议,使用SSL，企业邮箱必需！
        MailSSLSocketFactory mailSSLSocketFactory = null;
        if(sslFlag.equals("true")) {
            try {
                mailSSLSocketFactory = new MailSSLSocketFactory();
                mailSSLSocketFactory.setTrustAllHosts(true);
            } catch (GeneralSecurityException e1) {
                e1.printStackTrace();
            }
            prop.put("mail.smtp.ssl.enable", "true");
            prop.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);
        }
        Session session = Session.getDefaultInstance(prop, new MyAuthenricator(account, pass));
        session.setDebug(true);
        MimeMessage mimeMessage = new MimeMessage(session);
        try {
            //发件人
            if (StringUtils.isNotBlank(personal))
                mimeMessage.setFrom(new InternetAddress(account, personal));//设置发件人的别名
            else
                mimeMessage.setFrom(new InternetAddress(account));//如果不需要就省略
            //收件人
            mimeMessage.addRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients.replaceAll("\\s*", "")));
//          mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipients));
            
            //主题
            mimeMessage.setSubject(subject);
            //时间
            mimeMessage.setSentDate(new Date());
            //容器类，可以包含多个MimeBodyPart对象
            Multipart mp = new MimeMultipart();
 
            //MimeBodyPart可以包装文本，图片，附件
            MimeBodyPart body = new MimeBodyPart();
            //HTML正文
            body.setContent(emailMessage, "text/html; charset=UTF-8");
            mp.addBodyPart(body);
 
            //添加图片&附件
            if(StringUtils.isNotBlank(fileStr)){
                body = new MimeBodyPart();
                body.attachFile(fileStr);
                mp.addBodyPart(body);
            }
 
            //设置邮件内容
            mimeMessage.setContent(mp);
            //仅仅发送文本
            //mimeMessage.setText(content);
            mimeMessage.saveChanges();
            Transport.send(mimeMessage);
            // 发送成功
            logger.info(">>>>>>>>>>>>>>>>>>>>>>>邮件发送成功>>>>>>>>>>>>>>>>>>>>>>>>>");
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * 调用参考示例
     * */
	public static void main(String[] args) throws MessagingException, IOException {
		//邮件发件人相关设置
	 SendEmail se = new SendEmail("smtp", "smtp.exmail.qq.com", "465", "yinguoqiang@uinnova.com", "1111", "Tarsier");
	 //邮件收件人相关设置
	 se.sendEmail("yinguoqiang@uinnova.com", "邮件测试", "邮sadfa件 <br>测试", null, "true");
	}
}
