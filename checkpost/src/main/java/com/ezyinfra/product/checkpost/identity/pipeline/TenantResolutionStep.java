package com.ezyinfra.product.checkpost.identity.pipeline;

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

    private final TenantResolverService tenantResolverService;
    private final JdbcTemplate jdbcTemplate;

    public TenantResolutionStep(TenantResolverService tenantResolverService, JdbcTemplate jdbcTemplate) {
        this.tenantResolverService = tenantResolverService;
        this.jdbcTemplate = jdbcTemplate;
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
            String tenantId = resolveTenantByMobile(mobile)
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

    private Optional<String> resolveTenantByMobile(String mobile) {
        String sql = """
            SELECT tenant_id
            FROM identity_user
            WHERE phone_number = ?
              AND status = 'ACTIVE'
        """;
        return jdbcTemplate.query(
                sql,
                ps -> ps.setString(1, mobile),
                rs -> rs.next() ? Optional.of(rs.getString("tenant_id")) : Optional.empty()
        );
    }
}