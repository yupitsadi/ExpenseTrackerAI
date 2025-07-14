package com.expense.ExpenseTracker.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Data
@Getter
@Setter
public class DashboardRequest {
    private Date startDate;
    private Date endDate;
}
