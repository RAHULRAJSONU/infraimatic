package com.ezyinfra.product.checkpost.identity.service;

import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.record.ResourceCreateRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface ResourceService {

    Resource createResource(ResourceCreateRecord createRecord);

    Optional<Resource> getResourceById(UUID id);

    Resource updateResource(UUID id, ResourceCreateRecord resourceDetails);

    void deleteResource(UUID id);

    Page<Resource> listResources(Pageable p);

}
