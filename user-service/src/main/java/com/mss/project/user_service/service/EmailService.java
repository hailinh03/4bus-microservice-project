package com.mss.project.user_service.service;

import jakarta.mail.MessagingException;

public interface EmailService {

    void sendEmail(String to, String subject, String body) throws MessagingException;
}
