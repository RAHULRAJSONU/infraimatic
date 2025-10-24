package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.repository.PrivilegeRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.ResourceRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.RoleRepository;
import com.ezyinfra.product.checkpost.identity.service.AccessControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccessControlServiceImpl implements AccessControlService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PrivilegeRepository privilegeRepository;

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public boolean hasPermission(User user, Resource resource, Privilege privilege) {
        if (hasRole(user, resource, privilege)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean hasRole(User user, Resource resource, Privilege privilege) {
        // Check if the user has the required role
        for (Role role : user.getRoles()) {
            if (role.getPrivileges().contains(privilege)) {
                return true;
            }
        }
        return false;
    }

}