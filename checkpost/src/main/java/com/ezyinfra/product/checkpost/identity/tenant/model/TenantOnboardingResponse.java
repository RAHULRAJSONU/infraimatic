package com.ezyinfra.product.checkpost.identity.tenant.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TenantOnboardingResponse {

    private String tenantId;
    private String code;
    private String jwtIssuer;
    private String activeKeyId;
}