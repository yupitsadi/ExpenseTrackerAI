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
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final MongoTemplate mongoTemplate;
    private final UserUtils utils;

    public List<CategoryTotalDTO> getTotalPerCategory() {
        String userId = utils.getCurrentUserId();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group("category")
                        .sum("amount").as("total"),
                Aggregation.project("total")
                        .and("_id").as("category")
        );
        return mongoTemplate.aggregate(aggregation, "expenses", CategoryTotalDTO.class).getMappedResults();
    }

    public List<ExpenseTrendDTO> getMonthlyTrends() {
        String userId = utils.getCurrentUserId();

        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.project("amount", "category", "DateOfExpense")
                        .andExpression("year(DateOfExpense)").as("year")
                        .andExpression("month(DateOfExpense)").as("month"),
                Aggregation.group("year", "month", "category")
                        .sum("amount").as("total"),
                Aggregation.project("total")
                        .and("_id.year").as("year")
                        .and("_id.month").as("month")
                        .and("_id.category").as("category")
        );

        return mongoTemplate.aggregate(aggregation, "expenses", ExpenseTrendDTO.class).getMappedResults();
    }

    public List<Map> getMonthlySummary() {
        String userId = utils.getCurrentUserId();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.project("amount", "DateOfExpense")
                        .andExpression("year(DateOfExpense)").as("year")
                        .andExpression("month(DateOfExpense)").as("month"),
                Aggregation.group("year", "month")
                        .sum("amount").as("total"),
                Aggregation.sort(Sort.Direction.ASC, "_id.year", "_id.month")
        );

        return mongoTemplate.aggregate(aggregation, "expenses", Map.class).getMappedResults();
    }

    public List<Map> getCategoryWiseSummary() {
        String userId = utils.getCurrentUserId();
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("userId").is(userId)),
                Aggregation.group("category")
                        .sum("amount").as("total"),
                Aggregation.project("total").and("_id").as("category")
        );

        return mongoTemplate.aggregate(aggregation, "expenses", Map.class).getMappedResults();
    }



}

