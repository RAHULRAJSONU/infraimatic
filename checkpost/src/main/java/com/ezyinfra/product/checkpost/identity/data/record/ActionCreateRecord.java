package com.ezyinfra.product.checkpost.identity.data.record;

import com.ezyinfra.product.checkpost.identity.data.entity.Action;

public record ActionCreateRecord(String name, String description, boolean active) {
    public Action toEntity() {
        Action entity = new Action();
        entity.setName(name);
        entity.setDescription(description);
        entity.setActive(active);
        return entity;
    }
}
