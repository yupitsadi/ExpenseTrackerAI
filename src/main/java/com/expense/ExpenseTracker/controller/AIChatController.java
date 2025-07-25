package com.expense.ExpenseTracker.controller;

import com.expense.ExpenseTracker.dto.AIChatRequest;
import com.expense.ExpenseTracker.dto.AIChatResponse;
import com.expense.ExpenseTracker.service.AIChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIChatController {

    private final AIChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<AIChatResponse> chat(@RequestBody AIChatRequest request) {
        AIChatResponse response = aiChatService.chat(request);
        return ResponseEntity.ok(response);
    }
}
