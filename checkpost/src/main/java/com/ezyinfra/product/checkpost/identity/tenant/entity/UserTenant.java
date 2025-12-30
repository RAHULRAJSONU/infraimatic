package com.ezyinfra.product.checkpost.identity.tenant.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
    name = "user_tenants",
    indexes = {
        @Index(name = "idx_user_tenant_user", columnList = "user_id"),
        @Index(name = "idx_user_tenant_tenant", columnList = "tenant_id")
    }
)
@Data
public class UserTenant {

    @EmbeddedId
    private UserTenantId id;

    @Column(length = 64)
    private String role;
}