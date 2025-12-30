package com.ezyinfra.product.checkpost.identity.tenant.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantOnboardingRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private String adminEmail;
    private String adminMobile;
}