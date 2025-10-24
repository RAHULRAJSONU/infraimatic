package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Response from parsing natural language. Contains the recommended template
 * type and version, the normalized payload representation and a confidence
 * score indicating how certain the parser is about the extracted structure.
 */
public record NluParseResponse(
        String templateType,
        Integer templateVersion,
        JsonNode normalized,
        double confidence
) {
}