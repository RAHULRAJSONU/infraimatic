package com.ezyinfra.product.checkpost.identity.data.record;

import com.ezyinfra.product.checkpost.identity.data.entity.Resource;

public record ResourceCreateRecord(String name, String description, boolean active) {
    public Resource toEntity() {
        Resource resource = new Resource();
        resource.setName(name);
        resource.setDescription(description);
        resource.setActive(active);
        return resource;
    }
}
