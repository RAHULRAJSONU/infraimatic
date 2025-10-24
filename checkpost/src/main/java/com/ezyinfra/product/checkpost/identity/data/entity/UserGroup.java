package com.ezyinfra.product.checkpost.identity.data.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "identity_user_groups")
@NoArgsConstructor
@AllArgsConstructor
public class UserGroup extends AbstractPersistable {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 256)
    private String description;

    private boolean active;

    @ManyToMany
    @JoinTable(
            name = "identity_user_group_users",
            joinColumns = @JoinColumn(name = "user_group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    @ToString.Exclude
    private Collection<User> users = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "identity_user_group_roles",
            joinColumns = @JoinColumn(name = "user_group_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @ToString.Exclude
    private Collection<Role> roles = new ArrayList<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "identity_user_group_privileges",
            joinColumns = @JoinColumn(name = "user_group_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    @ToString.Exclude
    private Collection<Privilege> privileges = new ArrayList<>();

    public List<SimpleGrantedAuthority> getAuthorities() {
        Set<String> authorities = new HashSet<>();

        // Collect authorities from individual roles
        authorities.addAll(
                getRoles().stream()
                        .flatMap(role -> role.getPrivileges().stream().map(Privilege::expression))
                        .toList()
        );

        // Collect direct authorities from user group
        authorities.addAll(getPrivileges().stream().map(Privilege::expression).toList());

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
