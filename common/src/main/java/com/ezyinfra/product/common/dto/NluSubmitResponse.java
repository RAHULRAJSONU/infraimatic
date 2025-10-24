package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

/**
 * Response for NLU submit. Contains the persisted record identifier and
 * optionally the normalized payload returned after processing.
 */
public record NluSubmitResponse(UUID submissionId, JsonNode normalized) {
}