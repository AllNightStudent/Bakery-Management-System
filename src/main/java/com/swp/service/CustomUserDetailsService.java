package com.swp.service;

import com.swp.entity.UserEntity;
import com.swp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// CustomUserDetailsService.java
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy user"));
        boolean enabled = Boolean.TRUE.equals(user.getStatus());
        boolean accountNonLocked = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;


        String roleName = user.getRole().getName();
        boolean hasRolePrefix = roleName != null && roleName.startsWith("ROLE_");

        var builder = org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                .password(user.getPassword())
                .accountExpired(!accountNonExpired)
                .accountLocked(!accountNonLocked)
                .credentialsExpired(!credentialsNonExpired)
                .disabled(!enabled);

        if (hasRolePrefix) {
            builder.authorities(roleName);
        } else {
            builder.roles(roleName);
        }

        return builder.build();
    }

}

