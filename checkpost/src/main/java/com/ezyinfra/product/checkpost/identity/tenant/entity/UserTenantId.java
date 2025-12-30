package com.ezyinfra.product.checkpost.identity.tenant.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class UserTenantId {

    @Column(nullable = false)
    private UUID userId;

    @Column(length = 64, nullable = false)
    private String tenantId;
}
