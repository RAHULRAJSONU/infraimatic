package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.model.AuthenticationRequest;
import com.ezyinfra.product.checkpost.identity.data.model.AuthenticationResponse;
import com.ezyinfra.product.checkpost.identity.data.record.UserCreateRecord;

import java.io.IOException;

public interface AuthenticationService {

    AuthenticationResponse userRegistration(UserCreateRecord request, String tenantId);

    AuthenticationResponse authenticate(AuthenticationRequest request);

    AuthenticationResponse refreshToken(String authHeader) throws IOException;

    void revoke(String authorization, boolean fromAllDevices);
}