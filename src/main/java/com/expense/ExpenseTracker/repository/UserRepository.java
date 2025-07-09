package com.expense.ExpenseTracker.repository;

import com.expense.ExpenseTracker.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.jmx.export.metadata.ManagedOperation;

import java.util.Optional;


public interface UserRepository extends MongoRepository<User,String> {
    Optional<User> findByEmail(String email);
}
