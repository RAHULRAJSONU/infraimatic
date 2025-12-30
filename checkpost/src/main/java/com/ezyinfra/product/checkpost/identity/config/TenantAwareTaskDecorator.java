package com.ezyinfra.product.checkpost.identity.config;

import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
import org.springframework.core.task.TaskDecorator;
import org.springframework.lang.NonNull;

public class TenantAwareTaskDecorator implements TaskDecorator {

    @Override
    @NonNull
    public Runnable decorate(@NonNull Runnable runnable) {
        String tenantId = TenantContext.getCurrentTenantId();
        return () -> {
            try {
                if (tenantId == null) {
                    runnable.run();
                    return;
                }
                TenantContext.executeInTenantContext(tenantId, runnable);
            } finally {

            }
        };
    }
}