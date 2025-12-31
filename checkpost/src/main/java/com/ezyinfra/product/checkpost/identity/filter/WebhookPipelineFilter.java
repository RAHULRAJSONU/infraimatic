package com.ezyinfra.product.checkpost.identity.filter;

import com.ezyinfra.product.checkpost.identity.pipeline.WebhookContext;
import com.ezyinfra.product.checkpost.identity.pipeline.WebhookPipeline;
import com.ezyinfra.product.checkpost.identity.pipeline.WebhookType;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantScope;
import com.ezyinfra.product.common.exception.WebhookIgnoredException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class WebhookPipelineFilter extends OncePerRequestFilter {

    private final WebhookPipeline pipeline;

    public WebhookPipelineFilter(WebhookPipeline pipeline) {
        this.pipeline = pipeline;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/webhooks/whatsapp");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws IOException {
        log.info("Filtering requests at WebhookPipelineFilter.");
        if (isWebhookCallback(request)) {
            log.info("Ignoring twillio callback request.");
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }
        try {
            WebhookContext context =
                    new WebhookContext(WebhookType.TWILIO_WHATSAPP, request);

            // 1️⃣ Execute pipeline (resolves sender + tenant)
            pipeline.process(context);

            String tenantId = context.getTenantId();

            if (tenantId == null || tenantId.isBlank()) {
                throw new IllegalStateException(
                        "Tenant not resolved for webhook request"
                );
            }

            // 2️⃣ Bind tenant for the rest of request lifecycle
            TenantScope.run(
                    context.getTenantId(),
                    () -> {
                        try {
                            filterChain.doFilter(request, response);
                        } catch (IOException | ServletException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );


        } catch (WebhookIgnoredException ignored) {
            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().write("Ignored");
        }
    }

    private boolean isWebhookCallback(HttpServletRequest request) {
        String messageStatus = request.getParameter("MessageStatus");
        String smsStatus = request.getParameter("SmsStatus");
        log.info("MessageStatus: {}, smsStatus: {}",messageStatus,smsStatus);
        return ((messageStatus != null && !messageStatus.equalsIgnoreCase("received")) ||
                (smsStatus != null && !smsStatus.equalsIgnoreCase("received"))
        );
    }
}
