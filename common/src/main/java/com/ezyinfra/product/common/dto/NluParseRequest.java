package com.ezyinfra.product.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO representing a request to parse natural language input. Contains the
 * raw free text that will be processed by the NLU service.
 */
public record NluParseRequest(@NotBlank String text) {
}