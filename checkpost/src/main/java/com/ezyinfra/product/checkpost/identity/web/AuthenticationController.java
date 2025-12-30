package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.checkpost.identity.data.model.*;
import com.ezyinfra.product.checkpost.identity.data.record.UserCreateRecord;
import com.ezyinfra.product.checkpost.identity.service.AuthenticationService;
import com.ezyinfra.product.checkpost.identity.service.PasswordService;
import com.ezyinfra.product.common.utility.AppConstant;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@RestController
@RequestMapping("/api/v1/identity/authn")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthenticationController {

    private final AuthenticationService service;
    private final PasswordService passwordService;

    @PostMapping(value = "/register", consumes = {"application/json"})
    public ResponseEntity<AuthenticationResponse> userRegister(@Valid @RequestBody UserCreateRecord request,
                                                               @RequestHeader(AppConstant.TENANT_HEADER) String tenantId) {
        return ResponseEntity.ok(service.userRegistration(request, tenantId));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(HttpServletRequest request) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        return ResponseEntity.ok(service.refreshToken(authHeader));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request, Principal principal) {
        return ResponseEntity.ok(passwordService.changePassword(request.getDecodedPassword(), principal.getName()));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPassword resetPassword) {
        return ResponseEntity.ok(passwordService.sendResetPasswordLink(resetPassword.getUserEmail()));
    }

    @PostMapping("/reset-password/{token}")
    public ResponseEntity<?> resetPassword(@PathVariable String token, @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordService.resetPassword(request.getUserEmail(), token, request.getNewPassword()));
    }

    @GetMapping("/revoke")
    public ResponseEntity<AuthenticationResponse> logout(HttpServletRequest request,
                                                         @RequestParam("fromAllDevices") boolean fromAllDevices) {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        service.revoke(authHeader, fromAllDevices);
        return ResponseEntity.noContent().build();
    }

}