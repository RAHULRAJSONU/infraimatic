package com.ezyinfra.product.checkpost.identity.config;

import com.ezyinfra.product.common.utility.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class TenantExcludedPathMatcher {

    private final AntPathMatcher matcher = new AntPathMatcher();

    public boolean isExcluded(HttpServletRequest request) {
        String path = request.getRequestURI();
        return AppConstant.authExcludedPath.stream()
                .anyMatch(p -> matcher.match(p, path));
    }
}
