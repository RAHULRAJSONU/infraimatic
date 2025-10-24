package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.entity.User;

public interface AccessControlService {

    boolean hasPermission(User user, Resource resource, Privilege privilege);

    boolean hasRole(User user, Resource resource, Privilege privilege);

}