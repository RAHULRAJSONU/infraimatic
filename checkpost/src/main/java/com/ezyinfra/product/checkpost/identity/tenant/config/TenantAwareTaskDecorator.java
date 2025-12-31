package com.ezyinfra.product.checkpost.identity.tenant.config;

import org.springframework.core.task.TaskDecorator;

public class TenantAwareTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        String tenantId = TenantContext.get();

        return () -> {
            if (tenantId != null) {
                TenantContext.set(tenantId);
            }
            try {
                runnable.run();
            } finally {
                TenantContext.clear();
            }
        };
    }
}