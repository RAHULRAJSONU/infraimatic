package com.ezyinfra.product.checkpost.identity.data.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Base64;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ChangePasswordRequest {
    String currentPassword;
    String newPassword;
    String confirmPassword;


    public ChangePasswordRequest getDecodedPassword() {
        String decodedCurrentPassword = decodeBase64(currentPassword);
        String decodedNewPassword = decodeBase64(newPassword);
        String decodedConfirmPassword = decodeBase64(confirmPassword);
        return new ChangePasswordRequest(decodedCurrentPassword, decodedNewPassword, decodedConfirmPassword);
    }

    private String decodeBase64(String encodedPassword) {
        byte[] passwordBytes = Base64.getDecoder().decode(encodedPassword);
        return new String(passwordBytes);
    }
}
