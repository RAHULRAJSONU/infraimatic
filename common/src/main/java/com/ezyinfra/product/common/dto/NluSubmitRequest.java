package com.ezyinfra.product.common.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to submit a free text via NLU. The service will parse and persist
 * the resulting normalized submission.
 */
public record NluSubmitRequest(@NotBlank String text) {
}