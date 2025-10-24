package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.common.enums.UserStatus;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.record.UserUpdateRequest;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {


    Page<User> findAll(Pageable p, @Nullable UserStatus status);

    User findByEmailAndStatus(String email, @Nullable UserStatus status);

    List<User> findByEmailInAndStatus(List<String> emails, @Nullable UserStatus status);

    User updateUser(UserUpdateRequest request);

    User changeUserStatus(UUID id, UserStatus status);

}
