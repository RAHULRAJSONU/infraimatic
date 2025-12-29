package com.ezyinfra.product.checkpost.identity.data.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "identity_role")
@NoArgsConstructor
@AllArgsConstructor
public class Role extends AbstractPersistable {
    @Column(nullable = false, length = 200, unique = true)
    private String name;
    private String description;
    private boolean active;

    @Transient
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Collection<User> users;

    @Transient
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    @ToString.Exclude
    private Collection<UserGroup> userGroups;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "identity_roles_privileges",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    private Set<Privilege> privileges;

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Role role)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(getId(), role.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId());
    }
}
