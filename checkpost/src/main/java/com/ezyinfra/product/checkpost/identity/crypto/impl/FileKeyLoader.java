package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;
import com.ezyinfra.product.checkpost.identity.crypto.PemSupport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class FileKeyLoader implements KeyLoader {
    private final Path privatePath, publicPath;

    public FileKeyLoader(String privateKeyPath, String publicKeyPath) {
        this.privatePath = Paths.get(privateKeyPath);
        this.publicPath  = Paths.get(publicKeyPath);
    }
    @Override public PrivateKey loadPrivateKey() throws GeneralSecurityException, IOException {
        String pem = Files.readString(privatePath, StandardCharsets.UTF_8);
        return PemSupport.parseRsaPrivate(pem);
    }
    @Override public PublicKey loadPublicKey() throws GeneralSecurityException, IOException {
        String pem = Files.readString(publicPath, StandardCharsets.UTF_8);
        return PemSupport.parseRsaPublic(pem);
    }
}
