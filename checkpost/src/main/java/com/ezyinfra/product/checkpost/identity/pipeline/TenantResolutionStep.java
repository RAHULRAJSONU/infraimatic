package com.ezyinfra.product.checkpost.identity.pipeline;

import com.ezyinfra.product.checkpost.identity.service.TenantService;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantResolverService;
import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.common.exception.WebhookIgnoredException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Slf4j
@Component
public class TenantResolutionStep implements WebhookPipelineStep {

    private final TenantService tenantService;

    public TenantResolutionStep(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @Override
    public boolean supports(WebhookType type) {
        return type == WebhookType.TWILIO_WHATSAPP;
    }

    @Override
    public void execute(WebhookContext ctx) {

        log.info("Executing TenantResolutionStep with WebhookContext: {}", ctx);

        String mobile = ctx.getSenderMobile();

        if (mobile == null || mobile.isBlank()) {
            throw new WebhookIgnoredException(
                    "Tenant resolution skipped: sender mobile missing"
            );
        }
        try {
            String tenantId = tenantService.resolveTenantByMobile(mobile)
                    .orElseThrow(() ->
                            new AuthException(
                                    "No tenant mapped for mobile: " + mobile
                            )
                    );

            // ✅ ONLY update context (NO global mutation)
            ctx.setTenantId(tenantId);
        }catch (Exception ex){
            ex.printStackTrace();
        }
    }

}