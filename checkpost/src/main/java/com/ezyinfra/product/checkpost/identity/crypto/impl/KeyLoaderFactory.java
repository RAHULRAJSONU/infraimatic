package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;

import java.util.function.Function;
import java.util.function.Supplier;

public class KeyLoaderFactory {

    public static KeyLoader createFileKeyLoader(String privateKeyPath, String publicKeyPath) {
        return new FileKeyLoader(privateKeyPath, publicKeyPath);
    }

    public static KeyLoader createEnvKeyLoader(String privateKeyEnvVar, String publicKeyEnvVar) {
        return new EnvKeyLoader(privateKeyEnvVar, publicKeyEnvVar);
    }

    public static KeyLoader createSupplierKeyLoader(Supplier<String> privateKeySupplier, Supplier<String> publicKeySupplier) {
        return new SupplierKeyLoader(privateKeySupplier, publicKeySupplier);
    }
}
