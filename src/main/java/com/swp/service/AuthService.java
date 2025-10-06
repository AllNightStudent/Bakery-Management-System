package com.swp.service;

import com.swp.dto.request.RegisterRequest;
import com.swp.entity.RoleEntity;
import com.swp.entity.UserEntity;
import com.swp.exception.BadRequestException;
import com.swp.repository.RoleRepository;
import com.swp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public void registerUser(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email đã được sử dụng");
        }
        RoleEntity roleUser = roleRepository.findByName("USER");
        UserEntity user = UserEntity.builder()
                .name(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .password(passwordEncoder.encode(request.getPassword())) // TODO: mã hoá mật khẩu
                .status(Boolean.TRUE)
                .role(roleUser)
                .build();
        userRepository.save(user);
    }


}
