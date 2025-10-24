package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.record.RoleCreateRecord;
import com.ezyinfra.product.checkpost.identity.data.repository.RoleRepository;
import com.ezyinfra.product.checkpost.identity.service.PrivilegeService;
import com.ezyinfra.product.checkpost.identity.service.RoleService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PrivilegeService privilegeService;

    public RoleServiceImpl(RoleRepository roleRepository, PrivilegeService privilegeService) {
        this.roleRepository = roleRepository;
        this.privilegeService = privilegeService;
    }

    @Override
    public Role createRole(RoleCreateRecord createRecord) {
        Role role = createRecord.toEntity();
        Set<UUID> privilegeIds = new HashSet<>(createRecord.privileges());
        Set<Privilege> privilege = new HashSet<>(privilegeService.getAllPrivilegesByIds(privilegeIds));
        role.setPrivileges(privilege);
        return roleRepository.save(role);
    }

    @Override
    public Optional<Role> getRoleById(UUID id) {
        return roleRepository.findById(id);
    }

    @Override
    public Role updateRole(UUID id, RoleCreateRecord roleDetails) {
        return roleRepository.findById(id)
                .map(role -> {
                    role.setName(roleDetails.name());
                    role.setDescription(roleDetails.description());
                    role.setActive(roleDetails.active());
                    return roleRepository.save(role);
                })
                .orElse(null);
    }

    @Override
    public Role addPrivilegeToRole(UUID id, Set<UUID> privileges) {
        Set<Privilege> privilege = new HashSet<>(privilegeService.getAllPrivilegesByIds(privileges));
        return roleRepository.findById(id)
                .map(role -> {
                    role.getPrivileges().addAll(privilege);
                    return roleRepository.save(role);
                })
                .orElse(null);
    }

    @Override
    public void deleteRole(UUID id) {
        roleRepository.findById(id)
                .map(role -> {
                    role.setActive(false);
                    roleRepository.save(role);
                    return true;
                });
    }

    @Override
    public Page<Role> listRoles(Pageable p) {
        return roleRepository.findAll(p);
    }

    @Override
    public Role removePrivilegeToRole(UUID id, Set<UUID> privilegeIds) {
        Set<Privilege> privilege = new HashSet<>(privilegeService.getAllPrivilegesByIds(privilegeIds));
        return roleRepository.findById(id)
                .map(role -> {
                    role.getPrivileges().removeAll(privilege);
                    return roleRepository.save(role);
                })
                .orElse(null);
    }
}
