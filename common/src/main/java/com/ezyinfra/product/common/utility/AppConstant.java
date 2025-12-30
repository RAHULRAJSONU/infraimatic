package com.ezyinfra.product.common.utility;

import java.util.List;
import java.util.regex.Pattern;

public class AppConstant {
    public static final String DATE_FORMAT_SLASH_DDMMYYYY = "dd/MM/yyyy";
    public static final String DATE_FORMAT_DASH_DDMMYYYY = "dd-MM-yyyy";
    public static final String DATE_FORMAT_DASH_YYYYMMDD = "yyyy-MM-dd";
    public static final String VALID_STRING_REGEX = "^[a-zA-Z-0-9 ._-]{5,50}$";
    public static final String REQUIRED_STRING_FORMAT = "Only alpha-numeric and [. _ whitespace] special characters are allowed. Length should be 5-50 characters.";
    public static final String VALID_LONG_TEXT_REGEX = "^[a-zA-Z-0-9 ._@#,-]{5,225}$";
    public static final String REQUIRED_LONG_TEXT_FORMAT = "Only alpha-numeric and [. , _ @ # -] special characters are allowed. Length should be 5-225 characters.";
    public static final String VALID_USERNAME_REGEX = "^[A-Za-z0-9._]{5,16}$";
    public static final String REQUIRED_USERNAME_FORMAT = "Only alpha-numeric and [. _] special characters are allowed. Length should be 5-50 characters.";
    public static final String VALID_PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[?!@#$%^&*\\/\\\\])(?=.*[A-Z])(?=.*[a-z])[a-zA-Z0-9?!@#$%^&*\\/\\\\].{8,30}$";
    public static final String REQUIRED_PASSWORD_FORMAT = "Invalid password, password should contain at least one lowercase, one uppercase, one digit and one special character. Length should be 8-30 characters.";
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String UUID_SQL_PREFIX = "UUID:";
    public static final String TENANT_HEADER = "X-Tenant-Id";

    public static final List<String> authExcludedPath = List.of(
            "/api/v1/tenant/register",
            "/api/v1/identity/authn/authenticate",
            "/api/v1/identity/authn/reset-password",
            "/api/v1/identity/authn/reset-password/**",
            "/api/v1/identity/authn/register",
            "/api/v1/oauth/**",
            "/v2/api-docs",
            "/v3/api-docs",
            "/v3/api-docs/**",
            "/swagger-resources",
            "/swagger-resources/**",
            "/configuration/ui",
            "/configuration/security",
            "/swagger-ui/**",
            "/webjars/**",
            "/swagger-ui.html",
            "/webhooks/whatsapp");
    static List<Pattern> authExcludedPathPatterns = authExcludedPath.stream()
            .map(AppConstant::convertToRegex)
            .map(Pattern::compile)
            .toList();

    public static boolean isPathExcluded(String path) {
        // Check if the path matches any of the patterns
        for (Pattern pattern : authExcludedPathPatterns) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }

        return false;
    }

    private static String convertToRegex(String pathPattern) {
        // Convert wildcard patterns to regex
        String regex = pathPattern.replace("**", ".*").replace("*", "[^/]*");
        return "^" + regex + "$";
    }

    public static class Security {
        public static final String ANONYMOUS_USER = "anonymous";
    }

    public static class Jwt {
        public static final String TENANT_ID = "tenant";
        public static final String TOKEN_SCOPE = "scope";
        public static final String TOKEN_TYPE = "token-type";
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String ACCESS_TOKEN = "access_token";
        public static final String ROLE = "roles";
        public static final String AUTHORITIES = "authorities";
        public static final String USER_GROUP = "user_groups";
        public static final String EMAIL = "email";
        public static final String PHONE = "phone";
        public static final String PRINCIPAL = "principal";
        public static final String AUDIENCE = "aud";
    }
}