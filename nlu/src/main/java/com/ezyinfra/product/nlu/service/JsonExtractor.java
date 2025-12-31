package com.ezyinfra.product.nlu.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonExtractor {

    private final ObjectMapper mapper;

    public JsonExtractor(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public JsonNode safeParseOrEmpty(String raw) {
        try {
            String cleaned = raw
                    .replaceAll("(?s)```json", "")
                    .replaceAll("(?s)```", "")
                    .trim();

            int s = cleaned.indexOf('{');
            int e = cleaned.lastIndexOf('}');
            if (s < 0 || e <= s) {
                return mapper.createObjectNode();
            }
            return mapper.readTree(cleaned.substring(s, e + 1));
        } catch (Exception e) {
            return mapper.createObjectNode();
        }
    }
}
