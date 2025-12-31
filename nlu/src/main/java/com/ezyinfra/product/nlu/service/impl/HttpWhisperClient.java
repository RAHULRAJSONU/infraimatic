package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.nlu.service.WhisperClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class HttpWhisperClient implements WhisperClient {

    private final WebClient webClient;

    public HttpWhisperClient(WebClient.Builder builder,
                             @Value("${whisper.api.base-url}") String baseUrl,
                             @Value("${whisper.api.api-key}") String apiKey) {

        this.webClient = builder
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    @Override
    public String transcribeFromUrl(String audioUrl, String contentType) {

        return webClient.post()
                .uri("/audio/transcriptions")
                .bodyValue(Map.of(
                        "audio_url", audioUrl,
                        "response_format", "text"
                ))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(60))
                .block();
    }
}
