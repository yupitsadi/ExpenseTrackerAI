package com.expense.ExpenseTracker.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CategoryTotalDTO {
    private String category;
    private double total;
}
