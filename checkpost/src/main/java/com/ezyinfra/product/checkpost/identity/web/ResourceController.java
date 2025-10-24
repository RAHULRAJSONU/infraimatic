package com.ezyinfra.product.checkpost.identity.web;

import com.ezyinfra.product.common.exception.ResourceNotFoundException;
import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import com.ezyinfra.product.checkpost.identity.data.record.ResourceCreateRecord;
import com.ezyinfra.product.checkpost.identity.service.ResourceService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/identity/resource")
public class ResourceController {

    private final ResourceService resourceService;

    public ResourceController(ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    @PostMapping
    public ResponseEntity<Resource> createResource(@RequestBody ResourceCreateRecord createRecord) {
        return ResponseEntity.status(HttpStatus.CREATED).body(resourceService.createResource(createRecord));
    }

    @GetMapping
    public ResponseEntity<Page<Resource>> getAllResources(Pageable p) {
        return ResponseEntity.ok(resourceService.listResources(p));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> getResourceById(@PathVariable UUID id) {
        Resource resource = resourceService.getResourceById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id " + id));
        return ResponseEntity.ok(resource);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resource> updateResource(@PathVariable UUID id, @RequestBody ResourceCreateRecord resourceDetails) {
        Resource updatedResource = resourceService.updateResource(id, resourceDetails);
        return ResponseEntity.ok(updatedResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteResource(@PathVariable UUID id) {
        resourceService.deleteResource(id);
        return ResponseEntity.noContent().build();
    }
}
