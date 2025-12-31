package com.ezyinfra.product.checkpost.identity.tenant.config;

import java.util.function.Supplier;

public final class TenantScope {

    private static final ScopedValue<String> TENANT = ScopedValue.newInstance();

    private TenantScope() {}

    public static void run(String tenantId, Runnable task) {
        ScopedValue.where(TENANT, tenantId).run(() -> {
            TenantContext.set(tenantId);
            try {
                task.run();
            } finally {
                TenantContext.clear();
            }
        });
    }

    public static <T> T call(String tenantId, Supplier<T> task) {
        return ScopedValue.where(TENANT, tenantId).call(() -> {
            TenantContext.set(tenantId);
            try {
                return task.get();
            } finally {
                TenantContext.clear();
            }
        });
    }

    /** Optional helper for logging / tracing */
    public static String currentTenant() {
        return TENANT.isBound() ? TENANT.get() : TenantContext.get();
    }
}
