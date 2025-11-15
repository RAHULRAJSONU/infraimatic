package com.ezyinfra.product.configuration;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.service.IdentityService;
import lombok.NonNull;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @NonNull
    @Override
    public Optional<String> getCurrentAuditor() {
        return IdentityService.getLoggedInUser().map(User::getUsername);
    }

}