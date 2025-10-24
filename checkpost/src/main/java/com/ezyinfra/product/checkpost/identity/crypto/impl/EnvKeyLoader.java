package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;
import com.ezyinfra.product.checkpost.identity.crypto.PemSupport;

import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class EnvKeyLoader implements KeyLoader {

    private final String privEnv;
    private final String pubEnv;

    public EnvKeyLoader(String privEnv, String pubEnv) { this.privEnv = privEnv; this.pubEnv = pubEnv; }

    @Override
    public PrivateKey loadPrivateKey() throws GeneralSecurityException {
        String pem = System.getenv(privEnv);
        if (pem == null || pem.isBlank()) throw new IllegalArgumentException("env " + privEnv + " missing");
        return PemSupport.parseRsaPrivate(pem);
    }

    @Override
    public PublicKey loadPublicKey() throws GeneralSecurityException {
        String pem = System.getenv(pubEnv);
        if (pem == null || pem.isBlank()) throw new IllegalArgumentException("env " + pubEnv + " missing");
        return PemSupport.parseRsaPublic(pem);
    }
}