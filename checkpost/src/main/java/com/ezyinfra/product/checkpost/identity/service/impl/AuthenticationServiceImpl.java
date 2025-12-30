package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.connector.UserEmailNotificationHelper;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.entity.Token;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.model.AuthenticationRequest;
import com.ezyinfra.product.checkpost.identity.data.model.AuthenticationResponse;
import com.ezyinfra.product.checkpost.identity.data.record.UserCreateRecord;
import com.ezyinfra.product.checkpost.identity.data.repository.TokenRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.UserRepository;
import com.ezyinfra.product.checkpost.identity.service.*;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
import com.ezyinfra.product.common.enums.TokenType;
import com.ezyinfra.product.common.enums.UserStatus;
import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.common.utility.AppConstant;
import com.ezyinfra.product.notification.email.model.EmailTemplate;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RoleService roleService;
    private final UserGroupService userGroupService;
    private final ModelMapper mapper;
    private final PasswordService passwordService;
    private final UserEmailNotificationHelper emailNotificationHelper;
    private final TenantService tenantService;

    public AuthenticationServiceImpl(UserRepository userRepository, TokenRepository tokenRepository, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, RoleService roleService, UserGroupService userGroupService, ModelMapper mapper, PasswordService passwordService, UserEmailNotificationHelper emailNotificationHelper, TenantService tenantService) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.roleService = roleService;
        this.userGroupService = userGroupService;
        this.mapper = mapper;
        this.passwordService = passwordService;
        this.emailNotificationHelper = emailNotificationHelper;
        this.tenantService = tenantService;
    }


    @Override
    @Transactional
    public AuthenticationResponse userRegistration(UserCreateRecord request, String tenantId) {
        log.info("Registering new user: {}, for tenant: {}",request.email(),tenantId);
        return TenantContext.executeInTenantContext(tenantId, () -> {
            var userByEmail = userRepository.findByEmailIgnoreCaseAndStatus(request.email(), UserStatus.ACTIVE);
            var userByMobile = userRepository.findByPhoneNumberAndStatus(request.phoneNumber(), UserStatus.ACTIVE);

            if (userByEmail.isPresent()) {
                throw new AuthException("This email is already exits.");
            } else if (userByMobile.isPresent()) {
                throw new AuthException("This mobile is already exits.");
            } else {
                return registerUser(request);
            }
        });
    }

    private AuthenticationResponse registerUser(UserCreateRecord request) {
        passwordService.validatePasswordStrength(request.password());
        User newUser = mapper.map(request, User.class);
        newUser.setStatus(UserStatus.ACTIVE);
        newUser.setEnabled(true);
        newUser.setPassword(passwordEncoder.encode(request.password()));
        var savedUser = userRepository.save(newUser);
        var jwtToken = jwtService.generateToken(computeExtraClaims(savedUser), savedUser);
        var refreshToken = jwtService.generateRefreshToken(Map.of(AppConstant.Jwt.TOKEN_TYPE, AppConstant.Jwt.REFRESH_TOKEN), savedUser);
        saveUserToken(savedUser, jwtToken, refreshToken);
        emailNotificationHelper.notifyUser(savedUser,
                EmailTemplate.IDENTITY_ACCOUNT_CREATED,
                Map.of("name", savedUser.getName(),
                        "email", savedUser.getEmail(),
                        "registrationDate", savedUser.getCreatedAt()));
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    @Override
    public AuthenticationResponse authenticate(AuthenticationRequest request) {

        String tenantId = tenantService.resolveTenantByEmail(request.getEmail())
                .orElseThrow(() -> new AuthException("Invalid credentials"));

        return TenantContext.executeInTenantContext(tenantId, () -> {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
            var user = userRepository.findByEmailIgnoreCaseAndStatus(request.getEmail(), UserStatus.ACTIVE).orElseThrow();
            if (!user.isEnabled())
                throw new AuthException("Your account is currently inactive. Please get in touch with us at identity@intzdata.com for further assistance.");
            List<Token> allValidTokenByUser = tokenRepository.findValidTokenByUser(user);
            var jwtToken = "";
            if (allValidTokenByUser.isEmpty()) {
                Map<String, Object> userAuth = computeExtraClaims(user);
                jwtToken = jwtService.generateToken(userAuth, user);
                var refreshToken = jwtService.generateRefreshToken(Map.of(AppConstant.Jwt.TOKEN_TYPE, AppConstant.Jwt.REFRESH_TOKEN), user);
                saveUserToken(user, jwtToken, refreshToken);
                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build();
            } else {
                jwtToken = allValidTokenByUser.get(0).getToken();
                String refreshToken = allValidTokenByUser.get(0).getRefreshToken();
                boolean tokenValid = false;
                try {
                    tokenValid = jwtService.isTokenValid(jwtToken, user);
                } catch (ExpiredJwtException e) {
                    log.error("Jwt token expired: {}", e.getMessage());
                }
                if (!tokenValid) {
                    tokenRepository.revoke(jwtToken);
                    Map<String, Object> userAuth = computeExtraClaims(user);
                    jwtToken = jwtService.generateToken(userAuth, user);
                    refreshToken = jwtService.generateRefreshToken(Map.of(AppConstant.Jwt.TOKEN_TYPE, AppConstant.Jwt.REFRESH_TOKEN), user);
                    saveUserToken(user, jwtToken, refreshToken);
                }
                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build();
            }
        });
    }

    private Map<String, Object> computeExtraClaims(User user) {
        return Map.of(
                AppConstant.Jwt.TENANT_ID, TenantContext.getCurrentTenantId(),
                AppConstant.Jwt.PRINCIPAL, user.getEmail(),
                AppConstant.Jwt.AUDIENCE, "",
                "name", user.getName(),
                AppConstant.Jwt.PHONE, user.getPhoneNumber(),
                AppConstant.Jwt.AUTHORITIES, user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                AppConstant.Jwt.ROLE, user.getRoles().stream().map(Role::getName).toList(),
                AppConstant.Jwt.USER_GROUP, userGroupService.findUserAssociatedGroup(user.getId()),
                AppConstant.Jwt.TOKEN_TYPE, AppConstant.Jwt.ACCESS_TOKEN
        );
    }

    private void saveUserToken(User user, String jwtToken, String refreshToken) {
        log.info("Saving token: {}, refresh: {}", jwtToken, refreshToken);
        var token = Token.builder()
                .user(user)
                .token(jwtToken)
                .refreshToken(refreshToken)
                .tokenType(TokenType.BEARER)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    private void updateUserToken(String jwtToken, String refreshToken) {
        Token token = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new AuthException("Refresh token not found."));
        token.setToken(jwtToken);
        tokenRepository.save(token);
    }

    private void revokeAllUserTokens(User user) {
        List<Token> validUserTokens = tokenRepository.findAllValidTokenByUser(user.getId());
        if (validUserTokens.isEmpty())
            return;
        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    @Override
    public AuthenticationResponse refreshToken(String authHeader) throws IOException {
        final String refreshToken;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new AuthException("Unauthorized access.");
        }
        refreshToken = authHeader.substring(7);

        String tokenType = jwtService.extractClaim(refreshToken, c -> (String) c.get(AppConstant.Jwt.TOKEN_TYPE));
        if (tokenType == null || !tokenType.equals(AppConstant.Jwt.REFRESH_TOKEN)) {
            throw new AuthException("Invalid refresh token");
        }

        String tenantId = jwtService.extractClaim(
                refreshToken,
                c -> (String) c.get("tenant")
        );

        if (tenantId == null) {
            throw new AuthException("Tenant missing in refresh token");
        }
        return TenantContext.executeInTenantContext(tenantId, () -> {
            final String userEmail;
            AuthenticationResponse authResponse = null;
            userEmail = jwtService.extractUsername(refreshToken);

            if (userEmail != null) {
                var user = this.userRepository.findByEmailIgnoreCaseAndStatus(userEmail, UserStatus.ACTIVE)
                        .orElseThrow(() -> new AuthException("Invalid user details."));
                if (jwtService.isTokenValid(refreshToken, user)) {
                    var accessToken = jwtService.generateToken(computeExtraClaims(user), user);
                    updateUserToken(accessToken, refreshToken);
                    authResponse = AuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();
                } else {
                    throw new AuthException("Invalid refresh token.");
                }
            } else {
                throw new AuthException("Invalid auth token.");
            }
            return authResponse;
        });
    }

    @Override
    public void revoke(String authorization, boolean fromAllDevices) {
        log.info("logging out, fromAllDevices: {}", fromAllDevices);
        final String token;
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new AuthException("Unauthorized request, access denied.");
        }
        token = authorization.substring(7);

        String tenantId = jwtService.extractClaim(
                token,
                c -> (String) c.get("tenant")
        );

        if (tenantId == null) {
            throw new AuthException("Tenant missing in refresh token");
        }
        TenantContext.executeInTenantContext(tenantId, () -> {
            final String userEmail = jwtService.extractUsername(token);
            var user = userRepository.findByEmailIgnoreCaseAndStatus(userEmail, UserStatus.ACTIVE).orElseThrow(
                    () -> new AuthException("Unauthorized request, access denied."));
            if (fromAllDevices) {
                tokenRepository.revokeAll(user);
            } else {
                tokenRepository.revoke(token);
            }
        });
    }
}