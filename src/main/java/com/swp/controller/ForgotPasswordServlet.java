package com.swp.controller;

import com.swp.repository.OTPRepository;
import com.swp.repository.UserRepository;
import com.swp.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/forgot-password")
@RequiredArgsConstructor
public class ForgotPasswordServlet {

    private final UserRepository userDAO;
    private final OTPRepository otpDAO;


    private final ForgotPasswordService forgotPasswordService;

    @GetMapping
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    @PostMapping("/send-otp")
    public String sendOTP(@RequestParam String email, Model model) {
        boolean success = forgotPasswordService.sendOTPToEmail(email);

        if (success) {
            model.addAttribute("message", "OTP sent to your email. Please check your inbox.");
            model.addAttribute("email", email);
            return "send-password";
        } else {
            model.addAttribute("error", "Email not found in our system.");
            return "forgot-password";
        }
    }

    @GetMapping("/verify")
    public String showVerifyForm(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "send-password";
    }

    @PostMapping("/verify-otp")
    public String verifyOTPAndResetPassword(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model) {

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Passwords do not match.");
            return "send-password";
        }
        if (!forgotPasswordService.verifyOTP(email, otp)) {
            model.addAttribute("email", email);
            model.addAttribute("error", "Invalid or expired OTP. Please try again.");
            return "send-password";
        }
        if (forgotPasswordService.resetPassword(email, newPassword)) {
            model.addAttribute("success", "Password reset successfully! You can now login.");
            return "login";
        } else {
            model.addAttribute("email", email);
            model.addAttribute("error", "Failed to reset password. Please try again.");
            return "send-password";
        }
    }
}