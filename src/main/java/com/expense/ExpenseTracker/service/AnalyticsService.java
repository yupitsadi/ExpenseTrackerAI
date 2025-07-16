package com.expense.ExpenseTracker.service;

import com.expense.ExpenseTracker.dto.CategoryTotalDTO;
import com.expense.ExpenseTracker.dto.ExpenseTrendDTO;
import com.expense.ExpenseTracker.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final UserUtils utils;

    public List<CategoryTotalDTO> getTotalPerCategory() {
        String userId = utils.getCurrentUserId();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group("category").sum("amount").as("total"),
                Aggregation.sort(Sort.Direction.DESC, "total")

        );
        return mongoTemplate.aggregate(aggregation, "expenses", CategoryTotalDTO.class).getMappedResults();
    }

    public List<ExpenseTrendDTO> getMonthlyTrends() {
        String userId = utils.getCurrentUserId();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.project()
                        .andExpression("month(dateOfExpense)").as("month")
                        .andExpression("year(dateOfExpense)").as("year")
                        .and("category").as("category")
                        .and("amount").as("amount"),
                Aggregation.group("year", "month", "category").sum("amount").as("total"),
                Aggregation.sort(Sort.Direction.ASC, "_id.year")
                        .and(Sort.Direction.ASC, "_id.month")
        );

        return mongoTemplate.aggregate(aggregation, "expenses", ExpenseTrendDTO.class).getMappedResults();
    }
}

