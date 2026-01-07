package com.ezyinfra.product.infraimatic.data.entity;

import com.ezyinfra.product.infraimatic.data.dto.AssignmentStrategy;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "approval_integrations")
public class ApprovalIntegrationConfig extends AbstractPersistable{

    private String entityType;
    private boolean enabled;

    private UUID approvalTemplateId;

    @Enumerated(EnumType.STRING)
    private AssignmentStrategy assignmentStrategy;

    private boolean requireAllApprovals;
    private int levelOrder;
    private boolean allowConsumerApproverOverride;
    private boolean requireApproverInput;
}
