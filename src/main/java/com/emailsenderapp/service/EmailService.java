package com.emailsenderapp.service;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import com.emailsenderapp.helper.Messages;

public interface EmailService {

  // send email to single person
  void sendEmail(String to, String subject, String message);

  // send email to multiple persons
  void sendEmail(String[] to, String subject, String message);

  // send email with html content
  void sendEmailWithHtml(String to, String subject, String htmlContent);

  // send email with attachment
  void sendEmailWithAttachment(String to, String subject, String message, File attachmentPath);

  // send email with attachment
  void sendEmailWithAttachment(String to, String subject, String message, InputStream is);

  // email inbox mai lena hai

  List<Messages> getInboxMessages();

}
