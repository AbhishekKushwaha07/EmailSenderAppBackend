package com.emailsenderapp.service.impl;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.emailsenderapp.helper.Messages;
import com.emailsenderapp.service.EmailService;

import jakarta.mail.BodyPart;
import jakarta.mail.Folder;
import jakarta.mail.Message;
import jakarta.mail.Multipart;
import jakarta.mail.Part;
import jakarta.mail.Session;
import jakarta.mail.Store;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

  @Value("${spring.mail.username}")
  private String usernameEmail;

  @Autowired
  private JavaMailSender mailSender;

  private Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

  @Override
  public void sendEmail(String to, String subject, String message) {

    try {

      SimpleMailMessage mailMessage = new SimpleMailMessage();
      mailMessage.setTo(to);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);
      mailMessage.setFrom(usernameEmail);

      mailSender.send(mailMessage);

      logger.info("Email sent successfully to {}", to);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendEmail(String[] to, String subject, String message) {

    try {

      SimpleMailMessage mailMessage = new SimpleMailMessage();

      mailMessage.setTo(to);
      mailMessage.setSubject(subject);
      mailMessage.setText(message);
      mailMessage.setFrom(usernameEmail);

      mailSender.send(mailMessage);

      logger.info("message sent successfully to multiple recipients");

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendEmailWithHtml(String to, String subject, String htmlContent) {

    MimeMessage mimeMessage = mailSender.createMimeMessage();

    try {
      // set mime message helper
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "utf-8");
      mimeMessageHelper.setTo(to);
      mimeMessageHelper.setSubject(subject);
      mimeMessageHelper.setText(htmlContent, true);
      mimeMessageHelper.setFrom(usernameEmail);

      mailSender.send(mimeMessage);

      logger.info("htmlContent sent successfully to {}", to);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendEmailWithAttachment(String to, String subject, String message, File file) {

    MimeMessage mimeMessage = mailSender.createMimeMessage();
    try {

      // set mime message helper
      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);
      mimeMessageHelper.setTo(to);
      mimeMessageHelper.setSubject(subject);
      mimeMessageHelper.setText(message);
      FileSystemResource fileSystemResource = new FileSystemResource(file);
      mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), fileSystemResource);
      mimeMessageHelper.setFrom(usernameEmail);

      mailSender.send(mimeMessage);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Override
  public void sendEmailWithAttachment(String to, String subject, String message, InputStream is) {

    MimeMessage mimeMessage = mailSender.createMimeMessage();
    try {

      MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true);

      mimeMessageHelper.setTo(to);
      mimeMessageHelper.setSubject(subject);
      mimeMessageHelper.setText(message, true);
      mimeMessageHelper.setFrom(usernameEmail);

      File file = new File("src/main/resources/email/test.jpg");
      Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
      FileSystemResource fileSystemResource = new FileSystemResource(file);

      mimeMessageHelper.addAttachment(fileSystemResource.getFilename(), file);

      mailSender.send(mimeMessage);

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Value("${mail.store.protocol}")
  String protocol;

  @Value("${mail.imaps.host}")
  String host;

  @Value("${mail.imaps.port}")
  String port;

  @Value("${spring.mail.username}")
  String username;

  @Value("${spring.mail.password}")
  String password;

  @Override
  public List<Messages> getInboxMessages() {

    // code to recive all message : get all email messages

    try {

      Properties properties = new Properties();
      properties.setProperty("mail.store.protocol", protocol);
      properties.setProperty("mail.imaps.host", host);
      properties.setProperty("mail.imaps.port", port);

      Session session = Session.getDefaultInstance(properties);

      Store store = session.getStore();
      store.connect(username, password);
      Folder inbox = store.getFolder("INBOX");
      inbox.open(Folder.READ_ONLY);
      Message[] message = inbox.getMessages();

      List<Messages> lst = new ArrayList<>();

      for (Message m : message) {

        String content = getContentFromEmailMessage(m);
        List<String> files = getFilesFromEmailMessage(m);

        lst.add(Messages.builder()
            .subject(m.getSubject())
            .content(content)
            .file(files)
            .build());
      }

      return lst;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  private List<String> getFilesFromEmailMessage(Message m) {

    List<String> files = new ArrayList<>();

    try {

      if (m.isMimeType("multipart/*")) {

        Multipart part = (Multipart) m.getContent();

        for (int i = 0; i < part.getCount(); i++) {
          BodyPart bodyPart = part.getBodyPart(i);

          if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {

            InputStream is = bodyPart.getInputStream();

            File file = new File("src/main/resources/email/" + bodyPart.getFileName());

            Files.copy(is, file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            files.add(file.getAbsolutePath());

          }
        }
      }

      return files;

    } catch (Exception e) {
      e.printStackTrace();
      files.clear();
      return files;
    }

  }

  private String getContentFromEmailMessage(Message m) {

    try {

      if (m.isMimeType("text/plain") || m.isMimeType("text/html")) {

        String content = (String) m.getContent();
        return content;

      } else if (m.isMimeType("multipart/*")) {
        Multipart part = (Multipart) m.getContent();

        for (int i = 0; i < part.getCount(); i++) {

          BodyPart bodyPart = part.getBodyPart(i);

          if (bodyPart.isMimeType("text/plain")) {
            String content = (String) bodyPart.getContent();
            return content;
          }

        }

      }
      return null;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }

  }

}
