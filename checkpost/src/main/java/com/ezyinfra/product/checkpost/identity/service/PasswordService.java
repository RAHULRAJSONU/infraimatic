package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.model.ChangePasswordRequest;
import org.springframework.http.ResponseEntity;

public interface PasswordService {

    String generateResetToken();

    boolean validatePasswordStrength(String password);

    ResponseEntity<?> changePassword(ChangePasswordRequest request, String email);


    ResponseEntity<?> sendResetPasswordLink(String userEmail);

    ResponseEntity<?> resetPassword(String token, String password);
}
