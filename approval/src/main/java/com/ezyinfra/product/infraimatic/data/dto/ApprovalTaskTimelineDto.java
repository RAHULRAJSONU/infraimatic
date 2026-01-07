package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Instant;

public record ApprovalTaskTimelineDto(
        int levelOrder,
        int approvalGroup,
        String approverType,
        String approver,
        ApprovalTaskStatus status,
        Instant actedAt,
        String actedBy,
        String comment
) {}
