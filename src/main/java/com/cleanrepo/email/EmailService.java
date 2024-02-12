package com.cleanrepo.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

@Service
public class EmailService {
    @Value("${spring.mail.host}")
    private String host;

    @Value("${spring.mail.port}")
    private Integer port;

    @Value("${spring.mail.username}")
    private String username;

    @Value("${spring.mail.password}")
    private String password;

    @Async
    public void sendEmail(String email, String from, String text) {

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.imap.ssl.enable", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.user", username);
        props.put("mail.smtp.email", username);
        props.put("mail.smtp.password", password);

        // Get the Session object.
        Session session = Session.getInstance(props,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password);
                    }
                });
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));

            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(email));

            message.setSubject("Set password");

            MimeMultipart multipart = new MimeMultipart("related");

            BodyPart messageBodyPart = new MimeBodyPart();
            String html = "";
            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream("emailtemplate/verification_mail.html");
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line;
                while ((line = reader.readLine()) != null) {
                    html += line;
                }
                reader.close();
            } catch (IOException e) {
                System.out.println("Error reading HTML file: " + e.getMessage());
            }
            html = html.replaceAll("verificationLink", text);
            setEmailConfigData(message, multipart, messageBodyPart, html);
        } catch (MessagingException ex) {
            System.out.println(ex);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private void setEmailConfigData(Message message, MimeMultipart multipart, BodyPart messageBodyPart, String html)
            throws MessagingException, IOException {
        messageBodyPart.setContent(html, "text/html; charset=UTF-8");

        multipart.addBodyPart(messageBodyPart);
        message.setContent(multipart);

        Transport.send(message);
    }
}
