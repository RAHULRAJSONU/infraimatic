package com.ezyinfra.product.nlu.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;

import java.util.Map;
import java.util.Set;

@Builder
public record ExtractRequest(
        String type,
        String userText,
        JsonNode fullSchema,
        JsonNode existingJson,
        Set<String> targetFields,
        Set<String> alreadyAskedFields,
        Set<String> requiredFields,
        Map<String,Object> options
) {}
