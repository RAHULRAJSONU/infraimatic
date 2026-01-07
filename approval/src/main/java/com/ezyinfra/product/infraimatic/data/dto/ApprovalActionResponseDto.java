package com.ezyinfra.product.infraimatic.data.dto;

import java.util.List;
import java.util.UUID;

public record ApprovalActionResponseDto(
        UUID instanceId,
        String entityType,
        String entityId,
        ApprovalInstanceStatus status,
        boolean locked,
        List<ApprovalTaskTimelineDto> timeline
) {}
