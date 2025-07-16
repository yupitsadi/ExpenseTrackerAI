package com.expense.ExpenseTracker.service;


import com.expense.ExpenseTracker.dto.AddExprnsesRequest;
import com.expense.ExpenseTracker.dto.CategoryTotalDTO;
import com.expense.ExpenseTracker.dto.DashboardSummaryResponse;
import com.expense.ExpenseTracker.dto.ExpenseSearchRequest;
import com.expense.ExpenseTracker.model.Expenses;
import com.expense.ExpenseTracker.repository.ExpenseRepository;
import com.expense.ExpenseTracker.utils.UserUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private UserUtils userUtils;

    public Expenses addExpense(AddExprnsesRequest request){
        String userId = userUtils.getCurrentUserId();
        Expenses expenses = new Expenses();
        expenses.setUserId(userId);
        expenses.setTitle(request.getTitle());
        expenses.setAmount(Float.valueOf(request.getAmount()));
        expenses.setCategory(request.getCategory());
        expenses.setDateOfExpense(request.getDateOfExpense());
        expenses.setNotes(request.getNotes());
        expenses.setCreatedAt(new Date());
        expenses.setUpdatedAt(new Date());
        return expenseRepository.save(expenses);
    }

    public List<Expenses> getAllExpenese() {
        String userId = userUtils.getCurrentUserId();
        return expenseRepository.findByUserId(userId);
    }


    public Optional<Expenses> getExpensesById(String id){
        String userId = userUtils.getCurrentUserId();
        Optional<Expenses> exp = expenseRepository.findById(id);
        if(exp.isPresent() && exp.get().getUserId().equals(userId)){
            return exp;
        }
        return Optional.empty();
    }

    public Expenses updateExpenses(String id, Expenses updatedExpenses){
        String userId = userUtils.getCurrentUserId();
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

    public boolean deleteTheExpense(String id){
        String userId = userUtils.getCurrentUserId();
        Optional<Expenses> expenses = getExpensesById(id);
        if(expenses.isPresent()){
            expenseRepository.deleteById(id);
            return true;
        }
        else {
            return false;
        }
    }

    public DashboardSummaryResponse getDashboardSummary(Date start, Date end) {
        // Normalize start to 00:00:00 and end to 23:59:59
        ZoneId zone = ZoneId.systemDefault();

        LocalDate startLocal = start.toInstant().atZone(zone).toLocalDate();
        LocalDate endLocal = end.toInstant().atZone(zone).toLocalDate();

        Date normalizedStart = Date.from(startLocal.atStartOfDay(zone).toInstant());
        Date normalizedEnd = Date.from(endLocal.atTime(LocalTime.MAX).atZone(zone).toInstant());

        String userId = userUtils.getCurrentUserId();
        System.out.println("userId: " + userId);

        List<Expenses> expenses = expenseRepository.findByUserIdAndDateOfExpenseBetween(
                userId, normalizedStart, normalizedEnd
        );

        System.out.println("Expenses fetched: " + expenses.size());
        System.out.println("Start: " + normalizedStart + "   End: " + normalizedEnd);
        expenses.forEach(System.out::println);

        float totalAmount = (float) expenses.stream().mapToDouble(Expenses::getAmount).sum();
        int count = expenses.size();

        Map<String, Integer> categoryTotals = new HashMap<>();
        for (Expenses exp : expenses) {
            categoryTotals.put(
                    exp.getCategory(),
                    (int) (categoryTotals.getOrDefault(exp.getCategory(), 0) + exp.getAmount())
            );
        }

        List<DashboardSummaryResponse.CategorySummary> topCategories = categoryTotals.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .limit(3)
                .map(entry -> {
                    DashboardSummaryResponse.CategorySummary cat = new DashboardSummaryResponse.CategorySummary();
                    cat.setCategory(entry.getKey());
                    cat.setTotalAmount(entry.getValue());
                    return cat;
                })
                .collect(Collectors.toList());

        DashboardSummaryResponse response = new DashboardSummaryResponse();
        response.setTotalAmountSpent((int) totalAmount);
        response.setTotalExpenses(count);
        response.setTopCategories(topCategories);

        return response;
    }

    public List<Expenses> searchExpenses(ExpenseSearchRequest request) {
        String userId = userUtils.getCurrentUserId();

        Date start = request.getStartDate() != null ? request.getStartDate() : new Date(0);
        Date end = request.getEndDate() != null ? request.getEndDate() : new Date();

        String category = request.getCategory() != null ? request.getCategory() : "";
        String title = request.getTitle() != null ? request.getTitle() : "";

        return expenseRepository.searchExpenses(userId, start, end, category, title);
    }

    public byte[] exportToCSV() {
        String userId = userUtils.getCurrentUserId();
        List<Expenses> expenses = expenseRepository.findByUserId(userId);

        StringBuilder sb = new StringBuilder();
        sb.append("Date,Category,Amount,Note\n");

        for (Expenses e : expenses) {
            sb.append(e.getDateOfExpense()).append(",");
            System.out.println("Date: " + e.getDateOfExpense());
            sb.append(e.getCategory()).append(",");
            sb.append(e.getAmount()).append(",");
            sb.append("\"").append(e.getNotes().replace("\"", "\"\"")).append("\"\n");
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public byte[] exportToPDF() {
        String userId = userUtils.getCurrentUserId();
        List<Expenses> expenses = expenseRepository.findByUserId(userId);
        System.out.println("1");
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);
            System.out.println("2");

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("Expense Report");
                contentStream.endText();

                contentStream.setFont(PDType1Font.HELVETICA, 12);
                int y = 720;

                contentStream.beginText();
                contentStream.newLineAtOffset(50, y);
                contentStream.showText("Date    Category    Amount    Note"); // spaces instead of \t
                contentStream.endText();

                for (Expenses e : expenses) {
                    y -= 20;
                    contentStream.beginText();
                    contentStream.newLineAtOffset(50, y);
                    contentStream.showText(
                            String.format("%s    %s    %.2f    %s", // again: use spaces, not tabs
                                    e.getDateOfExpense(),
                                    e.getCategory(),
                                    e.getAmount(),
                                    e.getNotes()));
                    contentStream.endText();
                }

            }

            document.save(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



}
