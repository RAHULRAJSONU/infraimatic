package com.ezyinfra.product.checkpost.identity.crypto;

import java.util.regex.Pattern;

public final class PasswordStrengthValidator {

    private static final int MIN_LENGTH = 8;
    private static final int MAX_LENGTH = 30;

    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])" +           // at least 1 digit
            "(?=.*[a-z])" +            // at least 1 lowercase
            "(?=.*[A-Z])" +            // at least 1 uppercase
            "(?=.*[?!@#$%^&*/\\\\])" + // at least 1 special char
            "[A-Za-z0-9?!@#$%^&*/\\\\]{8,30}$"
    );

    private PasswordStrengthValidator() {}

    public static PasswordStrengthResult validate(String password) {

        if (password == null || password.isBlank()) {
            return new PasswordStrengthResult(false, "Password cannot be empty");
        }

        if (password.length() < MIN_LENGTH || password.length() > MAX_LENGTH) {
            return new PasswordStrengthResult(
                    false,
                    "Password length must be between 8 and 30 characters"
            );
        }

        if (password.chars().anyMatch(Character::isWhitespace)) {
            return new PasswordStrengthResult(false, "Password must not contain spaces");
        }

        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return new PasswordStrengthResult(
                    false,
                    """
                    Password must contain:
                    - at least 1 uppercase letter
                    - at least 1 lowercase letter
                    - at least 1 digit
                    - at least 1 special character (? ! @ # $ % ^ & * / \\)
                    """
            );
        }

        if (hasRepeatedSequence(password)) {
            return new PasswordStrengthResult(false, "Password contains repeated characters");
        }

        return new PasswordStrengthResult(true, "Password is strong");
    }

    private static boolean hasRepeatedSequence(String password) {
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1)
                && password.charAt(i) == password.charAt(i + 2)) {
                return true;
            }
        }
        return false;
    }
}
