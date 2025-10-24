package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.PemSupport;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.function.Supplier;

public class SupplierKeyLoader extends AbstractKeyLoader {

    private final Supplier<String> privateKeySupplier;
    private final Supplier<String> publicKeySupplier;

    public SupplierKeyLoader(Supplier<String> privateKeySupplier, Supplier<String> publicKeySupplier) {
        this.privateKeySupplier = privateKeySupplier;
        this.publicKeySupplier = publicKeySupplier;
    }

    @Override
    public PrivateKey loadPrivateKey() throws GeneralSecurityException {
        String key = privateKeySupplier.get();

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Supplied private key is null or empty");
        }

        return PemSupport.parseRsaPrivate(key);
    }

    @Override
    public PublicKey loadPublicKey() throws GeneralSecurityException {
        String key = publicKeySupplier.get();

        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Supplied public key is null or empty");
        }

        return PemSupport.parseRsaPublic(key);
    }
}
