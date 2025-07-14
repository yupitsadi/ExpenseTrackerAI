package com.expense.ExpenseTracker.repository;

import com.expense.ExpenseTracker.model.Expenses;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface ExpenseRepository extends MongoRepository<Expenses,String> {
    List<Expenses> findByUserId(String userId);
    List<Expenses> findByUserIdAndDateOfExpenseBetween(String userId, Date start, Date end);
    @Query("{ 'userId': ?0, " +
            "'dateOfExpense': { $gte: ?1, $lte: ?2 }, " +
            "'category': { $regex: ?3, $options: 'i' }, " +
            "'title': { $regex: ?4, $options: 'i' } }")
    List<Expenses> searchExpenses(String userId, Date start, Date end, String category, String title);


}
