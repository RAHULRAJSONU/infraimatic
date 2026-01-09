package com.ezyinfra.product.checkpost.identity.util;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static User getUser() {
        Authentication auth =
            SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User)) {
            return null;
        }
        return (User) auth.getPrincipal();
    }

    public static String tenantId() {
        return getUser().getTenantId();
    }
}
