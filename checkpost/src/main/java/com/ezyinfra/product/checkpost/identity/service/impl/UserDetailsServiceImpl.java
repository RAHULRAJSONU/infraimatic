package com.ezyinfra.product.checkpost.identity.service.impl;

import com.ezyinfra.product.checkpost.identity.data.entity.User;
import com.ezyinfra.product.checkpost.identity.data.repository.PrivilegeRepository;
import com.ezyinfra.product.checkpost.identity.data.repository.UserRepository;
import com.ezyinfra.product.common.enums.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final PrivilegeRepository privilegeRepository;

    private final UserRepository userRepository;

    @Override
    public User loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByStatus(username, UserStatus.ACTIVE)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}