package com.expense.ExpenseTracker.messaging;

import com.expense.ExpenseTracker.dto.EmailMessage;
import com.expense.ExpenseTracker.service.EmailService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmailConsumer {

    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private UserDetailsService userDetailsService;
    private EmailMessage emailMessage;

    @RabbitListener(queues = "${rabbitmq.email.queue:email.queue}")
    public void receiveEmail(String message) {
        try {
            EmailMessage emailMessage = objectMapper.readValue(message, EmailMessage.class);
            emailService.sendEmailWithAttachment(
                    emailMessage.getTo(),
                    emailMessage.getSubject(),
                    emailMessage.getContent(),
                    emailMessage.getAttachment()
            );
        } catch (Exception e) {
            log.error("Error processing email", e);
            throw new RuntimeException("Failed to process email", e);
        }
    }

}
