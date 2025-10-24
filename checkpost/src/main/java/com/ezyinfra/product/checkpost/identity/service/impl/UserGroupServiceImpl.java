package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import com.ezyinfra.product.checkpost.identity.data.entity.UserGroup;
import com.ezyinfra.product.checkpost.identity.data.repository.UserGroupRepository;
import com.ezyinfra.product.checkpost.identity.service.UserGroupService;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserGroupServiceImpl implements UserGroupService {
    private final UserGroupRepository userGroupRepository;

    @Override
    public UserGroup findByNameAndActive(String name, @Nullable Boolean active) {
        if (active == null) {
            return userGroupRepository.findByName(name)
                    .orElseThrow(() -> new ResourceNotFoundException("User group %s does not exist".formatted(name)));
        } else {
            return userGroupRepository.findByNameAndActive(name, active)
                    .orElseThrow(() -> new ResourceNotFoundException("User group %s does not exist with active: %s".formatted(name, active)));
        }
    }

    @Override
    public Page<UserGroup> findAllByActive(Pageable pageable, Boolean active) {
        if (active == null) {
            return userGroupRepository.findAll(pageable);
        } else {
            return userGroupRepository.findAllByActive(pageable, active);
        }
    }

    @Override
    public List<String> findUserAssociatedGroup(UUID userId) {
        return userGroupRepository.findUserGroupNamesByUserId(userId);
    }

    @Override
    public List<String> findAssociatedUserEmail(String name) {
        return userGroupRepository.findAssociatedUserEmailByGroupName(name);
    }

    @Override
    public boolean existsByName(String userGroupName) {
        return userGroupRepository.existsByName(userGroupName);
    }
}
