package com.expense.ExpenseTracker.dto;

import lombok.Data;

import java.util.Date;

@Data
public class AddExprnsesRequest {
    private String title;
    private Integer amount;
    private String category;
    private Date DateOfExpense;
    private String notes;
}
