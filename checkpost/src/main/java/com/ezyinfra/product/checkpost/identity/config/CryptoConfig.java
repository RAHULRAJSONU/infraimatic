package com.ezyinfra.product.checkpost.identity.config;

import com.ezyinfra.product.checkpost.identity.crypto.KeyLoader;
import com.ezyinfra.product.checkpost.identity.crypto.RsaHybridCrypto;
import com.ezyinfra.product.checkpost.identity.crypto.impl.EnvKeyLoader;
import com.ezyinfra.product.checkpost.identity.crypto.impl.FileKeyLoader;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.io.IOException;
import java.security.GeneralSecurityException;

@Configuration
@EnableConfigurationProperties(IdentityProperties.class)
class CryptoConfig {

    @Bean
    RsaHybridCrypto rsaHybridCrypto(IdentityProperties props) throws GeneralSecurityException, IOException {
        KeyLoader loader;
        if (props.getPrivateKeyPath() != null) {
            loader = new FileKeyLoader(props.getPrivateKeyPath(),
                                       props.getPublicKeyPath());
        } else {
            loader = new EnvKeyLoader("APP_RSA_PRIVATE_PEM", "APP_RSA_PUBLIC_PEM");
        }
        return RsaHybridCrypto.from(loader);
    }
}