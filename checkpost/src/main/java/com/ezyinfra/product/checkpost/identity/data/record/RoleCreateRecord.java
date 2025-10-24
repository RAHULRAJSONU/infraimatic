package com.ezyinfra.product.checkpost.identity.data.record;

import com.ezyinfra.product.checkpost.identity.data.entity.Role;

import java.util.List;
import java.util.UUID;

public record RoleCreateRecord(
        String name,
        String description,
        boolean active,
        List<UUID> privileges
) {
    public Role toEntity() {
        Role entity = new Role();
        entity.setName(name);
        entity.setDescription(description);
        entity.setActive(active);
        return entity;
    }
}
