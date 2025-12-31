package com.ezyinfra.product.nlu.service;

public interface WhisperClient {

    /**
     * Transcribe audio available at a URL
     */
    String transcribeFromUrl(String audioUrl, String contentType);
}
