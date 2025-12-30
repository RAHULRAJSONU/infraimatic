package com.ezyinfra.product.checkpost.identity.tenant.service;

import com.ezyinfra.product.checkpost.identity.tenant.model.TenantOnboardingRequest;
import com.ezyinfra.product.checkpost.identity.tenant.model.TenantOnboardingResponse;

public interface TenantOnboardingService {

    TenantOnboardingResponse onboardTenant(TenantOnboardingRequest request);
}