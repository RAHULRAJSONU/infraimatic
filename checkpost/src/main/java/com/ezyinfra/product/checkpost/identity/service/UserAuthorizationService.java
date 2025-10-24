package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.entity.User;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface UserAuthorizationService {

    User mapRolesToUser(UUID userId, List<UUID> roleIds);

    User removeRolesFromUser(UUID userId, List<UUID> roleIds);

    User addPrivilegesToUser(UUID userId, Set<UUID> privilegeIds);

    User removePrivilegesFromUser(UUID userId, Set<UUID> privilegeIds);

    List<Role> getAllRoles(UUID userId);

    List<Privilege> getAllPrivilege(UUID userId);
}
