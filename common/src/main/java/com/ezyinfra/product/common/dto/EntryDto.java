package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * DTO representing a stored submission. This excludes the raw payload and
 * internal metadata for security reasons.
 */
public record EntryDto(
        UUID id,
        String tenantId,
        String type,
        Integer templateVersion,
        JsonNode normalized
) {
}