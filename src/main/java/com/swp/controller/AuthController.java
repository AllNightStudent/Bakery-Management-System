package com.swp.controller;

import com.swp.dto.request.RegisterRequest;
import com.swp.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @GetMapping("/login")
    public String loginPage() {
        return "login"; // Trả về login.html
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerRequest", new RegisterRequest());
        return "register";
    }


    @PostMapping("/register")
    public String registerSubmit(
            @Valid @ModelAttribute("registerRequest") RegisterRequest registerRequest,
            BindingResult result,
            Model model) {
        if (result.hasErrors()) {
            return "register"; // trả lại form cùng với lỗi
        }
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.confirmPassword", "Mật khẩu xác nhận không khớp");
            return "register";
        }
        try {
            authService.registerUser(registerRequest);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }
}
