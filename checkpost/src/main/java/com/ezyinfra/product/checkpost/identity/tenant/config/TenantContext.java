package com.ezyinfra.product.checkpost.identity.tenant.config;

import java.util.NoSuchElementException;
import java.util.function.Supplier;

public final class TenantContext {

    private TenantContext() {}

    private static final ScopedValue<String> TENANT_ID =
            ScopedValue.newInstance();

    public static void executeInTenantContext(
            String tenantId,
            Runnable operation) {

        if (TENANT_ID.isBound()) {
            throw new IllegalStateException("Tenant already bound");
        }

        ScopedValue.where(TENANT_ID, tenantId).run(operation);
    }

    public static <T> T executeInTenantContext(
            String tenantId,
            Supplier<T> operation) {

        return ScopedValue.where(TENANT_ID, tenantId)
                .call(operation::get);
    }

    public static String getCurrentTenantId() {
        if (!TENANT_ID.isBound()) {
            throw new NoSuchElementException(
                    "Tenant context is not bound"
            );
        }
        return TENANT_ID.get();
    }

    public static boolean isBound(){
        return TENANT_ID.isBound();
    }
}
