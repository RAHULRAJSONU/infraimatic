package com.ezyinfra.product.checkpost.identity.data.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Builder
@Table(name = "identity_privilege")
@NoArgsConstructor
@AllArgsConstructor
public class Privilege extends AbstractPersistable {
    @Column(nullable = false, unique = true, length = 256)
    private String name;
    private String description;
    private boolean active;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "action_id", nullable = false)
    private Action action;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ToString.Exclude
    @JsonIgnore
    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    private Collection<Role> roles;

    @JsonIgnore
    @ToString.Exclude
    @ManyToMany(mappedBy = "privileges", fetch = FetchType.LAZY)
    private Collection<UserGroup> userGroups = new ArrayList<>();

    public String expression() {
        return "%s:%s".formatted(resource.getName(), action.getName());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Privilege privilege)) return false;
        if (!super.equals(object)) return false;
        return getId() == privilege.getId() &&
                Objects.equals(name, privilege.name) &&
                Objects.equals(action.getId(), privilege.action.getId()) &&
                Objects.equals(resource.getId(), privilege.resource.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getId(), name, action.getId(), resource.getId());
    }
}
