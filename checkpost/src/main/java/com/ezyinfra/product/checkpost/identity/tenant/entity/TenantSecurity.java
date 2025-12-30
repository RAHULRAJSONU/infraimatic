package com.ezyinfra.product.checkpost.identity.tenant.entity;

import com.ezyinfra.product.checkpost.identity.tenant.model.KeyRotationPolicy;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tenant_security")
@Data
public class TenantSecurity {

    @Id
    @Column(length = 64)
    private String tenantId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;

    @Column(nullable = false)
    private String jwtIssuer;

    @Column(nullable = false)
    private String jwtAudience;

    @Column(nullable = false)
    private int accessTokenTtl;   // seconds

    @Column(nullable = false)
    private int refreshTokenTtl;  // seconds

    @Column(nullable = false, length = 64)
    private String signingKeyId; // active kid

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private KeyRotationPolicy rotationPolicy;

    private Instant lastRotatedAt;
}