package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByIdAndActive(UUID id, boolean active);

    Optional<Role> findByNameAndActive(String name, boolean active);

    List<Role> findAllByActive(boolean active);

    List<Role> findAllByIdInAndActive(List<UUID> id, boolean active);
}
