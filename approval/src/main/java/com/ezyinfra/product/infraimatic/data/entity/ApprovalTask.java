package com.ezyinfra.product.infraimatic.data.entity;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalTaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

@Data
@Entity
@Table(name = "approval_tasks")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApprovalTask extends AbstractPersistable{

    private int levelOrder;
    private String approverType;
    private String approver;
    @Enumerated(EnumType.STRING)
    private ApprovalTaskStatus status;
    private Instant actedAt;
    private String actedBy;
    private String comment;
    private Instant dueAt;
    private Instant reminderAt;
    private boolean escalated;
    private String escalationRole;
    private Instant escalatedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "instance_id", nullable = false)
    @JsonIgnore
    private ApprovalInstance instance;
    private int approvalGroup;
}