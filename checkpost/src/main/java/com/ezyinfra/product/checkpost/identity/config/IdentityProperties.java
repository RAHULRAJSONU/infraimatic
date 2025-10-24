package com.ezyinfra.product.checkpost.identity.config;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "identity.security")
public class IdentityProperties {

    private String privateKeyPath;
    private String publicKeyPath;
    private String keyBeginMarker;
    private String keyEndMarker;
    private Password password;
    private ResetPassword resetPassword;
    private String secureRandomChar;
    private Jwt jwt;

    @Setter
    @Getter
    public static class Password {
        private String validationRegex;
        private String validationMessage;
        private boolean isPasswordUsageAllowed;
        private int oldPasswordSpan;

    }

    @Setter
    @Getter
    public static class ResetPassword {
        private int tokenValidityInSecond;
        private String redirectUrl;

    }

    @Setter
    @Getter
    public static class Jwt {
        private long expiration;
        private RefreshToken refreshToken;
        private String secretKey;

        @Setter
        @Getter
        public static class RefreshToken {
            private long expiration;

        }
    }

}