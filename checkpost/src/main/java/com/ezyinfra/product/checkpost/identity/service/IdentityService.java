package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.User;

import java.util.Optional;

public interface IdentityService {
    static Optional<User> getLoggedInUser() {
        return Optional.of(SignedUserHelper.user());
    }
}
