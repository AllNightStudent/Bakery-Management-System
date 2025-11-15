package com.swp.repository;

import com.swp.entity.OTP;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface OTPRepository extends JpaRepository<OTP, Long> {
    // Tìm OTP hợp lệ theo email, chưa dùng, và chưa hết hạn
    Optional<OTP> findFirstByEmailAndIsUsedFalseAndExpiresAtAfter(String email, LocalDateTime now);
}