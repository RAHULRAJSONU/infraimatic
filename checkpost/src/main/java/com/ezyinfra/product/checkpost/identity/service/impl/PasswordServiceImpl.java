package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.common.enums.UserStatus;
import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import com.ezyinfra.product.checkpost.identity.config.IdentityProperties;
import com.ezyinfra.product.checkpost.identity.connector.UserEmailNotificationHelper;
import com.ezyinfra.product.checkpost.identity.crypto.RsaEncryptionUtils;
import com.ezyinfra.product.checkpost.identity.crypto.impl.KeyLoaderFactory;
import com.ezyinfra.product.checkpost.identity.data.entity.PasswordResetToken;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.model.ChangePasswordRequest;
import com.ezyinfra.product.checkpost.identity.data.repository.PasswordResetTokenRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.UserRepository;
import com.ezyinfra.product.checkpost.identity.service.PasswordService;
import com.ezyinfra.product.notification.email.model.EmailTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class PasswordServiceImpl implements PasswordService {

    private static final int TOKEN_LENGTH = 20;
    private final UserRepository repository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserEmailNotificationHelper emailNotificationHelper;
    private final IdentityProperties identityProperties;
    @Value("${identity.ui-host}")
    private String uiHost;
    private final RsaEncryptionUtils encryptionUtils;

    public PasswordServiceImpl(UserRepository repository,
                               PasswordResetTokenRepository passwordResetTokenRepository,
                               PasswordEncoder passwordEncoder,
                               UserEmailNotificationHelper emailNotificationHelper,
                               IdentityProperties identityProperties) {
        this.repository = repository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailNotificationHelper = emailNotificationHelper;
        this.identityProperties = identityProperties;
        try {
            log.info("privateKeyPath: {}",identityProperties.getPrivateKeyPath());
            log.info("publicKeyPath: {}",identityProperties.getPublicKeyPath());
            var keyLoader = KeyLoaderFactory.createFileKeyLoader(
                    identityProperties.getPrivateKeyPath(),
                    identityProperties.getPublicKeyPath()
            );
            encryptionUtils = RsaEncryptionUtils.fromKeyLoader(keyLoader);
        } catch (Exception e) {
            throw new AuthException("Could not load the encryption key: "+e.getMessage());
        }
    }

    @Override
    public String generateResetToken() {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int randomCharIndex = secureRandom.nextInt(identityProperties.getSecureRandomChar().length());
            char randomChar = identityProperties.getSecureRandomChar().charAt(randomCharIndex);
            token.append(randomChar);
        }
        return token.toString();
    }

    @Override
    public boolean validatePasswordStrength(String password) {
        Pattern pattern = Pattern.compile(identityProperties.getPassword().getValidationRegex());
        Matcher matcher = pattern.matcher(password);
        if (matcher.matches()) {
            return Boolean.TRUE;
        } else {
            throw new AuthException(identityProperties.getPassword().getValidationMessage());
        }
    }

    @Override
    public ResponseEntity<?> changePassword(ChangePasswordRequest request, String email) {
        if (request.getNewPassword().equals(request.getCurrentPassword())) {
            throw new AuthException("New password and current password should not be same.");
        }
        validatePasswordStrength(request.getCurrentPassword());
        var user = repository.findByEmailIgnoreCaseAndStatus(email, UserStatus.ACTIVE).orElseThrow();
        if (passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getNewPassword()));
            repository.save(user);
            emailNotificationHelper.notifyUser(
                    user,
                    EmailTemplate.IDENTITY_PASSWORD_CHANGE_SUCCESS,
                    Map.of("name", user.getName())
            );
            return ResponseEntity.ok().body("Password changed successfully.");
        } else {
            throw new AuthException("Current password is not match with our record.");
        }
    }

    @Override
    @Transactional
    public ResponseEntity<?> sendResetPasswordLink(String userEmail) {
        var user = repository.findByEmailIgnoreCaseAndStatus(userEmail, UserStatus.ACTIVE).orElseThrow(() -> new ResourceNotFoundException("Email not registered with us."));
        String resetToken = generateResetToken();
        Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByUser(user);
        PasswordResetToken newPasswordRestToken;
        if (passwordResetToken.isEmpty()) {
            newPasswordRestToken = new PasswordResetToken();
            newPasswordRestToken.setToken(resetToken);
            newPasswordRestToken.setUser(user);
            newPasswordRestToken.setExpiryDate(Date.from(Instant.now().plus(identityProperties.getResetPassword().getTokenValidityInSecond(),
                    ChronoUnit.SECONDS)));
        } else {
            newPasswordRestToken = passwordResetToken.get();
            newPasswordRestToken.setToken(resetToken);
            newPasswordRestToken.setExpiryDate(Date.from(Instant.now().plus(identityProperties.getResetPassword().getTokenValidityInSecond(),
                    ChronoUnit.SECONDS)));
        }
        passwordResetTokenRepository.save(newPasswordRestToken);
        String emailLink = uiHost + identityProperties.getResetPassword().getRedirectUrl() + "?token=" + resetToken;
        log.info("Password reset link: {}", emailLink);
        try {
            emailNotificationHelper.notifyUser(
                    user,
                    EmailTemplate.IDENTITY_PASSWORD_RESET_REQUEST,
                    Map.of("name", user.getName(),
                            "passwordResetLink", emailLink)
            );
        } catch (Exception e) {
            log.info("Could not send the password reset link, error: {}", e.getMessage());
        }
        return ResponseEntity.ok().body("Reset password link sent to your email.");
    }

    @Override
    public ResponseEntity<?> resetPassword(String token, String password) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new AuthException("Unauthorized Request, reset token does not exist."));
        if (resetToken.getExpiryDate().before(new Date())) {
            throw new AuthException("Password reset token has expired.");
        }
        // Reset the user's password, update the user's password, and invalidate the token
        User user = resetToken.getUser();
        validatePasswordStrength(password);
        if(!identityProperties.getPassword().isPasswordUsageAllowed()) {
            checkPasswordHistory(user, password);
            if (user.getPasswordHistory().size() >= identityProperties.getPassword().getOldPasswordSpan()) {
                user.getPasswordHistory().removeFirst();
            }
            try {
                String encryptedPassword = new String(encryptionUtils.encrypt(user.getPassword()));
                user.getPasswordHistory().add(encryptedPassword);
            } catch (Exception e) {
                throw new RuntimeException("Failed to update password", e);
            }
        }
        user.setPassword(passwordEncoder.encode(password));
        repository.save(user);
        passwordResetTokenRepository.delete(resetToken);
        emailNotificationHelper.notifyUser(
                user,
                EmailTemplate.IDENTITY_PASSWORD_RESET_SUCCESS,
                Map.of("name", user.getName())
        );
        return ResponseEntity.ok("Password reset successfully.");
    }

    private boolean checkPasswordHistory(User user, String newPassword) {
        try {
            for (String encryptedPassword : user.getPasswordHistory()) {
                String decryptedPassword = encryptionUtils.decrypt(encryptedPassword.getBytes());
                if (passwordEncoder.matches(newPassword, decryptedPassword)) {
                    throw new AuthException("Invalid Password, can not re-use last 7 password");
                }
            }
        } catch (Exception e) {
            throw new AuthException("Failed to check password history", e);
        }
        return true;
    }

}
