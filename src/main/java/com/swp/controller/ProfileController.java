package com.swp.controller;

import com.swp.entity.UserEntity;
import com.swp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    // Hiển thị profile
    @GetMapping
    public String profile(Model model) {
        UserEntity currentUser = userService.getCurrentUser();
        model.addAttribute("currentUser", currentUser);
        return "profile";
    }

    // Cập nhật profile
    @PostMapping("/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam(required = false) String phone,
                                Model model) {

        UserEntity currentUser = userService.getCurrentUser();
        currentUser.setName(name);
        currentUser.setEmail(email);
        currentUser.setPhone(phone);

        userService.update(currentUser); // gọi service để lưu DB

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("successMessage", "Cập nhật thông tin thành công!");
        return "profile";
    }
}