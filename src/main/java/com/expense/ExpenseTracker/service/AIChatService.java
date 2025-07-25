package com.expense.ExpenseTracker.service;

import com.expense.ExpenseTracker.dto.AIChatRequest;
import com.expense.ExpenseTracker.dto.AIChatResponse;
import com.expense.ExpenseTracker.utils.UserUtils;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AIChatService {

    private final OpenAiService openAiService;
    private final AnalyticsService analyticsService;
    private final UserUtils userUtils;

    private final Map<String, List<ChatMessage>> conversationHistory = new ConcurrentHashMap<>();

    private static final String SYSTEM_PROMPT = """
            You are a financial assistant helping users analyze their expenses.
            Use the analytics data to generate insights about:
            - High spending categories
            - Monthly trends
            - Suggestions for savings

            Respond with:
            1. A short summary
            2. Top 2-3 categories
            3. 2-3 actionable tips
            4. Make sure you use rupee symbol and india context data
            """;


    public AIChatResponse chat(AIChatRequest request) {
        String userId = userUtils.getCurrentUserId();
        String conversationId = request.getConversationId() != null ?
            request.getConversationId() : UUID.randomUUID().toString();

        // Get or initialize conversation history
        List<ChatMessage> messages = conversationHistory.computeIfAbsent(
            conversationId, k -> new ArrayList<>()
        );

        // If it's a new conversation, add system message and analytics
        if (messages.isEmpty()) {
            messages.add(new ChatMessage("system", SYSTEM_PROMPT));
            String analyticsSummary = getAnalyticsSummary();
            messages.add(new ChatMessage("system", "User's spending data: " + analyticsSummary));
        }


        messages.add(new ChatMessage("user", request.getMessage()));

        // Create chat completion request
        ChatCompletionRequest chatRequest = ChatCompletionRequest.builder()
            .model("gpt-3.5-turbo")
            .messages(messages)
            .maxTokens(500)
            .temperature(0.7)
            .build();

        // Get AI response
        ChatMessage response = callWithRetry(chatRequest);


        // Add AI response to conversation history
        messages.add(response);

        // Extract suggestions from the response
        List<String> suggestions = extractSuggestions(response.getContent());

        return new AIChatResponse(
            response.getContent(),
            conversationId,
            suggestions
        );
    }

    private ChatMessage callWithRetry(ChatCompletionRequest chatRequest) {
        int retries = 0;
        int maxRetries = 3;
        long waitTime = 2000; // Start with 2 seconds

        while (retries < maxRetries) {
            try {
                return openAiService.createChatCompletion(chatRequest)
                        .getChoices().get(0).getMessage();
            } catch (retrofit2.HttpException e) {
                if (e.code() == 429) {
                    retries++;
                    try {
                        System.err.println("Rate limit hit. Retrying in " + waitTime + "ms...");
                        Thread.sleep(waitTime);
                        waitTime *= 2; // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during backoff", ie);
                    }
                } else {
                    throw e; // For other HTTP errors, rethrow
                }
            } catch (Exception e) {
                throw new RuntimeException("OpenAI API call failed", e);
            }
        }

        throw new RuntimeException("Exceeded max retries for OpenAI API");
    }


    private String getAnalyticsSummary() {
        try {
            StringBuilder summary = new StringBuilder();

            // Get category totals
            List<Map> categoryTotals = analyticsService.getCategoryWiseSummary();
            if (!categoryTotals.isEmpty()) {
                summary.append("Category-wise spending: ");
                categoryTotals.forEach(cat ->
                    summary.append(String.format("%s: $%.2f, ",
                        cat.get("category"),
                        Double.parseDouble(cat.get("total").toString()))
                    )
                );
                summary.append(". ");
            }

            // Get monthly trends
            List<Map> monthlyTrends = analyticsService.getMonthlySummary();
            if (!monthlyTrends.isEmpty()) {
                summary.append("Monthly spending trends: ");
                monthlyTrends.forEach(month ->
                    summary.append(String.format("%s-%s: $%.2f, ",
                        month.get("year"),
                        month.get("month"),
                        Double.parseDouble(month.get("total").toString()))
                    )
                );
                summary.append(". ");
            }

            return summary.toString();
        } catch (Exception e) {
            return "Unable to fetch analytics data at this time.";
        }
    }

    private List<String> extractSuggestions(String response) {
        List<String> suggestions = new ArrayList<>();
        String[] sentences = response.split("\\.\\s*");

        int count = Math.min(3, sentences.length);
        for (int i = 0; i < count; i++) {
            String sentence = sentences[i].trim();
            if (!sentence.isEmpty()) {
                suggestions.add(sentence + ".");
            }
        }

        return suggestions;
    }
}
