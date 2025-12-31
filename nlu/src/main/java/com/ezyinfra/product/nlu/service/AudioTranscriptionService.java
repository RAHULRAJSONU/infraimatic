package com.ezyinfra.product.nlu.service;

import com.ezyinfra.product.storage.model.StorageResult;

import java.util.Optional;

public interface AudioTranscriptionService {

    /**
     * Transcribe audio stored in StorageService
     */
    Optional<String> transcribe(StorageResult audio, String contentType);
}
