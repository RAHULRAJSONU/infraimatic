package com.ezyinfra.product.nlu.dto;

import com.ezyinfra.product.nlu.service.ParseMode;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParseByTypeRequest {
    private String type;    // template type key, e.g. "visitor-entry"
    private String text;    // free-form text input
    /** Existing structured data collected so far */
    private JsonNode existingData;

    /** Fields we want the LLM to focus on */
    private Set<String> targetFields;

    private ParseMode mode;
    private Map<String,Object> options; // gpNumberPrefix, strictValidation, timezone...
}