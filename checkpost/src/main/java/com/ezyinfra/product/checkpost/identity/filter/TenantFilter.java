package com.ezyinfra.product.checkpost.identity.filter;

import com.ezyinfra.product.checkpost.identity.config.TenantExcludedPathMatcher;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class TenantFilter extends OncePerRequestFilter {

    private final TenantExcludedPathMatcher excludedMatcher;

    public TenantFilter(TenantExcludedPathMatcher excludedMatcher) {
        this.excludedMatcher = excludedMatcher;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/webhooks/") || excludedMatcher.isExcluded(request);
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String tenantId = request.getHeader("X-Tenant-Id");

        if (tenantId == null || tenantId.isBlank()) {
            // No tenant → proceed without binding
            filterChain.doFilter(request, response);
            return;
        }

        log.info("Binding tenant [{}] using ScopedValue", tenantId);

        TenantContext.executeInTenantContext(
                tenantId,
                () -> {
                    try {
                        filterChain.doFilter(request, response);
                    } catch (IOException | ServletException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
    }
}
