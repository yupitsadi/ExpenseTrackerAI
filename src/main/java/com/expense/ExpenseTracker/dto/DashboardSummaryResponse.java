package com.expense.ExpenseTracker.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Data
public class DashboardSummaryResponse {
    private int totalExpenses;
    private int totalAmountSpent;
    private List<CategorySummary> topCategories;

    @Data
    public static class CategorySummary {
        private String category;
        private int totalAmount;

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getTotalAmount() { return totalAmount; }
        public void setTotalAmount(int totalAmount) { this.totalAmount = totalAmount; }
    }

}
