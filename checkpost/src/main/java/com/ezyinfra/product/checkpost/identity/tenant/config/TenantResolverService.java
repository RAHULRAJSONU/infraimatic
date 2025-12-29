package com.ezyinfra.product.checkpost.identity.tenant.config;

import java.util.Optional;

public interface TenantResolverService {

    Optional<String> resolveTenantByMobile(String mobileNumber);
}
