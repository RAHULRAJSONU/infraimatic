package com.ezyinfra.product.infraimatic.data.dto;

import java.time.Instant;

public record ApprovalTaskDto(
    Long taskId,
    int level,
    String approverType,
    String approver,
    String status,
    Instant dueAt,
    boolean escalated) {}
