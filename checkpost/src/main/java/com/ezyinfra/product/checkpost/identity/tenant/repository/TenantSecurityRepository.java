package com.ezyinfra.product.checkpost.identity.tenant.repository;

import com.ezyinfra.product.checkpost.identity.tenant.entity.TenantSecurity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TenantSecurityRepository
        extends JpaRepository<TenantSecurity, String> {
}