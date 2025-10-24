package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;

public final class Pkcs12KeyLoader implements KeyLoader {
    private final Path p12Path;
    private final char[] password;
    private final String keyAlias;

    Pkcs12KeyLoader(Path p12Path, char[] password, String keyAlias) {
        this.p12Path = p12Path; this.password = password.clone(); this.keyAlias = keyAlias;
    }
    @Override public PrivateKey loadPrivateKey() throws GeneralSecurityException, IOException {
        try (InputStream in = Files.newInputStream(p12Path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(in, password);
            Key key = ks.getKey(keyAlias, password);
            if (!(key instanceof PrivateKey pk)) throw new KeyStoreException("no private key for alias");
            return pk;
        }
    }
    @Override public PublicKey loadPublicKey() throws GeneralSecurityException, IOException {
        try (InputStream in = Files.newInputStream(p12Path)) {
            KeyStore ks = KeyStore.getInstance("PKCS12"); ks.load(in, password);
            var cert = ks.getCertificate(keyAlias);
            if (cert == null) throw new KeyStoreException("no cert for alias");
            return cert.getPublicKey();
        }
    }
}