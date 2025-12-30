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
    name = "tenant_contacts",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_contact_type_value", columnNames = {"type", "value"})
    }
)
@Data
public class TenantContact {

    @EmbeddedId
    private TenantContactId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tenantId")
    @JoinColumn(name = "tenant_id")
    private Tenant tenant;
}