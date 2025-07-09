package com.expense.ExpenseTracker.controller;


import com.expense.ExpenseTracker.model.User;
import com.expense.ExpenseTracker.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    private final UserRepository userRepo;

    public TestController(UserRepository userRepo) {
        this.userRepo = userRepo;
    }

    @PostMapping("/add")
    public String addUser(@RequestParam String email, @RequestParam String password){
        userRepo.save(new User(email,password));
        return  "Added User";
    }
}
