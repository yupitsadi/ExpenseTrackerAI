package com.expense.ExpenseTracker.controller;

import com.expense.ExpenseTracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthController {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

}
