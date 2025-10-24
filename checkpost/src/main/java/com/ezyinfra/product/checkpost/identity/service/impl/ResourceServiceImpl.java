package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.record.ResourceCreateRecord;
import com.ezyinfra.product.checkpost.identity.data.repository.ResourceRepository;
import com.ezyinfra.product.checkpost.identity.service.ResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceServiceImpl implements ResourceService {

    @Autowired
    private ResourceRepository resourceRepository;

    @Override
    public Resource createResource(ResourceCreateRecord createRecord) {
        return resourceRepository.save(createRecord.toEntity());
    }

    @Override
    public Optional<Resource> getResourceById(UUID id) {
        return resourceRepository.findById(id);
    }

    @Override
    public Resource updateResource(UUID id, ResourceCreateRecord resourceDetails) {
        return resourceRepository.findById(id)
                .map(resource -> {
                    resource.setDescription(resourceDetails.description());
                    resource.setActive(resourceDetails.active());
                    return resourceRepository.save(resource);
                })
                .orElse(null);
    }

    @Override
    public void deleteResource(UUID id) {
        resourceRepository.findById(id)
                .map(resource -> {
                    resource.setActive(false);
                    resourceRepository.save(resource);
                    return true;
                });
    }

    @Override
    public Page<Resource> listResources(Pageable p) {
        return resourceRepository.findAll(p);
    }

}
