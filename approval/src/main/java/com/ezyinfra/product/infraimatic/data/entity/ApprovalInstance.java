package com.ezyinfra.product.infraimatic.data.entity;

import com.ezyinfra.product.infraimatic.data.dto.ApprovalInstanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "approval_instances")
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ApprovalInstance extends AbstractPersistable{
    private String entityType;
    private String entityId;
    @Enumerated(EnumType.STRING)
    private ApprovalInstanceStatus status;
    @ManyToOne
    @JsonIgnore
    private ApprovalTemplate template;
    private boolean locked;
    @OneToMany(
            mappedBy = "instance",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("levelOrder ASC")
    @JsonIgnore
    private List<ApprovalTask> tasks = new ArrayList<>();
}