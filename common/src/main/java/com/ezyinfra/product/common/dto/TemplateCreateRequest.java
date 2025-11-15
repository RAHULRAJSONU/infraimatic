package com.ezyinfra.product.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request body for creating a new template version. Requires a non blank name
 * and a JSON schema for validation of normalized submissions.
 */
public record TemplateCreateRequest(
        @JsonProperty("name") String name,
        @JsonProperty("jsonSchema") JsonNode jsonSchema
) {
}