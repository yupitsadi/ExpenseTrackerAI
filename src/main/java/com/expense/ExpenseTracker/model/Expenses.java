package com.expense.ExpenseTracker.model;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "epenses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expenses {
    private String userId;
    private String title;
    private Integer amount;
    private String category;
    private Date createdAt;
    private Date updatedAt;
    private Date DateOfExpense;
    private String notes;
}
