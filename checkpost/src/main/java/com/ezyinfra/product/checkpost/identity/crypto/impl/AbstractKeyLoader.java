package com.ezyinfra.product.checkpost.identity.crypto.impl;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;

import java.security.KeyFactory;
import java.util.Base64;

public abstract class AbstractKeyLoader implements KeyLoader {

    protected static final String RSA_ALGORITHM = "RSA";

    protected byte[] decodeBase64Key(String key) {
        return Base64.getDecoder().decode(key);
    }

    protected String stripKeyHeaders(String key, String beginMarker, String endMarker) {
        return key.replaceAll(beginMarker, "")
                  .replaceAll(endMarker, "")
                  .replaceAll("\\s+", "");
    }

    protected KeyFactory getKeyFactory() throws Exception {
        return KeyFactory.getInstance(RSA_ALGORITHM);
    }
}
