package com.ezyinfra.product.checkpost.identity.crypto;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;

public interface KeyLoader {
    PrivateKey loadPrivateKey() throws GeneralSecurityException, IOException;
    PublicKey  loadPublicKey()  throws GeneralSecurityException, IOException;
}
