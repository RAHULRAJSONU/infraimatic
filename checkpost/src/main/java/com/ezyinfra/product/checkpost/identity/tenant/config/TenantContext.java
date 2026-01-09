package com.ezyinfra.product.checkpost.identity.tenant.config;

import com.ezyinfra.product.checkpost.identity.data.entity.User;

public final class TenantContext {

    private static final ThreadLocal<String> TENANT = new ThreadLocal<>();
    private static final ThreadLocal<User> CURRENT_USER = new ThreadLocal<>();

    private TenantContext() {}

    public static void bind(String tenantId) {
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
        CURRENT_USER.remove();
    }

    public static void bindUser(User user) {
        CURRENT_USER.set(user);
    }

    public static User getUser() {
        return CURRENT_USER.get();
    }
}
