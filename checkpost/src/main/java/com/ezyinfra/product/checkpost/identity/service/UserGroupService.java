package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.UserGroup;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserGroupService {

    UserGroup findByNameAndActive(String name, @Nullable Boolean active);

    Page<UserGroup> findAllByActive(Pageable pageable, Boolean active);

    List<String> findUserAssociatedGroup(UUID userId);

    List<String> findAssociatedUserEmail(String name);

    boolean existsByName(String userGroupName);
}
