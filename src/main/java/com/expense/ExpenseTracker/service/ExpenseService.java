package com.expense.ExpenseTracker.service;


import com.expense.ExpenseTracker.dto.AddExprnsesRequest;
import com.expense.ExpenseTracker.model.Expenses;
import com.expense.ExpenseTracker.repository.ExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    public Expenses addExpense(AddExprnsesRequest request, String userId){
        Expenses expenses = new Expenses();
        expenses.setUserId(userId);
        expenses.setTitle(request.getTitle());
        expenses.setAmount(request.getAmount());
        expenses.setCategory(request.getCategory());
        expenses.setDateOfExpense(request.getDateOfExpense());
        expenses.setNotes(request.getNotes());
        expenses.setCreatedAt(new Date());
        expenses.setUpdatedAt(new Date());
        return expenseRepository.save(expenses);
    }

    public List<Expenses> getAllExpenese(String userId){
        return expenseRepository.findAllById(Collections.singleton(userId));
    }

    public Optional<Expenses> getExpensesById(String id, String userId){
        Optional<Expenses> exp = expenseRepository.findById(id);
        if(exp.isPresent() && exp.get().getUserId().equals(userId)){
            return exp;
        }
        return Optional.empty();
    }

    public Expenses updateExpenses(String id, Expenses updatedExpenses, String userId){
        Optional<Expenses> exp = expenseRepository.findById(id);
        if (exp.isPresent() && exp.get().getUserId().equals(userId)){
            Expenses existing = exp.get();
            existing.setTitle(updatedExpenses.getTitle());
            existing.setTitle(updatedExpenses.getTitle());
            existing.setCategory(updatedExpenses.getCategory());
            existing.setNotes(updatedExpenses.getNotes());
            existing.setDateOfExpense(updatedExpenses.getDateOfExpense());
            existing.setUpdatedAt(new Date());
            return expenseRepository.save(existing);
        }
        else {
            throw new RuntimeException("Expense not found or you are not authorized to update this expense.");
        }
    }

    public boolean deleteTheExpense(String id, String userId){
        Optional<Expenses> expenses = getExpensesById(id,userId);
        if(expenses.isPresent()){
            expenseRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }

}
