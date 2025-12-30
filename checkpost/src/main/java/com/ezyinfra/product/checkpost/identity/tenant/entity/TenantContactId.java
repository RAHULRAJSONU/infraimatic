package com.ezyinfra.product.checkpost.identity.tenant.entity;

import com.ezyinfra.product.checkpost.identity.tenant.model.ContactType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class TenantContactId {

    @Column(length = 64)
    private String tenantId;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private ContactType type;

    @Column(length = 128)
    private String value;
}