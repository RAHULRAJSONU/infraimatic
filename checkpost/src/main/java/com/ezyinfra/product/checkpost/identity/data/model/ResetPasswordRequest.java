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
public class ResetPasswordRequest {
    String newPassword;
    String confirmPassword;

    public ResetPasswordRequest getDecodedPassword() {
        String decodedNewPassword = decodeBase64(newPassword);
        String decodedConfirmPassword = decodeBase64(confirmPassword);
        return new ResetPasswordRequest(decodedNewPassword, decodedConfirmPassword);
    }

    private String decodeBase64(String encodedPassword) {
        byte[] passwordBytes = Base64.getDecoder().decode(encodedPassword);
        return new String(passwordBytes);
    }
}
