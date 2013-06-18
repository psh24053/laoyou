package com.shntec.saf;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.util.ByteArrayDataSource;

/**
 * 邮件发送工具
 * @author Panshihao
 *
 */
public class SAFMailSender extends Authenticator {

	private String host;   
    private String userName;  
    private String password;  
      
    private Session session;  
      
    public SAFMailSender(String smtphost, String userName, String password) {
    	this.host = smtphost;
        this.userName = userName;  
        this.password = password;  
          
        initialize(); //初始化  
    }  
      
    private void initialize() {  
        Properties props = new Properties();  
        props.setProperty("mail.transport.protocol", "smtp");  
        props.setProperty("mail.host", host);  
        props.put("mail.smtp.auth", true);  
          
        session = Session.getDefaultInstance(props, this);  
    }  
  
    @Override  
    protected PasswordAuthentication getPasswordAuthentication() {  
        return new PasswordAuthentication(userName, password);  
    }  
      
    /** 
     * 发送Email 
     * @param subject 标题 
     * @param body 内容 
     * @param sender 发送者 
     * @param recipients 接收者 
     * @throws MessagingException  
     * @throws AddressException  
     * */  
    public synchronized void sendMail(String subject, String body, String sender, String recipients) throws AddressException, MessagingException {  
        MimeMessage message = new MimeMessage(session);  
        DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));  
          
        /* 
         * 设置MIME消息 
         * */  
        message.setSender(new InternetAddress(sender));  
        message.setSubject(subject);  
        message.setDataHandler(handler);  
        if(recipients.contains(",")) {  
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));  
        } else {  
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));  
        }  
          
        Transport.send(message); //发送  
    }  
}
