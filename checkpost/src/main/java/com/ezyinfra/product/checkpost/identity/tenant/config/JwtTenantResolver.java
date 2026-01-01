package com.ezyinfra.product.checkpost.identity.tenant.config;

import com.ezyinfra.product.checkpost.identity.service.JwtService;
import com.ezyinfra.product.common.utility.AppConstant;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtTenantResolver {

    private final JwtService jwtService;

    public JwtTenantResolver(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Optional<String> resolveTenant(String token) {

        if (token == null) {
            return Optional.empty();
        }

        try {
            String tenant =
                    jwtService.extractClaim(
                        token,
                        claims -> (String) claims.get(AppConstant.Jwt.TENANT_ID)
                    );

            return Optional.ofNullable(tenant);

        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
