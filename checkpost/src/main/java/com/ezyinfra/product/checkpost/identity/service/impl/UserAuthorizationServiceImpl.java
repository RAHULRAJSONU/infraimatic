package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.common.exception.AuthException;
import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.repository.RoleRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.UserRepository;
import com.ezyinfra.product.checkpost.identity.service.PrivilegeService;
import com.ezyinfra.product.checkpost.identity.service.UserAuthorizationService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
public class UserAuthorizationServiceImpl implements UserAuthorizationService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PrivilegeService privilegeService;
    private final ModelMapper mapper = new ModelMapper();

    public UserAuthorizationServiceImpl(RoleRepository roleRepository, UserRepository userRepository, PrivilegeService privilegeService) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.privilegeService = privilegeService;
    }

    @Override
    public User mapRolesToUser(UUID userId, List<UUID> roleIds) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));
        List<Role> roles = roleRepository.findAllByIdInAndActive(roleIds, Boolean.TRUE);
        user.getRoles().addAll(roles);
        return userRepository.save(user);
    }

    @Override
    public User removeRolesFromUser(UUID userId, List<UUID> roleIds) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));
        List<Role> roles = roleRepository.findAllByIdInAndActive(roleIds, Boolean.TRUE);
        roles.forEach(user.getRoles()::remove);
        return userRepository.save(user);
    }

    @Override
    public User addPrivilegesToUser(UUID userId, Set<UUID> privilegeIds) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));
        List<Privilege> privileges = privilegeService.getAllPrivilegesByIds(privilegeIds);
        user.getPrivileges().addAll(privileges);
        return userRepository.save(user);
    }

    @Override
    public User removePrivilegesFromUser(UUID userId, Set<UUID> privilegeIds) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));
        List<Privilege> privileges = privilegeService.getAllPrivilegesByIds(privilegeIds);
        privileges.forEach(user.getPrivileges()::remove);
        return userRepository.save(user);
    }

    @Override
    public List<Role> getAllRoles(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));
        return new ArrayList<>(user.getRoles());
    }

    @Override
    public List<Privilege> getAllPrivilege(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(User::validateStatus)
                .orElseThrow(() -> new AuthException("User not found for given id: " + userId));

        // Collect user-specific privileges and role-based privileges into a set to ensure uniqueness
        Set<Privilege> privilegeSet = new HashSet<>(user.getPrivileges());

        // Collect role privileges
        user.getRoles().stream()
                .map(Role::getPrivileges)
                .flatMap(Collection::stream)
                .forEach(privilegeSet::add);

        // Return a list created from the set
        return new ArrayList<>(privilegeSet);
    }
}
