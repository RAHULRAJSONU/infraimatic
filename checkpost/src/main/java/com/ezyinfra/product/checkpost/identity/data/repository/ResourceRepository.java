package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResourceRepository extends JpaRepository<Resource, UUID> {
    Optional<Resource> findByIdAndActive(UUID id, boolean active);

    Optional<Resource> findByNameAndActive(String name, boolean active);

    List<Resource> findAllByActive(boolean active);
}
