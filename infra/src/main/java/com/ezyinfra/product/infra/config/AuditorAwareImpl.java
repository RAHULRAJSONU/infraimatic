package com.ezyinfra.product.infra.config;

import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
//        return IdentityService.getLoggedInUser().map(User::getUsername);
        return Optional.of("SYSTEM");
    }

}