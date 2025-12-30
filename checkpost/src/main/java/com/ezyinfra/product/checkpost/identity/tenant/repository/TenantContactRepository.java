package com.ezyinfra.product.checkpost.identity.tenant.repository;

import com.ezyinfra.product.checkpost.identity.tenant.entity.TenantContact;
import com.ezyinfra.product.checkpost.identity.tenant.entity.TenantContactId;
import com.ezyinfra.product.checkpost.identity.tenant.model.ContactType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TenantContactRepository
        extends JpaRepository<TenantContact, TenantContactId> {

    Optional<TenantContact> findById_TypeAndId_Value(
            ContactType type,
            String value
    );
}