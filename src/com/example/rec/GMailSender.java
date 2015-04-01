package com.example.rec;

import java.io.*;
import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.Message;
import javax.mail.internet.*;

import android.os.*;
  
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
  
    public synchronized void sendMail(String subject, String body, String sender, String recipients,String filename) throws Exception {  
        MimeMessage message = new MimeMessage(session);  
       // DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));  
        message.setSender(new InternetAddress(sender));  
        message.setSubject(subject);  
      // message.setDataHandler(handler);  
        if (recipients.indexOf(',') > 0)  
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));  
        else  
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));  
        //텍스트와 첨부파일을 함께 전송하는 경우 create the message part
        BodyPart messageBodyPart = new MimeBodyPart();
        
        //Fill the message
        messageBodyPart.setText(body);
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);
        
        //part two is attachement
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
//        
        new TransportTask().execute(message);
 //       Transport.send(message);
        
        
    }  
   
    class TransportTask extends AsyncTask<MimeMessage, Void,Void>{
    	@Override
    	protected Void doInBackground(MimeMessage... params) {
    		try {
				Transport.send(params[0]);
			} catch (MessagingException e) {
				e.printStackTrace();
			}
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