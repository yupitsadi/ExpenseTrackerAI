package com.expense.ExpenseTracker.messaging;

public interface MessageProducer {
    void sendNotification(String message);
    void sendEmail(String email, String subject, String content);
    void sendAnalytics(String eventType, String data);
}
