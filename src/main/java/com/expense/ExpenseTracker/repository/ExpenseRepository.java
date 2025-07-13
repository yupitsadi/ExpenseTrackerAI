package com.expense.ExpenseTracker.repository;

import com.expense.ExpenseTracker.model.Expenses;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expenses,String> {
    List<Expenses> findByUserId(String userId);
}
