package com.expense.ExpenseTracker.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class OpenAIConfig {

    @Value("${spring.openai.api.key}")
    private String openAIApiKey;

    @Value("${spring.openai.api.timeout:60}")
    private long timeoutInSeconds;

    @Bean
    public OpenAiService openAiService() {
        return new OpenAiService(openAIApiKey, Duration.ofSeconds(timeoutInSeconds));
    }
}
