package com.ezyinfra.product.checkpost.identity.data.repository;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.common.enums.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByIdAndStatus(UUID id, UserStatus status);

    Optional<User> findByEmailAndStatus(String email, UserStatus status);

    Optional<User> findByEmailIgnoreCaseAndStatus(String email, UserStatus status);

    @Query("""
       SELECT u
       FROM User u
       WHERE (u.email = :user OR u.username = :user)
         AND u.status = :status
       """)
    Optional<User> findByStatus(@Param("user") String user, @Param("status") UserStatus status);

    Optional<User> findByPhoneNumberAndStatus(String phoneNumber, UserStatus status);

    Optional<User> findByEmail(String email);

    Page<User> findAllByStatus(Pageable pageable, UserStatus status);

    List<User> findByEmailIn(List<String> emails);

    List<User> findByEmailInAndStatus(List<String> emails, UserStatus status);
}