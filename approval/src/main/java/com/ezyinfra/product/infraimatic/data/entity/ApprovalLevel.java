package com.ezyinfra.product.infraimatic.data.entity;

import com.ezyinfra.product.infraimatic.data.dto.AssignmentStrategy;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.Duration;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "approval_levels")
@EqualsAndHashCode(callSuper = true)
public class ApprovalLevel extends AbstractPersistable{

    private int levelOrder;
    private String approverType; // USER | ROLE
    private String approver;     // userId or roleName
    private Duration slaDuration;      // e.g. PT4H
    private Duration reminderBefore;   // e.g. PT30M
    private String escalationRole;
    @ManyToOne(optional = false)
    @JsonIgnore
    private ApprovalTemplate template;
    @Enumerated(EnumType.STRING)
    private AssignmentStrategy assignmentStrategy;
    // For multi-user approvals
    private boolean requireAllApprovals; // true = ALL, false = ANY
}