package com.ezyinfra.product.checkpost.identity.crypto;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

final class KeyPairValidator {
    static void validateRsa(PrivateKey priv, PublicKey pub) {
        if (!(priv instanceof RSAPrivateKey p) || !(pub instanceof RSAPublicKey q))
            throw new IllegalArgumentException("Keys must be RSA");
        int bits = q.getModulus().bitLength();
        if (bits < 2048) throw new IllegalArgumentException("RSA key size must be >= 2048, was " + bits);
        // same modulus check
        if (!p.getModulus().equals(q.getModulus()))
            throw new IllegalArgumentException("Public and private keys do not match");
    }
    private KeyPairValidator() {}
}