package com.ezyinfra.product.checkpost.identity.tenant.service.impl;

import com.ezyinfra.product.checkpost.identity.tenant.entity.*;
import com.ezyinfra.product.checkpost.identity.tenant.model.*;
import com.ezyinfra.product.checkpost.identity.tenant.repository.TenantContactRepository;
import com.ezyinfra.product.checkpost.identity.tenant.repository.TenantRepository;
import com.ezyinfra.product.checkpost.identity.tenant.repository.TenantSecurityRepository;
import com.ezyinfra.product.checkpost.identity.tenant.repository.TenantSigningKeyRepository;
import com.ezyinfra.product.checkpost.identity.tenant.service.TenantOnboardingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TenantOnboardingServiceImpl
        implements TenantOnboardingService {

    private final TenantRepository tenantRepository;
    private final TenantSecurityRepository tenantSecurityRepository;
    private final TenantSigningKeyRepository signingKeyRepository;
    private final TenantContactRepository contactRepository;

    @Override
    @Transactional
    public TenantOnboardingResponse onboardTenant(
            TenantOnboardingRequest request) {

        tenantRepository.findByCode(request.getCode())
                .ifPresent(t -> {
                    throw new IllegalStateException(
                            "Tenant already exists: " + request.getCode()
                    );
                });

        String tenantId = request.getCode().toUpperCase();

        Tenant tenant = Tenant.builder()
                .id(tenantId)
                .code(request.getCode())
                .name(request.getName())
                .status(TenantStatus.ACTIVE)
                .tier(TenantTier.ENTERPRISE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .metadata(Map.of("source", "ONBOARDING_API"))
                .build();

        tenantRepository.save(tenant);

        TenantSigningKey key = TenantSigningKey.builder()
                .id(UUID.randomUUID().toString())
                .tenant(tenant)
                .algorithm(JwtAlgorithm.RS256)
                .status(KeyStatus.ACTIVE)
                .createdAt(Instant.now())
                .build();

        signingKeyRepository.save(key);

        TenantSecurity security = TenantSecurity.builder()
                .tenant(tenant)
                .jwtIssuer("https://auth.ezyinfra.com/" + tenantId)
                .jwtAudience("ezyinfra-api")
                .accessTokenTtl(900)
                .refreshTokenTtl(604800)
                .signingKeyId(key.getId())
                .rotationPolicy(KeyRotationPolicy.AUTO)
                .lastRotatedAt(Instant.now())
                .build();

        tenantSecurityRepository.save(security);

        if (request.getAdminEmail() != null) {
            contactRepository.save(
                    TenantContact.builder()
                            .id(new TenantContactId(
                                    tenantId,
                                    ContactType.EMAIL,
                                    request.getAdminEmail()))
                            .tenant(tenant)
                            .build()
            );
        }

        if (request.getAdminMobile() != null) {
            contactRepository.save(
                    TenantContact.builder()
                            .id(new TenantContactId(
                                    tenantId,
                                    ContactType.MOBILE,
                                    request.getAdminMobile()))
                            .tenant(tenant)
                            .build()
            );
        }

        log.info("Tenant [{}] onboarded successfully", tenantId);

        return TenantOnboardingResponse.builder()
                .tenantId(tenantId)
                .code(request.getCode())
                .jwtIssuer(security.getJwtIssuer())
                .activeKeyId(key.getId())
                .build();
    }
}