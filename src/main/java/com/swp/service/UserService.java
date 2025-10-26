package com.swp.service;

import com.swp.dto.request.ChangePasswordRequest;
import com.swp.entity.RoleEntity;
import com.swp.entity.UserEntity;
import com.swp.repository.RoleRepository;
import com.swp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    @Transactional
    public boolean changePassword(String username, ChangePasswordRequest request) {
        UserEntity user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu hiện tại không đúng");
        }
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Mật khẩu mới và xác nhận mật khẩu không khớp");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new RuntimeException("Mật khẩu mới không được trùng với mật khẩu hiện tại");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return true;
    }

    public UserEntity getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return null; // chưa đăng nhập
        }

        String email = auth.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    public List<UserEntity> findAll() {
        return userRepository.findAll();
    }

    public List<UserEntity> findByRole(Long roleId) {
        return this.findAll().stream().filter(user -> user.getRole().getId().equals(roleId)).toList();
    }

    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }

    public void toggleStatus(Long id) {
        UserEntity user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setStatus(!user.getStatus());
            userRepository.save(user);
        }
    }

    public boolean deleteById(Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }
}
