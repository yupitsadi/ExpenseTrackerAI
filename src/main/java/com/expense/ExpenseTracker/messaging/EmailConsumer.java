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

    private final JavaMailSender mailSender;
    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private UserDetailsService userDetailsService;
    private EmailMessage emailMessage;

    @RabbitListener(queues = "${rabbitmq.email.queue:email.queue}")
    public void receiveEmail(String message) {
        try {
            log.info("Email Sent: {}", message);

            RabbitMQProducer.EmailMessage emailMessage = objectMapper.readValue(
                message,
                RabbitMQProducer.EmailMessage.class
            );

            // Generate the PDF report
            byte[] pdfBytes = yourPdfService.generateMonthlyReport(emailMessage.getTo());

            String subject = "Your Monthly Expense Report";
            String body = "Please find attached your monthly expense report.";

            emailService.sendEmailWithAttachment(emailMessage.getTo(), subject, body, pdfBytes);


        } catch (Exception e) {
            log.error("Error processing email: {}", e.getMessage(), e);
            // Consider implementing a retry mechanism or dead-letter queue for failed messages
            throw new RuntimeException("Failed to process email", e);
        }
    }
}
