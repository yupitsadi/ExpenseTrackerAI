package com.expense.ExpenseTracker.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalyticsConsumer {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${rabbitmq.analytics.queue:analytics.queue}")
    public void receiveAnalytics(String message) {
        try {
            log.info("Received analytics event: {}", message);

            // Deserialize the message
            RabbitMQProducer.AnalyticsMessage analyticsMessage = objectMapper.readValue(
                message,
                RabbitMQProducer.AnalyticsMessage.class
            );

            // Process the analytics event
            processAnalyticsEvent(
                analyticsMessage.getEventType(),
                analyticsMessage.getData()
            );

        } catch (Exception e) {
            log.error("Error processing analytics event: {}", e.getMessage(), e);
            // Consider implementing a retry mechanism or dead-letter queue for failed messages
            throw new RuntimeException("Failed to process analytics event", e);
        }
    }

    private void processAnalyticsEvent(String eventType, String data) {
        // Implement your analytics processing logic here
        // This could include:
        // - Storing the event in a time-series database
        // - Updating aggregated analytics
        // - Triggering alerts based on certain conditions
        // - Generating reports

        log.info("Processing analytics event - Type: {}, Data: {}", eventType, data);

        // Example: You could have different handlers for different event types
        switch (eventType.toLowerCase()) {
            case "user_signup":
                handleUserSignup(data);
                break;
            case "expense_created":
                handleExpenseCreated(data);
                break;
            case "login":
                handleUserLogin(data);
                break;
            default:
                log.warn("Unknown analytics event type: {}", eventType);
        }
    }

    private void handleUserSignup(String data) {
        // Process user signup analytics
        log.info("Processing user signup: {}", data);
    }

    private void handleExpenseCreated(String data) {
        // Process expense creation analytics
        log.info("Processing expense created: {}", data);
    }

    private void handleUserLogin(String data) {
        // Process user login analytics
        log.info("Processing user login: {}", data);
    }
}
