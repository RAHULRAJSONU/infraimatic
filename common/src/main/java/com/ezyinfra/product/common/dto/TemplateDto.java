package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Data transfer object for template definitions. Represented in API responses.
 */
public record TemplateDto(
        UUID id,
        String type,
        Integer version,
        String name,
        JsonNode jsonSchema
) {
}