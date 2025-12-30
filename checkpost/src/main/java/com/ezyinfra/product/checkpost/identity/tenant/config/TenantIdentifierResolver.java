package com.ezyinfra.product.checkpost.identity.tenant.config;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class TenantIdentifierResolver
        implements CurrentTenantIdentifierResolver<String>, HibernatePropertiesCustomizer {

    private static final String SYSTEM_TENANT = "SYSTEM";

    @Override
    public String resolveCurrentTenantIdentifier() {

        if (!TenantContext.isBound()) {
            return SYSTEM_TENANT;
        }

        return TenantContext.getCurrentTenantId();
    }

    @Override
    public boolean validateExistingCurrentSessions() {
        return true;
    }

    @Override
    public void customize(Map<String, Object> hibernateProperties) {
        hibernateProperties.put(
                AvailableSettings.MULTI_TENANT_IDENTIFIER_RESOLVER,
                this
        );
    }
}

