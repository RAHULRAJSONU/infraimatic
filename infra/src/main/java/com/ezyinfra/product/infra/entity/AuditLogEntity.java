package com.ezyinfra.product.infra.entity;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_tenant_entity", columnList = "tenant_id,entity_type,entity_id"),
                @Index(name = "idx_audit_created_at", columnList = "created_at")
        }
)
@EqualsAndHashCode
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", length = 100)
    private String tenantId;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "actor", length = 100)
    private String actor;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "before", columnDefinition = "jsonb")
    private JsonNode before;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "after", columnDefinition = "jsonb")
    private JsonNode after;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
