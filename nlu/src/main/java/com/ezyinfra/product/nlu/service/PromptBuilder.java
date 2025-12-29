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
            Set<String> targetFields
    ) {
        StringBuilder sb = new StringBuilder();

        sb.append("You are a strict JSON patch generator.\n");
        sb.append("Your task is to extract ONLY the information explicitly present in the USER MESSAGE.\n");
        sb.append("Do NOT repeat fields already present unless updating them.\n");
        sb.append("Do NOT hallucinate values.\n");
        sb.append("Return JSON ONLY.\n\n");

        sb.append("RULES:\n");
        sb.append("- Use ONLY property names from the SCHEMA\n");
        sb.append("- Include only fields you can confidently extract from the message\n");
        sb.append("- If a field is not mentioned, OMIT it\n");
        sb.append("- Use null ONLY if the user explicitly indicates absence\n");
        sb.append("- Include a top-level _confidence map (0..1) ONLY for returned fields\n");
        sb.append("- Dates must be ISO-8601\n\n");

        sb.append("SCHEMA:\n")
                .append(fullSchema.toPrettyString())
                .append("\n\n");

        sb.append("CURRENT_JSON:\n")
                .append(existingJson == null ? "{}" : existingJson.toPrettyString())
                .append("\n\n");

        if (targetFields != null && !targetFields.isEmpty()) {
            sb.append("TARGET_FIELDS (focus on these if present):\n")
                    .append(targetFields)
                    .append("\n\n");
        }

        sb.append("USER_MESSAGE:\n\"\"\"\n")
                .append(userText)
                .append("\n\"\"\"\n\n");

        sb.append("Return JSON PATCH only.");

        return sb.toString();
    }

}