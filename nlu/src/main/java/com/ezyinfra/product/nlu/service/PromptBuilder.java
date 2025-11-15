package com.ezyinfra.product.nlu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;

@Component
public class PromptBuilder {
    private final ObjectMapper mapper = new ObjectMapper();

    public String buildInitialPrompt(JsonNode minimalSchema, String text, List<String> examples, Map<String,Object> options) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are a strict extractor. Input: free-form gatepass natural language. ");
        sb.append("Output: JSON only, must use the exact property names found in the provided SCHEMA. ");
        sb.append("Include a top-level _confidence map with per-field confidence (0..1). Use ISO-8601 for dates.\n\n");
        sb.append("SCHEMA:\n").append(minimalSchema.toPrettyString()).append("\n\n");
        if (examples != null) {
            for (String ex : examples) {
                sb.append("EXAMPLE_INPUT:\n\"\"\"\n").append(ex).append("\n\"\"\"\n");
                sb.append("EXAMPLE_OUTPUT:\n").append("{ /* example JSON matching schema */ }\n\n");
            }
        }
        sb.append("TEXT:\n\"\"\"\n").append(text).append("\n\"\"\"\n\n");
        sb.append("Return JSON only. If a field is not present set it to null if allowed or omit it. Do not add extra fields.");
        return sb.toString();
    }

    public String buildFocusedPrompt(JsonNode fullSchema, String text, List<String> missingPaths, JsonNode previous) {
        StringBuilder sb = new StringBuilder();
        sb.append("Fix only the following fields: ").append(missingPaths).append(". ");
        sb.append("Use the schema and previous JSON, return JSON only containing only corrected fields and an _confidence map for them.\n\n");
        sb.append("SCHEMA:\n").append(fullSchema.toPrettyString()).append("\n\n");
        sb.append("TEXT:\n\"\"\"\n").append(text).append("\n\"\"\"\n\n");
        sb.append("PREVIOUS_JSON:\n").append(previous.toPrettyString()).append("\n\nReturn JSON only.");
        return sb.toString();
    }
}