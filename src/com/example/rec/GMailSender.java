package com.example.rec;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import android.os.AsyncTask;
  
public class GMailSender extends javax.mail.Authenticator {  
    private String mailhost = "smtp.gmail.com";  
    private String user;  
    private String password;  
    private Session session;  
  
    public GMailSender(String user, String password) {
        this.user = user;  
        this.password = password;  
  
        Properties props = new Properties();  
        props.setProperty("mail.transport.protocol", "smtp");  
        props.setProperty("mail.host", mailhost);  
        props.put("mail.smtp.auth", "true");  
        props.put("mail.smtp.port", "465");  
        props.put("mail.smtp.socketFactory.port", "465");  
        props.put("mail.smtp.socketFactory.class",  
                "javax.net.ssl.SSLSocketFactory");  
        props.put("mail.smtp.socketFactory.fallback", "false");  
        props.setProperty("mail.smtp.quitwait", "false");  
  
        session = Session.getDefaultInstance(props, this);  
    }  
  
    
    protected PasswordAuthentication getPasswordAuthentication() {  
        return new PasswordAuthentication(user, password);  
    }  
  
    public synchronized void sendMail(String subject, String sender, String recipients,String filename,String filename2) throws Exception {  
        MimeMessage message = new MimeMessage(session);  
        message.setSender(new InternetAddress(sender));  
        message.setSubject(subject);  
        if (recipients.indexOf(',') > 0)  
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));  
        else  
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));  
        //텍스트와 첨부파일을 함께 전송하는 경우 create the message part
        BodyPart messageBodyPart = new MimeBodyPart();
        BodyPart messageBodyPart2 = new MimeBodyPart();
        
        //Fill the message
        Multipart multipart = new MimeMultipart();
        
        //part two is attachement
        if(!filename2.equals("0")){
        	File file2 = new File(filename2);
        	FileDataSource fds2 = new FileDataSource(file2);
        	messageBodyPart2.setDataHandler(new DataHandler(fds2));
        	messageBodyPart2.setFileName(fds2.getName());
        	multipart.addBodyPart(messageBodyPart2);
        }
        File file = new File(filename);
        FileDataSource fds = new FileDataSource(file); 
        messageBodyPart.setDataHandler(new DataHandler(fds));
        messageBodyPart.setFileName(fds.getName());
        multipart.addBodyPart(messageBodyPart);
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);   
        //put parts in message
        message.setContent(multipart);     
        new TransportTask().execute(message);
    }
   
    // TODO 토스트 메시지로 출력
    class TransportTask extends AsyncTask<MimeMessage, Void,Void>{
    	protected Void doInBackground(MimeMessage... params) {
    		try {
				Transport.send(params[0]);
				System.out.println("파일 전송 성공!");
			} catch (MessagingException e) { System.out.println("파일 전송 실패!!"); }
    		return null;
    	}
       }
   
  
    public class ByteArrayDataSource implements DataSource {  
        private byte[] data;  
        private String type;  
  
        public ByteArrayDataSource(byte[] data, String type) {  
            super();  
            this.data = data;  
            this.type = type;  
        }  
  
        public ByteArrayDataSource(byte[] data) {  
            super();  
            this.data = data;  
        }  
  
        public void setType(String type) {  
            this.type = type;  
        }  
  
        public String getContentType() {  
            if (type == null)  
                return "application/octet-stream";  
            else  
                return type;  
        }  
  
        public InputStream getInputStream() throws IOException {  
            return new ByteArrayInputStream(data);  
        }  
  
        public String getName() {  
            return "ByteArrayDataSource";  
        }  
  
        public OutputStream getOutputStream() throws IOException {  
            throw new IOException("Not Supported");  
        }  
    }  
}