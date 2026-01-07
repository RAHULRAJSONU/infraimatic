package com.ezyinfra.product.infraimatic.data.dto;

import java.util.UUID;

public record ApprovalIntegrationRequest(
    String entityType,
    boolean enabled,
    UUID approvalTemplateId,
    AssignmentStrategy assignmentStrategy,
    boolean requireAllApprovals,
    int levelOrder
) {}
