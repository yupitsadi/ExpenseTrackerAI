package com.expense.ExpenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AIChatResponse {
    private String response;
    private String conversationId;
    private List<String> suggestions;
}
