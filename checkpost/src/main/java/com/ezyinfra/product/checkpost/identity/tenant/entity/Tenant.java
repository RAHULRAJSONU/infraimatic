package com.ezyinfra.product.checkpost.identity.tenant.entity;

import com.ezyinfra.product.checkpost.identity.tenant.model.TenantStatus;
import com.ezyinfra.product.checkpost.identity.tenant.model.TenantTier;
import com.ezyinfra.product.checkpost.identity.util.JsonMapConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "tenants",
    indexes = {
        @Index(name = "idx_tenant_code", columnList = "code"),
        @Index(name = "idx_tenant_status", columnList = "status")
    }
)
@Data
public class Tenant {

    @Id
    @Column(length = 64)
    private String id; // TENANT_ACME

    @Column(length = 64, nullable = false, unique = true)
    private String code;

    @Column(length = 255, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private TenantStatus status;

    @Enumerated(EnumType.STRING)
    @Column(length = 32, nullable = false)
    private TenantTier tier;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    @Column
    private String createdBy;

    @Convert(converter = JsonMapConverter.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
}