package com.ezyinfra.product.nlu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public String buildPartialPatchPrompt(
            JsonNode fullSchema,
            String userText,
            JsonNode existingJson,
            Set<String> targetFields,
            Set<String> alreadyAskedFields
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("""
            You are a STRICT INFORMATION EXTRACTION ENGINE.
            You are NOT a chatbot.
            You MUST NOT ask questions.
            You MUST NOT explain anything.
            
            ====================
            ABSOLUTE RULES
            ====================
            - Output VALID JSON only
            - No markdown
            - No explanations
            - No inferred or guessed values
            - Extract ONLY what is EXPLICITLY stated
            - Do NOT repeat existing values unless user clearly updates them
            - If information is missing or ambiguous, OMIT the field
            - NEVER fabricate values
            - NEVER ask follow-up questions
            - Returning {} is VALID
            
            ====================
            SCHEMA (AUTHORITATIVE)
            ====================
            """);

                    sb.append(fullSchema.toPrettyString()).append("\n\n");

                    sb.append("""
            ====================
            CURRENT_STATE_JSON
            ====================
            """);

                    sb.append(existingJson == null ? "{}" : existingJson.toPrettyString())
                            .append("\n\n");

                    sb.append("""
            ====================
            ALREADY_ASKED_FIELDS
            (Do NOT extract these unless explicitly answered)
            ====================
            """);

                    sb.append(alreadyAskedFields == null ? "[]" : alreadyAskedFields)
                            .append("\n\n");

                    if (targetFields != null && !targetFields.isEmpty()) {
                        sb.append("""
            ====================
            TARGET_FIELDS
            (Focus ONLY on these if present)
            ====================
            """);
                        sb.append(targetFields).append("\n\n");
                    }

                    sb.append("""
            ====================
            USER_MESSAGE
            ====================
            """
                    );

        sb.append("\"\"\"\n")
                .append(userText)
                .append("\n\"\"\"\n\n");

        sb.append("""
            ====================
            OUTPUT FORMAT
            ====================
            Return a JSON object containing:
            1. Extracted fields only (partial patch)
            2. A top-level "_confidence" object with 0.0–1.0 per returned field
            
            If no fields can be extracted, return {} exactly.
            """
        );

        return sb.toString();
    }


}