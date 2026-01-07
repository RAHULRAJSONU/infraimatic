package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;

public record ApprovalTemplateRequest(
    String name,
    String description,
    List<ApprovalLevelRequest> levels
) {}
