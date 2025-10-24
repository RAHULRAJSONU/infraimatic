package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.record.RoleCreateRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface RoleService {

    Role createRole(RoleCreateRecord createRecord);

    Optional<Role> getRoleById(UUID id);

    Role updateRole(UUID id, RoleCreateRecord roleDetails);

    Role addPrivilegeToRole(UUID id, Set<UUID> privileges);

    void deleteRole(UUID id);

    Page<Role> listRoles(Pageable p);

    Role removePrivilegeToRole(UUID id, Set<UUID> privilegeIds);
}
