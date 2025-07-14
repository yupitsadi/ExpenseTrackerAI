package com.expense.ExpenseTracker.dto;


import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResetPasswordRequest {
    private String token;
    private String newPassword;
}
