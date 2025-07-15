package com.expense.ExpenseTracker.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Setter
@Getter
public class AddExprnsesRequest {
    private String title;
    private Integer amount;
    private String category;
    private Date DateOfExpense;
    private String notes;
}
