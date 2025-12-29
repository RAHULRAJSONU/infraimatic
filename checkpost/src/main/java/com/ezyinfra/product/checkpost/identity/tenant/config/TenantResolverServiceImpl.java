package com.ezyinfra.product.checkpost.identity.tenant.config;

import com.ezyinfra.product.checkpost.identity.service.UserService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TenantResolverServiceImpl implements TenantResolverService {

    private final UserService userService;

    public TenantResolverServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Optional<String> resolveTenantByMobile(String mobileNumber) {
        return userService.findTenantIdByMobile(mobileNumber);
    }
}
