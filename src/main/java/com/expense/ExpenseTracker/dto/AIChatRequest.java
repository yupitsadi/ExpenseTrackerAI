package com.expense.ExpenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AIChatRequest {
    private String message;
    private String conversationId;
}
