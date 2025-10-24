package com.ezyinfra.product.checkpost.identity.data.record;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PrivilegeCreateRecord(
        @NotNull String name,
        String description,
        boolean active,
        @NotNull UUID actionId,
        @NotNull UUID resourceId) {
    public Privilege toEntity() {
        Privilege entity = new Privilege();
        entity.setName(name);
        entity.setDescription(description);
        entity.setActive(active);
        return entity;
    }
}
