package com.expense.ExpenseTracker.service;

import com.expense.ExpenseTracker.dto.CategoryTotalDTO;
import com.expense.ExpenseTracker.dto.ExpenseTrendDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/total-per-category")
    public ResponseEntity<List<CategoryTotalDTO>> getTotalPerCategory() {
        return ResponseEntity.ok(analyticsService.getTotalPerCategory());
    }

    @GetMapping("/monthly-trends")
    public ResponseEntity<List<ExpenseTrendDTO>> getMonthlyTrends() {
        return ResponseEntity.ok(analyticsService.getMonthlyTrends());
    }
}
