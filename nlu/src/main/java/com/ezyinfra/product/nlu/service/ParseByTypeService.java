package com.ezyinfra.product.nlu.service;

import com.ezyinfra.product.nlu.dto.ExtractRequest;
import com.ezyinfra.product.nlu.dto.QuestionRequest;
import com.fasterxml.jackson.databind.JsonNode;

public interface ParseByTypeService {

    /**
     * STRICT extractor.
     * - Stateless per turn
     * - Never asks questions
     * - Returns partial JSON patch or {}
     */
    JsonNode extractPartialPatch(ExtractRequest request);

    /**
     * Interviewer.
     * - Generates ONE message
     * - Asks all missing fields once
     */
    String generateQuestions(QuestionRequest request);
}
