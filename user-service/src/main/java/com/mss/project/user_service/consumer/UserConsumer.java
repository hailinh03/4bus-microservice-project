package com.mss.project.user_service.consumer;

import com.mss.project.user_service.service.EmailService;
import com.mss.project.user_service.utils.EmailUtils;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = {"${rabbitmq.queue.emailQueue}"})
    public void registerConsumer(String userEmail) throws MessagingException {
        System.out.println("Received registration message: " + userEmail);
        emailService.sendEmail(userEmail, EmailUtils.subjectRegister(), EmailUtils.getWelcomeEmailContent());

    }
}
