package com.ezyinfra.product.nlu.service.impl;

import com.ezyinfra.product.nlu.service.AudioTranscriptionService;
import com.ezyinfra.product.nlu.service.WhisperClient;
import com.ezyinfra.product.storage.model.StorageResult;
import com.ezyinfra.product.storage.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Optional;

@Slf4j
@Service
public class WhisperAudioTranscriptionService implements AudioTranscriptionService {

    private final StorageService storageService;
    private final WhisperClient whisperClient;

    public WhisperAudioTranscriptionService(
            StorageService storageService,
            WhisperClient whisperClient
    ) {
        this.storageService = storageService;
        this.whisperClient = whisperClient;
    }

    @Override
    public Optional<String> transcribe(StorageResult audio, String contentType) {
        try {
            // 1️⃣ Generate temporary read URL
            String presignedUrl = storageService
                    .generatePresignedUrl(audio.getId(), 300)
                    .orElseThrow(() ->
                            new IllegalStateException("Unable to generate presigned URL for " + audio.getId()));

            // 2️⃣ Delegate to Whisper client
            String text = whisperClient.transcribeFromUrl(presignedUrl, contentType);

            return Optional.ofNullable(text).filter(t -> !t.isBlank());

        } catch (Exception e) {
            log.error("Failed to transcribe audio id={}", audio.getId(), e);
            return Optional.empty();
        }
    }
}
