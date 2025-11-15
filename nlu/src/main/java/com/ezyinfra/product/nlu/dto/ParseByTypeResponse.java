package com.ezyinfra.product.nlu.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParseByTypeResponse {
    public JsonNode parsed;       // LLM-extracted JSON (raw)
    public Map<String,Double> fieldConfidence;
    public List<String> validationErrors;
    public String rawLLMOutput;
    public List<String> warnings;
}