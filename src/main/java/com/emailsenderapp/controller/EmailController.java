package com.emailsenderapp.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.emailsenderapp.helper.CustomResponse;
import com.emailsenderapp.pojo.EmailRequest;
import com.emailsenderapp.service.EmailService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/email/v1")
public class EmailController {

  @Autowired
  private EmailService emailService;

  @GetMapping("/")
  public ResponseEntity<CustomResponse> DemoTest(){
    return ResponseEntity.ok(CustomResponse
        .builder()
        .message("API Work successfully")
        .httpStatus(HttpStatus.OK)
        .success(true)
        .build());
  
  }
    

  @PostMapping("/send")
  public ResponseEntity<CustomResponse> sendEmail(@RequestBody EmailRequest request) {

    emailService.sendEmailWithHtml(request.getTo(), request.getSubject(), request.getMessage());

    return ResponseEntity.ok(CustomResponse
        .builder()
        .message("Email sent successfully")
        .httpStatus(HttpStatus.OK)
        .success(true)
        .build());
  }

  @PostMapping("/send-email-file")
  public ResponseEntity<CustomResponse> sendEmailWithAttachment(@RequestPart("request") EmailRequest request,
      @RequestPart("file") MultipartFile file) throws IOException {

    emailService.sendEmailWithAttachment(request.getTo(), request.getSubject(), request.getMessage(),
        file.getInputStream());

    return ResponseEntity.ok(CustomResponse.builder()
        .message("Email sent succrssfully")
        .httpStatus(HttpStatus.OK)
        .success(true)
        .build());
  }

}
