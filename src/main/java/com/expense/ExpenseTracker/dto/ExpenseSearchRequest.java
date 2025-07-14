package com.expense.ExpenseTracker.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ExpenseSearchRequest {
    private Date startDate;
    private Date endDate;
    private String category;
    private String title;
}
