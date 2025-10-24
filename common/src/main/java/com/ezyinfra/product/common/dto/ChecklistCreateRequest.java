package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a new checklist.  Contains the name,
 * optional companyId, norm reference and frequency, and an array of
 * check point definitions stored as JSON.
 */
public record ChecklistCreateRequest(
        @NotBlank String name,
        String companyId,
        String normReference,
        String frequency,
        @NotNull JsonNode checkPoints
) {}
