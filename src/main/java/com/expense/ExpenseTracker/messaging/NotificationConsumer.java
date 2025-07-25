package com.expense.ExpenseTracker.messaging;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationConsumer {

    @RabbitListener(queues = "${rabbitmq.notification.queue:notification.queue}")
    public void receiveNotification(String message) {
        try {
            log.info("Received notification: {}", message);
            // Process the notification here
            // You can add your notification logic, e.g., sending push notifications, in-app notifications, etc.
        } catch (Exception e) {
            log.error("Error processing notification: {}", e.getMessage(), e);
            // Consider implementing a retry mechanism or dead-letter queue for failed messages
        }
    }
}
