package com.expense.ExpenseTracker.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTrendDTO {
    private Map<String, Object> id; // contains year, month, category
    private double total;
}

