package com.ezyinfra.product.checkpost.identity.crypto;

import javax.crypto.Cipher;
import java.security.PrivateKey;
import java.security.PublicKey;

public final class RsaEncryptionUtils {

    private static final String RSA_ALGORITHM = "RSA";
    private final PrivateKey privateKey;
    private final PublicKey publicKey;

    // Private constructor to ensure immutability
    private RsaEncryptionUtils(PrivateKey privateKey, PublicKey publicKey) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
    }

    // Factory method to create an instance using KeyLoader
    public static RsaEncryptionUtils fromKeyLoader(KeyLoader keyLoader) throws Exception {
        PrivateKey loadedPrivateKey = keyLoader.loadPrivateKey();
        PublicKey loadedPublicKey = keyLoader.loadPublicKey();
        return new RsaEncryptionUtils(loadedPrivateKey, loadedPublicKey);
    }

    // Method to encrypt data using the public key
    public byte[] encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
        return cipher.doFinal(data.getBytes());
    }

    // Method to decrypt data using the private key
    public String decrypt(byte[] encryptedData) throws Exception {
        Cipher cipher = Cipher.getInstance(RSA_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        byte[] decryptedBytes = cipher.doFinal(encryptedData);
        return new String(decryptedBytes);
    }
}
