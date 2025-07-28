package com.expense.ExpenseTracker.controller;

import com.expense.ExpenseTracker.dto.AddExprnsesRequest;
import com.expense.ExpenseTracker.dto.DashboardRequest;
import com.expense.ExpenseTracker.dto.DashboardSummaryResponse;
import com.expense.ExpenseTracker.dto.ExpenseSearchRequest;
import com.expense.ExpenseTracker.messaging.RabbitMQProducer;
import com.expense.ExpenseTracker.model.Expenses;
import com.expense.ExpenseTracker.model.User;
import com.expense.ExpenseTracker.repository.UserRepository;
import com.expense.ExpenseTracker.service.ExpenseService;
import com.expense.ExpenseTracker.utils.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/expenses")
public class ExpenseController {
    private final ExpenseService expenseService;
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserUtils utils;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping("/add")
    public ResponseEntity<Expenses> addExpense(@RequestBody AddExprnsesRequest request){
        Expenses newExp = expenseService.addExpense(request);
        return ResponseEntity.ok(newExp);
    }

    @GetMapping
    public List<Expenses> getAllExpenese(){
        return expenseService.getAllExpenese();
    }

    @PutMapping("/updateExpenses")
    public Expenses updateExpenses(@RequestParam String id, @RequestBody Expenses updateExpenses){
        return expenseService.updateExpenses(id,updateExpenses);
    }

    @DeleteMapping("/deleteExpenses")
    public boolean deleteTheExpense(@RequestParam String id){
        return expenseService.deleteTheExpense(id);
    }

    @PostMapping("/summary")
    public ResponseEntity<DashboardSummaryResponse> getDashboardSummary(@RequestBody DashboardRequest request) {
        return ResponseEntity.ok(expenseService.getDashboardSummary(
                request.getStartDate(), request.getEndDate()
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<List<Expenses>> searchExpenses(@RequestBody ExpenseSearchRequest request) {
        return ResponseEntity.ok(expenseService.searchExpenses(request));
    }

    @GetMapping("/csv")
    public ResponseEntity<byte[]> exportCSV() {
        byte[] data = expenseService.exportToCSV();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.setContentDispositionFormData("attachment", "expenses.csv");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPDF() {
        byte[] data = expenseService.exportToPDF();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "expenses.pdf");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @GetMapping("/email-report")
    public ResponseEntity<String> sendEmailReport() {
        expenseService.sendReportToUser(); // No email logic here
        return ResponseEntity.ok("Email report generation started");
    }


}
