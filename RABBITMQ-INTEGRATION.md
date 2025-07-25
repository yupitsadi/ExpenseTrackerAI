# RabbitMQ Integration Guide

This document provides an overview of the RabbitMQ integration in the Expense Tracker application, including setup instructions, architecture, and usage examples.

## Table of Contents
- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Setup](#setup)
- [Architecture](#architecture)
- [Message Types](#message-types)
- [Usage Examples](#usage-examples)
- [Monitoring and Management](#monitoring-and-management)
- [Troubleshooting](#troubleshooting)

## Overview
The Expense Tracker application uses RabbitMQ as a message broker to handle asynchronous processing for notifications, emails, and analytics. This decouples the main application flow from time-consuming tasks, improving overall system performance and user experience.

## Prerequisites
- Java 21+
- RabbitMQ 3.11+
- Docker (optional, for local development with Testcontainers)

## Setup

### Local Development
1. Ensure RabbitMQ is running locally:
   ```bash
   # Using Docker
   docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 rabbitmq:3.11-management
   ```
   
   Access the management console at: http://localhost:15672 (guest/guest)

2. Configure the application to connect to RabbitMQ by updating `application.yml`:
   ```yaml
   spring:
     rabbitmq:
       host: localhost
       port: 5672
       username: guest
       password: guest
   ```

### Production
Update the configuration in `application-prod.yml` with your production RabbitMQ credentials and connection details.

## Architecture

### Exchanges and Queues
- **Notification Exchange** (`notification.exchange`)
  - Queue: `notification.queue`
  - Purpose: Handles application notifications

- **Email Exchange** (`email.exchange`)
  - Queue: `email.queue`
  - Purpose: Handles email sending

- **Analytics Exchange** (`analytics.exchange`)
  - Queue: `analytics.queue`
  - Purpose: Handles analytics events

## Message Types

### 1. Notifications
```java
messageProducer.sendNotification("Your expense was saved successfully!");
```

### 2. Emails
```java
messageProducer.sendEmail(
    "user@example.com",
    "Welcome to Expense Tracker",
    "Thank you for signing up!"
);
```

### 3. Analytics Events
```java
String eventData = "{\"userId\":\"123\",\"action\":\"expense_created\"}";
messageProducer.sendAnalytics("expense_created", eventData);
```

## Usage Examples

### Sending a Welcome Email After User Registration
```java
@PostMapping("/signup")
public ResponseEntity<?> registerUser(@RequestBody UserRegistrationRequest request) {
    // Register user...
    
    // Send welcome email asynchronously
    messageProducer.sendEmail(
        user.getEmail(),
        "Welcome to Expense Tracker!",
        "Thank you for signing up!"
    );
    
    return ResponseEntity.ok().build();
}
```

### Tracking User Actions
```java
@PostMapping("/expenses")
public ResponseEntity<Expense> createExpense(@RequestBody Expense expense) {
    // Save expense...
    
    // Track analytics
    String eventData = String.format(
        "{\"userId\":\"%s\",\"amount\":%.2f,\"category\":\"%s\"}",
        currentUserId,
        expense.getAmount(),
        expense.getCategory()
    );
    messageProducer.sendAnalytics("expense_created", eventData);
    
    return ResponseEntity.ok(savedExpense);
}
```

## Monitoring and Management

### RabbitMQ Management Console
Access the RabbitMQ management console at `http://your-rabbitmq-host:15672` to:
- Monitor queue lengths
- View message rates
- Inspect connections and channels
- Manage queues and exchanges

### Logging
All messaging operations are logged with appropriate log levels:
- INFO: Successful message publishing and consumption
- ERROR: Failed message processing with stack traces

## Troubleshooting

### Common Issues

#### Messages Not Being Processed
1. Check if the RabbitMQ server is running
2. Verify the connection settings in `application.yml`
3. Check the application logs for any errors
4. Inspect the RabbitMQ management console for queue backlogs

#### Message Processing Failures
1. Check the application logs for detailed error messages
2. Implement dead-letter queues for failed messages (recommended for production)
3. Consider adding retry mechanisms for transient failures

### Enabling Debug Logging
Add to `application.yml`:
```yaml
logging:
  level:
    org.springframework.amqp: DEBUG
    com.expense.ExpenseTracker.messaging: DEBUG
```
