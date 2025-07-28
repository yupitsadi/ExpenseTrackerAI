package com.expense.ExpenseTracker.service;

import com.expense.ExpenseTracker.dto.*;
import com.expense.ExpenseTracker.model.Expenses;
import com.expense.ExpenseTracker.repository.ExpenseRepository;
import com.expense.ExpenseTracker.utils.UserUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor
public class ExpenseService {
    @Autowired
    private ExpenseRepository expenseRepository;
    private EmailMessage emailMessage;

    @Autowired
    private UserUtils userUtils;

    @Autowired
    private RabbitTemplate rabbitMQProducer;

    private final ObjectMapper objectMapper;


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

    public void sendReportToUser(){
            String userId = userUtils.getCurrentUserId();
            String email = userUtils.getCurrentEmailId().getEmail();
            byte[] pdfBytes = generateAnalyticsPdf();
            EmailMessage emailMessage = new EmailMessage(
                    email,
                    "Your Monthly Expense Report",
                    "Please find attached your monthly expense report.",
                    pdfBytes
            );
            try {
                String jsonMessage = objectMapper.writeValueAsString(emailMessage);
                rabbitMQProducer.convertAndSend("email.exchange", "email.routingKey", jsonMessage);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
    }


    private byte[] generateAnalyticsPdf() {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            
            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
                contentStream.beginText();
                contentStream.newLineAtOffset(100, 700);
                contentStream.showText("Expense Analytics Report");
                contentStream.setFont(PDType1Font.HELVETICA, 12);
                contentStream.newLineAtOffset(0, -30);
                contentStream.showText("Generated on: " + new Date());
                contentStream.newLineAtOffset(0, -20);

                List<CategoryTotalDTO> categoryTotals = getCategoryTotals(userUtils.getCurrentUserId());
                contentStream.showText("Expense by Category:");
                contentStream.newLineAtOffset(0, -20);
                
                for (CategoryTotalDTO category : categoryTotals) {
                    contentStream.showText(String.format("- %s: $%.2f", 
                        category.getCategory(), category.getTotal()));
                    contentStream.newLineAtOffset(0, -15);
                }
                
                contentStream.endText();
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            throw new RuntimeException("Error generating PDF report", e);
        }
    }

    private List<CategoryTotalDTO> getCategoryTotals(String userId) {
        List<Expenses> expenses = expenseRepository.findByUserId(userId);
        Map<String, Float> categoryTotals = new HashMap<>();
        for (Expenses exp : expenses) {
            categoryTotals.put(
                    exp.getCategory(),
                    categoryTotals.getOrDefault(exp.getCategory(), 0f) + exp.getAmount()
            );
        }
        List<CategoryTotalDTO> result = new ArrayList<>();
        for (Map.Entry<String, Float> entry : categoryTotals.entrySet()) {
            CategoryTotalDTO dto = new CategoryTotalDTO();
            dto.setCategory(entry.getKey());
            dto.setTotal(entry.getValue());
            result.add(dto);
        }
        return result;
    }
}
