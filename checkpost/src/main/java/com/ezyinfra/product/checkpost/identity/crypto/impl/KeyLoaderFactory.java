package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;
import com.ezyinfra.product.common.exception.CryptoException;

import java.util.function.Supplier;

public class KeyLoaderFactory {

    /**
     * Factory function to create encryption key loader
     * @param providerType : FILE_KEY_PROVIDER | ENV_KEY_PROVIDER
     * @param privateKey
     * @param publicKey
     * @return KeyLoader
     * @throws CryptoException : When invalid providerType or invalid key
     */
    public static KeyLoader createKeyProvider(String providerType, String privateKey, String publicKey){
        return switch (providerType){
            case "FILE_KEY_PROVIDER" -> new FileKeyLoader(privateKey, publicKey);
            case "ENV_KEY_PROVIDER" -> new EnvKeyLoader(privateKey, publicKey);
            default -> throw new CryptoException("Invalid Encryption Key Provider.");
        };
    }

    public static KeyLoader createSupplierKeyLoader(Supplier<String> privateKeySupplier, Supplier<String> publicKeySupplier) {
        return new SupplierKeyLoader(privateKeySupplier, publicKeySupplier);
    }
}
