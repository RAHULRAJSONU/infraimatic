package com.ezyinfra.product.infraimatic.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Data
@Entity
@Table(name = "approval_attachments",
       uniqueConstraints = @UniqueConstraint(columnNames = {"entity_type", "entity_id"}))
public class ApprovalAttachment extends AbstractPersistable{

    private String entityType; // e.g. "GATEPASS"
    private String entityId;   // the id of the entity as string
    @ManyToOne
    private ApprovalTemplate template;
}