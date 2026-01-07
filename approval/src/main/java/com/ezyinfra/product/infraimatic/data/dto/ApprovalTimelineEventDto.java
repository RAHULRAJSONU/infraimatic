package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Instant;

public record ApprovalTimelineEventDto(
    int level,
    int group,
    String approver,
    String approverType,
    ApprovalTaskStatus status,
    Instant actedAt,
    String actedBy,
    String comment
) {}
