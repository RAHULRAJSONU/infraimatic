package com.ezyinfra.product.checkpost.identity.data.record;

import com.ezyinfra.product.common.enums.Gender;
import com.ezyinfra.product.common.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDate;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserCreateRecord(
        UUID id,
        String givenName,
        String middleName,
        String familyName,
        String nickname,
        String preferredUsername,
        String address,
        String email,
        Gender gender,
        LocalDate birthdate,
        String zoneInfo,
        String website,
        String username,
        String password,
        String phoneNumber,
        String apiKey,
        String apiSecret,
        String picture,
        String locale,
        UserStatus status) {

}