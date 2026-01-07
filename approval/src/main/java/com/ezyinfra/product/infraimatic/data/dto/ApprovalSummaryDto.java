package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Instant;
import java.util.UUID;

public record ApprovalSummaryDto(
    UUID instanceId,
    String entityType,
    String entityId,
    ApprovalInstanceStatus status,
    boolean locked,
    Instant createdAt) {}
