package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.UserGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserGroupRepository extends JpaRepository<UserGroup, UUID> {
    Optional<UserGroup> findByNameAndActive(String name, boolean active);

    Optional<UserGroup> findByName(String name);

    Page<UserGroup> findAllByActive(Pageable pageable, boolean active);

    @Query("SELECT ug.name FROM User u JOIN u.userGroups ug WHERE u.id = :userId")
    List<String> findUserGroupNamesByUserId(@Param("userId") UUID userId);

    @Query("SELECT u.email FROM UserGroup ug JOIN ug.users u WHERE ug.name = :name")
    List<String> findAssociatedUserEmailByGroupName(@Param("name") String name);

    boolean existsByName(String userGroupName);
}
