package com.ezyinfra.product.infraimatic.data.dto;

import java.util.UUID;

public record AttachApprovalRequest(
        String entityType,
        String entityId,
        UUID templateId) {}
