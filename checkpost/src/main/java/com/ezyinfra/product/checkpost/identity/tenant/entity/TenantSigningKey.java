package com.ezyinfra.product.checkpost.identity.tenant.entity;

import com.ezyinfra.product.checkpost.identity.tenant.model.JwtAlgorithm;
import com.ezyinfra.product.checkpost.identity.tenant.model.KeyStatus;
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
@Table(
    name = "tenant_signing_keys",
    indexes = {
        @Index(name = "idx_key_tenant", columnList = "tenant_id"),
        @Index(name = "idx_key_status", columnList = "status")
    }
)
@Data
public class TenantSigningKey {

    @Id
    @Column(length = 64)
    private String id; // kid

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", nullable = false)
    private Tenant tenant;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private JwtAlgorithm algorithm;

    @Column(columnDefinition = "text")
    private String secret;       // HMAC

    @Column(columnDefinition = "text")
    private String publicKey;    // RSA / EC

    @Column(columnDefinition = "text")
    private String privateKey;

    @Enumerated(EnumType.STRING)
    @Column(length = 16, nullable = false)
    private KeyStatus status;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}