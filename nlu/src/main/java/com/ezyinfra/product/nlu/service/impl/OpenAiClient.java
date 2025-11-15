package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.nlu.service.LLMClient;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OpenAiClient implements LLMClient {

    private final ChatClient chatClient;

    public OpenAiClient(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String complete(String prompt, Map<String, Object> options) {
        int maxTokens = 800;
        double temperature = 0.0;

        if (options != null) {
            Object mt = options.get("max_tokens");
            if (mt instanceof Number) {
                maxTokens = ((Number) mt).intValue();
            } else if (mt instanceof String) {
                try { maxTokens = Integer.parseInt((String) mt); } catch (NumberFormatException ignored) {}
            }

            Object tp = options.get("temperature");
            if (tp instanceof Number) {
                temperature = ((Number) tp).doubleValue();
            } else if (tp instanceof String) {
                try { temperature = Double.parseDouble((String) tp); } catch (NumberFormatException ignored) {}
            }
        }

        OpenAiChatOptions chatOptions = OpenAiChatOptions.builder()
                .maxTokens(maxTokens)
                .temperature(temperature)
                .build();

        // Fluent Spring AI 1.0.2 call
        return chatClient
                .prompt()
                .user(prompt)
                .options(chatOptions)
                .call()
                .content(); // returns the assistant's message content as String
    }
}