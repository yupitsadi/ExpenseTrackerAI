package com.expense.ExpenseTracker.controller;

import com.expense.ExpenseTracker.dto.AddExprnsesRequest;
import com.expense.ExpenseTracker.model.Expenses;
import com.expense.ExpenseTracker.service.ExpenseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/add")
    public ResponseEntity<Expenses> addExpense(@RequestBody AddExprnsesRequest request, @RequestParam String userId){
        Expenses newExp = expenseService.addExpense(request,userId);
        return ResponseEntity.ok(newExp);
    }

    @GetMapping
    public List<Expenses> getAllExpenese(@RequestParam String userId){
        return expenseService.getAllExpenese(userId);
    }

    @PutMapping("/updateExpenses")
    public Expenses updateExpenses(@RequestParam String id, @RequestBody Expenses updateExpenses, @RequestParam String userId){
        return expenseService.updateExpenses(id,updateExpenses,userId);
    }

    @DeleteMapping("/deleteExpenses")
    public boolean deleteTheExpense(@RequestParam String id,@RequestParam String userId){
        return expenseService.deleteTheExpense(id,userId);
    }

}
