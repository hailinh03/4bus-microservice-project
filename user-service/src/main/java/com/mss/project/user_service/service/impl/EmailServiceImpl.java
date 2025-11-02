package com.mss.project.user_service.service.impl;

import com.mss.project.user_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    @Value("${spring.mail.username}")
    private String systemMail;

    @Override
    public void sendEmail(String to, String subject, String body) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        mimeMessage.setFrom(systemMail);
        mimeMessage.setRecipients(MimeMessage.RecipientType.TO, to);
        mimeMessage.setSubject(subject);
        mimeMessage.setContent(body, "text/html; charset=utf-8");

        javaMailSender.send(mimeMessage);
    }
}
