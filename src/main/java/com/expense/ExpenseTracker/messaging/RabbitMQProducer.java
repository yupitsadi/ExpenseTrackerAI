package com.expense.ExpenseTracker.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import static com.expense.ExpenseTracker.config.RabbitMQConfig.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class RabbitMQProducer implements MessageProducer {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void sendNotification(String message) {
        try {
            log.info("Sending notification: {}", message);
            rabbitTemplate.convertAndSend(NOTIFICATION_EXCHANGE, NOTIFICATION_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Error sending notification: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to send notification", e);
        }
    }

    @Override
    public void sendEmail(String email, String subject, String content) {
        try {
            EmailMessage emailMessage = new EmailMessage(email, subject, content);
            String message = objectMapper.writeValueAsString(emailMessage);
            log.info("Sending email to: {}", email);
            rabbitTemplate.convertAndSend(EMAIL_EXCHANGE, EMAIL_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Error sending email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }

    @Override
    public void sendAnalytics(String eventType, String data) {
        try {
            AnalyticsMessage analyticsMessage = new AnalyticsMessage(eventType, data);
            String message = objectMapper.writeValueAsString(analyticsMessage);
            log.info("Sending analytics event: {}", eventType);
            rabbitTemplate.convertAndSend(ANALYTICS_EXCHANGE, ANALYTICS_ROUTING_KEY, message);
        } catch (Exception e) {
            log.error("Error sending analytics event {}: {}", eventType, e.getMessage(), e);
            throw new RuntimeException("Failed to send analytics event", e);
        }
    }



    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnalyticsMessage {
        private String eventType;
        private String data;
    }
}
