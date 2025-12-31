package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.record.UserUpdateRequest;
import com.ezyinfra.product.checkpost.identity.data.repository.UserRepository;
import com.ezyinfra.product.checkpost.identity.service.UserService;
import com.ezyinfra.product.checkpost.identity.tenant.config.TenantContext;
import com.ezyinfra.product.common.enums.UserStatus;
import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.ezyinfra.product.common.utility.UtilityService.nullSafeOperation;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public Page<User> findAll(Pageable p, @Nullable UserStatus status) {
        if (status == null) {
            return userRepository.findAll(p);
        } else {
            return userRepository.findAllByStatus(p, status);
        }
    }

    @Override
    public User findByEmailAndStatus(String email, @Nullable UserStatus status) {
        Objects.requireNonNull(email, "Not a valid email: " + email);
        if (status == null) {
            return userRepository.findByEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for email: " + email));
        } else {
            return userRepository.findByEmailAndStatus(email, status)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for email: %s and status: %s".formatted(email, status)));
        }
    }

    @Override
    public List<User> findByEmailInAndStatus(List<String> emails, @Nullable UserStatus status) {
        Objects.requireNonNull(emails, "Not a valid email: " + emails);
        if (emails.isEmpty()) {
            return List.of();
        }
        if (status == null) {
            return userRepository.findByEmailIn(emails);
        } else {
            return userRepository.findByEmailInAndStatus(emails, status);
        }
    }

    @Override
    @Transactional
    public User updateUser(UserUpdateRequest request) {
        Objects.requireNonNull(request, "Invalid user update request");
        log.info("User update request received: {}", request);
        User user = userRepository.findById(request.id())
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: %s".formatted(request.id())));
        nullSafeOperation(user, request.givenName(), User::setGivenName);
        nullSafeOperation(user, request.middleName(), User::setMiddleName);
        nullSafeOperation(user, request.familyName(), User::setFamilyName);
        nullSafeOperation(user, request.nickname(), User::setNickname);
        nullSafeOperation(user, request.preferredUsername(), User::setPreferredUsername);
        nullSafeOperation(user, request.address(), User::setAddress);
        nullSafeOperation(user, request.zoneInfo(), User::setZoneInfo);
        nullSafeOperation(user, request.website(), User::setWebsite);
        nullSafeOperation(user, request.picture(), User::setPicture);
        nullSafeOperation(user, request.locale(), User::setLocale);
        log.info("Updating user entity: {}", user);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User changeUserStatus(UUID id, UserStatus status) {
        Objects.requireNonNull(id, "Invalid user id: " + id);
        log.info("User status update request received for id: {}, status: {}", id, status);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found for id: %s".formatted(id)));
        user.setStatus(status);
        log.info("Persisting updated user entity: {}", user);
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByPhoneNumberAndStatus(String phoneNumber, @Nullable UserStatus status) {
        Objects.requireNonNull(phoneNumber, "Invalid mobile number: " + phoneNumber);
        return userRepository.findByPhoneNumberAndStatus(phoneNumber, status);
    }

    @Override
    public Optional<String> findTenantIdByMobile(String phoneNumber) {
        log.info("Finding tenant for tenantId: {}, and phoneNumber: {}", TenantContext.get(),phoneNumber);
        Objects.requireNonNull(phoneNumber, "Invalid mobile number: " + phoneNumber);
        Optional<User> user = findByPhoneNumberAndStatus(phoneNumber, UserStatus.ACTIVE);
        if(user.isPresent()){
            User currentUser = user.get();
            return Optional.of(currentUser.getTenantId());
        }
        return Optional.empty();
    }

}
