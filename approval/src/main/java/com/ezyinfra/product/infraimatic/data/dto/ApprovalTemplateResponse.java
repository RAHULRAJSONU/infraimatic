package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;
import java.util.UUID;

public record ApprovalTemplateResponse(
    UUID id,
    String name,
    String description,
    boolean active,
    List<ApprovalLevelResponse> levels
) {}
