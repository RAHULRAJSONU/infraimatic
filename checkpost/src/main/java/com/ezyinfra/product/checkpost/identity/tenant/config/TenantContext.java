package com.ezyinfra.product.checkpost.identity.tenant.config;

public final class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();

    private TenantContext() {}

    public static void set(String tenantId) {
        TENANT.set(tenantId);
    }

    public static String get() {
        return TENANT.get();
    }

    public static boolean isBound() {
        return TENANT.get() != null;
    }

    public static void clear() {
        TENANT.remove();
    }
}
