package com.ezyinfra.product.checkpost.identity.crypto;

public record PasswordStrengthResult(
        boolean valid,
        String message
) {}
