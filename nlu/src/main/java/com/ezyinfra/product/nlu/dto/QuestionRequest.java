package com.ezyinfra.product.nlu.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.Set;

@Builder
public record QuestionRequest(
        String type,
        Set<String> requiredFields,
        Set<String> pendingFields,
        Set<String> alreadyAskedFields,
        JsonNode collectedJson
) {}
