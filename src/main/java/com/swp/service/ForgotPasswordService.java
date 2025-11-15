package com.swp.service;

import com.swp.entity.OTP;
import com.swp.entity.UserEntity;
import com.swp.repository.OTPRepository;
import com.swp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ForgotPasswordService {

    private final OTPRepository otpDAO;
    private final UserRepository userDAO;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    public String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public boolean sendOTPToEmail(String email) {
        Optional<UserEntity> user = userDAO.findByEmail(email);
        if (user.isEmpty()) {
            return false;
        }
        String otpCode = generateOTP();
        OTP otp = new OTP();
        otp.setEmail(email);
        otp.setOtpCode(otpCode);
        otp.setIsUsed(Boolean.FALSE);
        otpDAO.save(otp);
        String subject = "Password Reset OTP";
        emailService.sendEmailActiveAccount(email, subject, otpCode);
        return true;
    }

    // Verify OTP
    @Transactional
    public boolean verifyOTP(String email, String otpCode) {
        Optional<OTP> otp = otpDAO.findFirstByEmailAndIsUsedFalseAndExpiresAtAfter(email, LocalDateTime.now());
        if (otp.isEmpty()) {
            return false;
        }
        OTP otpEntity = otp.get();
        if (!otpEntity.getOtpCode().equals(otpCode)) {
            return false; // Sai mã OTP
        }
        otpEntity.markAsUsed();
        otpDAO.save(otpEntity);
        return true;
    }


    // Reset password
    @Transactional
    public boolean resetPassword(String email, String newPassword) {
        Optional<UserEntity> user = userDAO.findByEmail(email);
        if (user.isEmpty()) {
            return false;
        }
        UserEntity userEntity = user.get();
        userEntity.setPassword(passwordEncoder.encode(newPassword));
        userDAO.save(userEntity);
        return true;
    }
}