package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.Privilege;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Optional<Privilege> findByNameAndActive(String name, boolean active);

    Optional<Privilege> findByIdAndActive(UUID id, boolean active);

    List<Privilege> findAllByActive(boolean active);

    List<Privilege> findAllByIdInAndActive(List<UUID> id, boolean active);
}
