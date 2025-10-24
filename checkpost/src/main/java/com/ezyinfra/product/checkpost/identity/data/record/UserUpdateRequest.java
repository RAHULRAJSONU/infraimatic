package com.ezyinfra.product.checkpost.identity.data.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserUpdateRequest(
        UUID id,
        String givenName,
        String middleName,
        String familyName,
        String nickname,
        String preferredUsername,
        String address,
        String zoneInfo,
        String website,
        String picture,
        String locale) {

}