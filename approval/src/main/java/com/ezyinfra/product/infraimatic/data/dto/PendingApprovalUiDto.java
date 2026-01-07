package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PendingApprovalUiDto(
        UUID instanceId,
        String entityType,
        String entityId,
        int level,
        int approvalGroup,
        String assignmentType,   // FIXED | RUNTIME_SINGLE | RUNTIME_MULTI
        boolean requireAll,
        Instant dueAt,
        List<String> allowedActions
) {}


