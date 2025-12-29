package com.ezyinfra.product.common.dto;

public record PhoneNumberParts(
        String rawInput,
        String countryCode,
        String nationalNumber,
        String region,
        String e164,
        boolean valid
) {}
