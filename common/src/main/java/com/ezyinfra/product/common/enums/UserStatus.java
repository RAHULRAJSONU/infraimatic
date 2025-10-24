package com.ezyinfra.product.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
@RequiredArgsConstructor
public enum UserStatus {
    ACTIVE("User is active."),
    VERIFICATION_PENDING("User account verification is pending, please check your email and verify it."),
    CREDENTIALS_EXPIRED("User credential is expired, please try resetting it."),
    PERM_SUSPENDED("User account is suspended permanently."),
    LOCKED("User account is locked."),
    TEMP_SUSPENDED("User account is suspended temporarily.");

    private final String message;

    public static UserStatus get(String statusStr) {
        try {
            return UserStatus.valueOf(statusStr);
        } catch (Exception e) {
            log.error("Invalid user status: {}, error: {}", statusStr, e.getMessage());
            return null;
        }
    }
}
