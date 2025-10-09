package com.swp.service;

import com.swp.dto.request.ChangePasswordRequest;
import com.swp.entity.UserEntity;
import com.swp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;

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


}
