package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.Action;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ActionRepository extends JpaRepository<Action, UUID> {

    Optional<Action> findByNameAndActive(String name, boolean active);

    Optional<Action> findByIdAndActive(UUID id, boolean active);

    List<Action> findAllByIdInAndActive(List<UUID> id, boolean active);
}
