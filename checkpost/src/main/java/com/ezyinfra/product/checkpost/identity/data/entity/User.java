package com.ezyinfra.product.checkpost.identity.data.entity;

import com.ezyinfra.product.common.enums.Gender;
import com.ezyinfra.product.common.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
@Builder
@ToString
@DynamicUpdate
@JsonDeserialize
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "identity_user")
@JsonIgnoreProperties(ignoreUnknown = true)
public class User extends AbstractPersistable implements UserDetails {

    @Column(nullable = false, length = 56)
    private String givenName;
    @Column(length = 56)
    private String middleName;
    @Column(length = 56)
    private String familyName;
    @Column(length = 56)
    private String nickname;
    @Column(length = 56)
    private String preferredUsername;
    @Column(length = 1200)
    private String address;
    @Column(unique = true, nullable = false, length = 256)
    private String email;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    @Temporal(TemporalType.DATE)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthdate;
    private String zoneInfo;
    @Column(length = 100)
    private String website;
    @Column(unique = true, nullable = false, length = 256)
    private String username;
    @JsonIgnore
    private String password;
    @ElementCollection
    private List<String> passwordHistory = new ArrayList<>();
    @Column(unique = true, nullable = false, length = 15)
    private String phoneNumber;
    @Builder.Default
    private LocalDateTime lastLogin = new Date().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    private String MobileOtp;
    private String emailOtp;
    @Column(unique = true, length = 56)
    private String apiKey;
    private String apiSecret;
    @Lob
    private String picture;
    private String locale;
    private int incorrectLoginAttempt;
    private boolean enabled;
    private boolean expired;
    private boolean emailVerified;
    private boolean phoneNumberVerified;
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "identity_users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "identity_users_privileges",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "privilege_id")
    )
    private Set<Privilege> privileges = new HashSet<>();

    @JsonIgnore
    @ManyToMany(mappedBy = "users")
    @ToString.Exclude
    private Collection<UserGroup> userGroups = new ArrayList<>();

    @Override
    public List<SimpleGrantedAuthority> getAuthorities() {

        // Collect authorities from individual roles
        Set<String> authorities = new HashSet<>(getRoles().stream()
                .flatMap(role -> role.getPrivileges().stream().map(Privilege::expression))
                .toList());

        // Collect direct authorities from user
        authorities.addAll(getPrivileges().stream().map(Privilege::expression).toList());
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
    }

    @Override
    public boolean isAccountNonExpired() {
        return !isExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return !getStatus().equals(UserStatus.LOCKED);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return !getStatus().equals(UserStatus.CREDENTIALS_EXPIRED);
    }

    @Override
    public boolean isEnabled() {
        return getStatus().equals(UserStatus.ACTIVE);
    }

    public String getName() {
        return String.join(" ",
                getOrEmpty(getGivenName()),
                getOrEmpty(getMiddleName()),
                getOrEmpty(getFamilyName())).trim();
    }

    public String getOrEmpty(String namePart) {
        return namePart == null ? "" : namePart.trim();
    }

    public boolean validateStatus() {
        return this.status.equals(UserStatus.ACTIVE);
    }

    public void setStatus(UserStatus status) {
        switch (status) {
            case ACTIVE -> this.setEnabled(true);
            case LOCKED, CREDENTIALS_EXPIRED, TEMP_SUSPENDED -> this.setEnabled(false);
            case PERM_SUSPENDED -> {
                this.setEnabled(false);
                this.setExpired(true);
            }
        }
        this.status = status;
    }
}